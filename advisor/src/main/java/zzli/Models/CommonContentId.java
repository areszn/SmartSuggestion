package zzli.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by catfish on 2016-12-8.
 */
public class CommonContentId{
    @JsonIgnore
    public int appId;
    public int itemId;
    public int modelId;
    public int cId;
    @JsonIgnore
    public int dId;

    @Override
    public String toString() {
        return appId+"-"+itemId+"-"+modelId+"-"+cId+"-"+dId;
    }
}
