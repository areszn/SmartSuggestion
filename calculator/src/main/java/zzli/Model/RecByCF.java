package zzli.Model;


/**
 * Created by catfish on 2016-11-30.
 */
public class RecByCF {
    public String uuId;//appid-userid-commoncontentid
    public int appId;
    public int userId;
    public CommonContentId commonContentId;
    public long timestamp;

    public RecByCF(int appId, int userId, CommonContentId commonContentId) {
        this.appId = appId;
        this.userId = userId;
        this.commonContentId = commonContentId;
        this.uuId=this.toString();
        this.timestamp=System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return appId+"-"+userId+"-"+commonContentId.toString();
    }

    public RecByCF(){this.timestamp= System.currentTimeMillis();}

}
