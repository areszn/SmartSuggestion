package zzli.Business;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer;
import org.grouplens.lenskit.baseline.UserMeanBaseline;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.util.LogContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.CloseableIterator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzli.Config.MyConfig;
import zzli.Dao.ElasticTemplate;
import zzli.Model.*;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by catfish on 2016-11-2.
 */
@Component
public class Worker {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ElasticTemplate elasticTemplate;
    @Autowired
    MyConfig myConfig;

    @Value("${my.max-keywords}")
    int maxKeywords;
    @Value("${my.redis.db-list-key}")
    String dbListKey;
    @Value("${my.redis.action-list-key}")
    String actionListKey;

    boolean consumingAction=true;
    boolean consumingDb=true;

    @PostConstruct
    public void init(){
        //beginConsumeAction();
        //beginConsumeDb();
    }

    //开始处理action
    void beginConsumeAction(){
        ExecutorService singleThreadExecutor=Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(() -> {
            ExecutorService fixedThreadPool=Executors.newFixedThreadPool(4);
            while (consumingAction) {
                try {
                    String js = redisTemplate.opsForList().rightPop(actionListKey);
                    if (js == null || js.equals("")) continue;
                    Action action = new ObjectMapper().readValue(js,Action.class);
                    if (action.check()) {
                        saveAction(action);
                        savePreference(action);

                        fixedThreadPool.execute(()->{
                            updateUserKeyword(action);
                        });
                    }
                } catch (Exception e) {}
            }
        });
    }

    //将action保存到es
    void saveAction(Action action)  {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy.MM");
        String index="date:"+simpleDateFormat.format(action.actionTime);
        elasticTemplate.save(index,null,null,action);
    }

    //根据action将用户偏好保存到es
    void savePreference(Action action) {
        if(action.actionId==11)return;
        if(action.userId==0)return;
        try {
            String index=myConfig.recByCFIndex+action.appId;
            User user=new User();
            user.userId=action.userId;
            elasticTemplate.save(index,null, String.valueOf(user.userId),user);
            CommonContentId commonContentId=new CommonContentId(action);
            Item item=elasticTemplate.findOne(index,null,commonContentId.toString(),Item.class);
            if(item==null){
                item=new Item(commonContentId);
                item.luuid=elasticTemplate.count(index,Item.class.getSimpleName())+1;
                elasticTemplate.save(index,null,commonContentId.toString(),item);
            }
            Rate rate=new Rate(user.userId,item.luuid,action.actionId,System.currentTimeMillis());
            elasticTemplate.save(index,null,rate.uuid,rate);
        }catch (Exception e){e.printStackTrace();}
    }

    //更新用户关键字
    void updateUserKeyword(Action action){
        try {
            if(action.appId!=1)return;
            String index=myConfig.recByContentIndex+action.appId;
            String keyword=null;
            if(action.actionId==11){
                keyword=action.iId;
            }else
            {
                CommonContentId commonContentId=new CommonContentId(action);
                CommonContent commonContent= elasticTemplate.findOne(index,
                        null,commonContentId.toString(),CommonContent.class);
                if(commonContent!=null){
                    keyword= commonContent.text;
                }
            }
            if(keyword==null||keyword.equals(""))return;

            String uuid=action.appId+"-"+action.userId;
            UserKeywords userKeywords= elasticTemplate.findOne(index,null,uuid,UserKeywords.class);
            if(userKeywords==null){
                userKeywords=new UserKeywords();
                userKeywords.uuId=uuid;
                userKeywords.appId=action.appId;
                userKeywords.userId=action.userId;
                userKeywords.keywords=keyword+"##";
            }else{
                String[] words=userKeywords.keywords.split("##");
                List keywordList=new ArrayList();
                keywordList.addAll(Arrays.asList(words));
                if(keywordList.contains(keyword)) {
                   computRecsByNewKeyword(action.userId,action.appId,keyword);
                   return;
                }
                keywordList.add(keyword);
                int n=keywordList.size();
                if(n>maxKeywords){
                    keywordList=keywordList.subList(n-maxKeywords,n);
                }
                userKeywords.keywords=String.join("##",keywordList);
            }
            elasticTemplate.save(index,null,userKeywords.uuId,userKeywords);
            computRecsByNewKeyword(action.userId,action.appId,keyword);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //根据新关键字计算推荐项
    void computRecsByNewKeyword(int userId,int appId, String keyword){
        String index=myConfig.recByContentIndex+appId;
        String[] idsNotin=elasticTemplate.findUserViewedIds(userId,appId);
        QueryBuilder queryBuilder=QueryBuilders.boolQuery()
                .must(QueryBuilders.moreLikeThisQuery(new String[]{keyword}))
                .mustNot(QueryBuilders.idsQuery().addIds(idsNotin));
        List<CommonContent> commonContents=elasticTemplate
                .findListByQuery(index,null,queryBuilder,3,CommonContent.class);
        if(commonContents==null||commonContents.size()==0)return;
        for(CommonContent commonContent:commonContents){
            RecByContent recByContent=new RecByContent(appId,userId,commonContent.commonContentId);
            elasticTemplate.save(index,null,recByContent.uuId,recByContent);
            //保存默认推荐，用户id=0
            RecByContent defaultRecByContent=new RecByContent(appId,0,commonContent.commonContentId);
            elasticTemplate.save(index,null,defaultRecByContent.uuId,defaultRecByContent);
        }
    }

    //开始处理新内容
    void beginConsumeDb(){
        ExecutorService singleThreadExecutor=Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(()->{
            ExecutorService fixedThreadPool=Executors.newFixedThreadPool(4);
            while (consumingDb) {
                try {
                    String js = redisTemplate.opsForList().rightPop(dbListKey);
                    if (js == null || js.equals("")) continue;
                    CommonContent commonContent = new ObjectMapper().readValue(js, CommonContent.class);
                    String index=myConfig.recByContentIndex+commonContent.commonContentId.appId;
                    if (commonContent.getAct() == 2) {
                        elasticTemplate.delete(index,CommonContent.class.getSimpleName(),commonContent.uuId);
                    } else {
                        elasticTemplate.save(index,CommonContent.class.getSimpleName(),commonContent.uuId,commonContent);
                        fixedThreadPool.execute(()->{
                            computeRecsByNewContent(commonContent);
                        });
                    }
                } catch (Exception e) {
                }
            }
        });

    }

    //根据新内容计算推荐项
    void computeRecsByNewContent(CommonContent commonContent){
        try {
            String index=myConfig.recByContentIndex+commonContent.commonContentId.appId;
            QueryBuilder queryBuilder=QueryBuilders.moreLikeThisQuery(new String[]{commonContent.text});
            CloseableIterator<UserKeywords> keywordsIterator=elasticTemplate.streamByQuery(
                    index,null,queryBuilder,UserKeywords.class);
            while (keywordsIterator.hasNext()){
                UserKeywords userKeywords=keywordsIterator.next();
                RecByContent recByContent=new RecByContent(userKeywords.appId,userKeywords.userId,commonContent.commonContentId);
                elasticTemplate.save(index,null,recByContent.uuId,recByContent);
            }
            keywordsIterator.close();
        }catch (Exception e){e.printStackTrace();}
    }

    //定时计算推荐项，依据协同过滤算法
    @Scheduled(fixedDelay =60000)
    void computeRecs(){
        for(String index:elasticTemplate.findRecByCFIndices()){
            int count= ((int) elasticTemplate.count(index, Rate.class.getSimpleName()));
            List<Rate> rates=elasticTemplate.findListByQuery(index,null,
                    QueryBuilders.matchAllQuery(),count,Rate.class);
            LenskitConfiguration config = new LenskitConfiguration();
            config.bind(ItemScorer.class)
                    .to(ItemItemScorer.class);
            config.bind(BaselineScorer.class, ItemScorer.class)
                    .to(UserMeanItemScorer.class);
            config.bind(UserMeanBaseline.class, ItemScorer.class)
                    .to(ItemMeanRatingItemScorer.class);
            config.bind(UserVectorNormalizer.class)
                    .to(BaselineSubtractingUserVectorNormalizer.class);
            EventDAO eventCollectionDAO= EventCollectionDAO.create(rates);
            config.bind(EventDAO.class).to(eventCollectionDAO);
            CloseableIterator<User> userIterator=null;
            try {
                LenskitRecommender rec = LenskitRecommender.build(config);
                ItemRecommender irec = rec.getItemRecommender();
                userIterator=elasticTemplate.streamByQuery(index,null,
                        QueryBuilders.matchAllQuery(),User.class);
                while (userIterator.hasNext()){
                    User user=userIterator.next();
                    List<ScoredId> recommendations = irec.recommend(user.userId,20);
                    for(ScoredId scoredId:recommendations){
                        QueryBuilder queryBuilder=QueryBuilders.termQuery("luuid",scoredId.getId());
                        Item item=elasticTemplate.findOneByQuery(index,null,queryBuilder,Item.class);
                        RecByCF recByCF=new RecByCF(item.commonContentId.appId,(int)user.userId,item.commonContentId);
                        elasticTemplate.save(index,null,recByCF.uuId,recByCF);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                userIterator.close();
            }
        }

    }
}
