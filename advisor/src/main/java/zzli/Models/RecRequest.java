package zzli.Models;
/**
 * Created by catfish on 2016-12-12.
 */
public class RecRequest {
    public int appId;
    public int userId;
    public int modelId;
    public int cId;
    public int count;
    public int page;
    public long timestamp;
    public boolean isRefresh;

    public RecRequest(String aId,String uId,String mId,String cId,String c,String p,String t,String isR){
        this.appId=Integer.valueOf(aId);
        this.userId=Integer.valueOf(uId);
        this.modelId=Integer.valueOf(mId);
        this.cId=Integer.valueOf(cId);
        this.count=Integer.valueOf(c);
        this.page=Integer.valueOf(p);
        this.timestamp=Long.valueOf(t);
        this.isRefresh=Boolean.parseBoolean(isR);
    }
    public RecRequest(){}
}
