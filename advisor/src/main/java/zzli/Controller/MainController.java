package zzli.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zzli.Dao.EsDao;
import zzli.Models.HotRequest;
import zzli.Models.RecRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MainController{
    @Autowired
    EsDao esDao;

    @RequestMapping("/getRec")
    public List getRec(@RequestParam(value = "appId",defaultValue = "0") String appId,
                       @RequestParam(value = "userId",defaultValue = "0") String userId,
                       @RequestParam(value = "modelId",defaultValue = "0") String modelId,
                       @RequestParam(value = "cId",defaultValue = "0") String cId,
                       @RequestParam(value = "count",defaultValue = "10") String count,
                       @RequestParam(value = "page",defaultValue = "0") String page,
                       @RequestParam(value = "timestamp",defaultValue = "9499910154000") String timestamp,
                       @RequestParam(value = "isRefresh",defaultValue = "false") String isRefresh,
                       @RequestBody(required = false) String jss){
        RecRequest recRequest=null;
        List r=new ArrayList();
        r.add("参数错误");
        try {
            recRequest=new RecRequest(appId,userId,modelId,cId,count,page,timestamp,isRefresh);
        }catch (Exception e){
            return r;
        }
        if(recRequest.appId==0){
            try {
                recRequest=new ObjectMapper().readValue(jss,RecRequest.class);
            } catch (IOException e) {
                return r;
            }
        }
        return esDao.queryRecs(recRequest);
    }

    @RequestMapping("/getHot")
    public List getHot(@RequestParam(value = "appId",defaultValue = "0") String appId,
                       @RequestParam(value = "actionId",defaultValue = "0") String actionId,
                       @RequestParam(value = "modelId",defaultValue = "0") String modelId,
                       @RequestParam(value = "cId",defaultValue = "0") String cId,
                       @RequestParam(value = "count",defaultValue = "10") String count,
                       @RequestParam(value = "page",defaultValue = "0") String page,
                       @RequestBody(required = false) String jss){

        HotRequest hotRequest=null;
        List r=new ArrayList();
        r.add("参数错误");
        try {
            hotRequest=new HotRequest(appId,actionId,modelId,cId,count,page);
        }catch (Exception e){
            return r;
        }
        if(hotRequest.appId==0){
            try {
                hotRequest=new ObjectMapper().readValue(jss,HotRequest.class);
            } catch (IOException e) {
                return r;
            }
        }
        return esDao.queryHotItems(hotRequest);
    }
}
