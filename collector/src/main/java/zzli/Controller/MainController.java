package zzli.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zzli.Business.Recorder;
import zzli.Model.Action;


@RestController
public class MainController{
    @Autowired
    Recorder recorder;

    @RequestMapping("/useraction")
    public String recordAct(
              @RequestParam(value="appId",defaultValue = "0") String appid,
              @RequestParam(value="actionId",defaultValue = "1") String actionid,
              @RequestParam(value="oId",defaultValue = "0") String oid,
              @RequestParam(value="cId",defaultValue = "0") String cid,
              @RequestParam(value = "iId",defaultValue = "") String iid,
              @RequestParam(value = "userId",defaultValue = "0")String userid,
              @RequestParam(value = "entryId",defaultValue = "0")String entryid,
              @RequestParam(value = "unitId",defaultValue = "0")String unitid,
              @RequestParam(value = "uIp",defaultValue = "0.0.0.0")String uip,
              @RequestBody(required = false) String jss){
        String r;
        try {
            Action act=new Action(appid,actionid,oid,cid,iid,userid,entryid,unitid,uip);
            if(!act.check()){
                act=new ObjectMapper().readValue(jss,Action.class);
            }
            r= recorder.push2List(act);
        }catch (Exception e){
            return "0:参数错误！";
        }
        return r;
    }
}
