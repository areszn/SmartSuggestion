package zzli.Models;

/**
 * Created by catfish on 2016-12-12.
 */
public class HotRequest {
    public int appId;
    public int actionId;
    public int modelId;
    public int cId;
    public int count;
    public int page;

    public HotRequest(String aId,String acId,String mId,String cId,String c,String p){
        this.appId=Integer.valueOf(aId);
        this.actionId=Integer.valueOf(acId);
        this.modelId=Integer.valueOf(mId);
        this.cId=Integer.valueOf(cId);
        this.count=Integer.valueOf(c);
        this.page=Integer.valueOf(p);
    }

    public HotRequest(){}
}
