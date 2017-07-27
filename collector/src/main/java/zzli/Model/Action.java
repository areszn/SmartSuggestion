package zzli.Model;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public class Action{

    public int appId;
    public int actionId;
    public int oId;
    public int cId;
    public String iId;
    public int userId;
    public int entryId;
    public int unitId;
    public long actionTime;
    public String uIp;

    public boolean check(){
        if(appId<1||appId>9)return false;
        if(actionId==0)actionId=1;
        if(actionId<1||actionId>12)return false;
        if(oId<1||oId>15)return false;
        if(entryId<0||entryId>2)return false;
        if(iId==null||iId.equals(""))return false;
        if(actionTime==0)actionTime=System.currentTimeMillis();
        return true;
    }

    public Action(){}

    public Action(String appId,String actionId,String oId,String cId,String iId,
                  String userId,String entryId,String unitId,String uIp){
        this.appId= Integer.valueOf(appId);
        this.actionId= Integer.valueOf(actionId);
        this.oId= Integer.valueOf(oId);
        this.cId= Integer.valueOf(cId);
        this.iId=iId;
        this.userId=Integer.valueOf(userId);
        this.entryId= Integer.valueOf(entryId);
        this.unitId= Integer.valueOf(unitId);
        this.actionTime=System.currentTimeMillis();
        this.uIp=uIp;
    }
}
