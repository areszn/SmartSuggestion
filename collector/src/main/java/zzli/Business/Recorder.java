package zzli.Business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import zzli.Model.Action;

import java.util.Date;


/**
 * Created by catfish on 2016-11-1.
 */
@Component
public class Recorder {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Value("${my.redis.action-list-key}")
    String actionList;

    public String push2List(Action action) throws JsonProcessingException {
        if(!action.check())return "0:参数错误！";
        stringRedisTemplate.opsForList().leftPush(actionList,objectMapper.writeValueAsString(action));
        return "1";
    }
}
