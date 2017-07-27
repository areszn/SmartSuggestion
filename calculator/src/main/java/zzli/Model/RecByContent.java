package zzli.Model;


/**
 * Created by catfish on 2016-11-7.
 */
public class RecByContent {
    public String uuId;//userId+"-"+commonContentId.toString()
    public int appId;
    public int userId;
    public CommonContentId commonContentId;
    public long timestamp;

    @Override
    public String toString(){
        return appId+"-"+userId+"-"+commonContentId.toString();
    }

    public RecByContent(){
        this.timestamp=System.currentTimeMillis();
    }

    public RecByContent(int appId,int userId,CommonContentId commonContentId){
        this.appId=appId;
        this.userId=userId;
        this.commonContentId=commonContentId;
        this.uuId=this.toString();
        this.timestamp=System.currentTimeMillis();
    }
}
