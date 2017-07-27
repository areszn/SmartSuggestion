package zzli.Dao;

import ch.qos.logback.core.joran.action.Action;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import zzli.Config.MyConfig;
import zzli.Models.HotRequest;
import zzli.Models.RecByCF;
import zzli.Models.RecByContent;
import zzli.Models.RecRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by catfish on 2016-11-7.
 */
@Repository
public class EsDao {

    @Autowired
    TransportClient transportClient;
    @Autowired
    MyConfig myConfig;

    //查询根据内容推荐的结果
    public List queryRecsByContent(RecRequest recRequest){
        String index=myConfig.recByContentIndex+recRequest.appId;
        QueryBuilder queryBuilder=QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("appId",recRequest.appId))
                .must(QueryBuilders.termQuery("userId",recRequest.userId))
                .must(recRequest.modelId==0?QueryBuilders.matchAllQuery()
                        :QueryBuilders.termQuery("commonContentId.modelId",recRequest.modelId))
                .must(recRequest.cId==0?QueryBuilders.matchAllQuery()
                        :QueryBuilders.termQuery("commonContentId.cId",recRequest.cId))
                .must(recRequest.isRefresh?QueryBuilders.rangeQuery("timestamp").gte(recRequest.timestamp)
                        :QueryBuilders.rangeQuery("timestamp").lte(recRequest.timestamp));

        SearchResponse searchResponse= transportClient.prepareSearch(index)
                .setTypes(RecByContent.class.getSimpleName())
                .setQuery(queryBuilder)
                .addSort("timestamp",(recRequest.isRefresh?SortOrder.ASC:SortOrder.DESC))
                .setFrom(recRequest.page*recRequest.count).setSize(recRequest.count)
                .get();
        List r= Arrays.stream(searchResponse.getHits().getHits())
                .map(hit -> {
                    RecByContent recByContent=null;
                    try {
                        recByContent= new ObjectMapper().readValue(hit.getSourceAsString(), RecByContent.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return recByContent;
                }).collect(Collectors.toList());
        return r;
    }
    //查询根据用户聚类推荐的结果
    public List queryRecsByCF(RecRequest recRequest){
        String index=myConfig.recByCFIndex+recRequest.appId;
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("appId", recRequest.appId))
                    .must(QueryBuilders.termQuery("userId", recRequest.userId))
                    .must(recRequest.modelId == 0 ? QueryBuilders.matchAllQuery()
                            : QueryBuilders.termQuery("commonContentId.modelId", recRequest.modelId))
                    .must(recRequest.cId == 0 ? QueryBuilders.matchAllQuery()
                            : QueryBuilders.termQuery("commonContentId.cId", recRequest.cId));
        SearchResponse  searchResponse= transportClient.prepareSearch(index)
                    .setTypes(RecByCF.class.getSimpleName())
                    .setQuery(queryBuilder)
                    .setSize(500)
                    .get();

        List recs= Arrays.stream(searchResponse.getHits().getHits())
                .map(hit -> {
                    RecByCF recByCF=null;
                    try {
                        recByCF= new ObjectMapper().readValue(hit.getSourceAsString(), RecByCF.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return recByCF;
                }).collect(Collectors.toList());
        int n=recs.size();
        List<Integer> nums=new ArrayList();
        for(int i=0;i<n;i++){
            nums.add(i);
        }
        try {
            Collections.shuffle(nums);
        }catch (Exception e){
            e.printStackTrace();
        }
        List r=new ArrayList();
        int count=n>recRequest.count?recRequest.count:n;
        for (int i=0;i<count;i++){
            r.add(recs.get(nums.get(i)));
        }
        return r;
    }
    //查询推荐结果
    public List queryRecs(RecRequest recRequest){
        List r=null;
        try {
            if(recRequest.appId==1){
                r= queryRecsByContent(recRequest);
            }else {
                r= queryRecsByCF(recRequest);
            }
        }catch (Exception e){
        }
        return r;
    }
    //查询热门结果
    public List queryHotItems(HotRequest hotRequest) {
        class Agg{
            public int iId;
            public long Count;
        }
        List<Agg> r=new ArrayList<>();
        try {
            QueryBuilder queryBuilder=QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("appId",hotRequest.appId))
                    .must(hotRequest.actionId==0?QueryBuilders.matchAllQuery():QueryBuilders.termQuery("actionId",hotRequest.actionId))
                    .must(hotRequest.modelId==0?QueryBuilders.matchAllQuery():QueryBuilders.termQuery("modelId",hotRequest.modelId))
                    .must(hotRequest.cId==0?QueryBuilders.matchAllQuery():QueryBuilders.termQuery("cId",hotRequest.cId));
            SearchResponse searchResponse= transportClient.prepareSearch("date:*")
                    .setTypes(Action.class.getSimpleName())
                    .setQuery(queryBuilder)
                    .addAggregation(AggregationBuilders.terms("items").field("iId.keyword").size((hotRequest.page+1)*hotRequest.count))
                    .execute().actionGet();
            Terms useragg=searchResponse.getAggregations().get("items");
            int n=useragg.getBuckets().size();
            for(int i=hotRequest.page*hotRequest.count;i<(hotRequest.page+1)*hotRequest.count&&i<n;i++) {
                Terms.Bucket bucket=useragg.getBuckets().get(i);
                Agg agg=new Agg();
                agg.iId=Integer.valueOf(bucket.getKeyAsString());
                agg.Count=bucket.getDocCount();
                r.add(agg);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return r;
    }

}
