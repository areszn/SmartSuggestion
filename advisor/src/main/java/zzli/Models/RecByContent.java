package zzli.Models;


import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by catfish on 2016-11-7.
 */
public class RecByContent {
    @JsonIgnore
    public String uuId;//userId+"-"+commonContentId.toString()
    public int appId;
    public int userId;
    public CommonContentId commonContentId;
    public Long timestamp;

    public RecByContent(){
        this.timestamp=System.currentTimeMillis();
    }
}
