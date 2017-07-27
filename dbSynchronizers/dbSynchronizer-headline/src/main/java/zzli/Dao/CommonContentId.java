package zzli.Dao;


/**
 * Created by catfish on 2016-12-8.
 */
public class CommonContentId {
    public int appId;
    public int itemId;
    public int modelId;
    public int cId;
    public int dId;

    @Override
    public String toString() {
        return appId+"-"+itemId+"-"+modelId+"-"+cId+"-"+dId;
    }
    public CommonContentId(){}
}
