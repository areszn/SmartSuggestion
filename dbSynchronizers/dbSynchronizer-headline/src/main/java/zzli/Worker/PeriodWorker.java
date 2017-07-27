package zzli.Worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zzli.Dao.CommonContent;
import zzli.Dao.CommonContentId;
import zzli.Dao.ContentShare;
import zzli.Dao.ContentShareRepository;

/**
 * Created by catfish on 2016-11-29.
 */
@Component
public class PeriodWorker {
    @Autowired
    ContentShareRepository contentShareRepository;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Value("${my.redis.db-list-key}")
    String dbListKey;
    @Value("${my.app-id}")
    int appId;


    ObjectMapper objectMapper=new ObjectMapper();

    @Scheduled(fixedDelay = 10000)
    void syncDB(){
        Iterable<ContentShare> contents= contentShareRepository.findAll();
        if(contents==null)return;
        for (ContentShare content:contents) {
            CommonContent commonContent=new CommonContent();
            CommonContentId commonContentId=new CommonContentId();
            commonContentId.appId=appId;
            commonContentId.itemId=content.id;
            commonContentId.modelId=content.modelid;
            commonContentId.cId=content.cid;
            commonContentId.dId=0;
            commonContent.commonContentId=commonContentId;
            commonContent.uuId=commonContentId.toString();
            commonContent.text=content.title;
            commonContent.act=content.act;
            try {
                String jss=objectMapper.writeValueAsString(commonContent);
                stringRedisTemplate.opsForList().leftPush(dbListKey,jss);
                contentShareRepository.delete(content);
            }catch (Exception e){

            }
        }
    }
}
