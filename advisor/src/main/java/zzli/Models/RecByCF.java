package zzli.Models;


import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by catfish on 2016-11-30.
 */
public class RecByCF {
    @JsonIgnore
    public String uuId;//appid-userid-commoncontentid
    public int appId;
    public int userId;
    public CommonContentId commonContentId;
    public long timestamp;
    public RecByCF(){this.timestamp= System.currentTimeMillis();}
}
