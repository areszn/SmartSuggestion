package zzli.Model;

/**
 * Created by catfish on 2016-11-7.
 */
public class UserKeywords {
    public String uuId;//appId-userId
    public int userId;
    public int appId;
    public String keywords;

    @Override
    public String toString() {
        return appId+"-"+userId;
    }

    public UserKeywords(){}
    public UserKeywords(int appId,int userId){
        this.appId=appId;
        this.userId=userId;
        this.uuId=this.toString();
    }
}
