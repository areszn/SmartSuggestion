package zzli.Model;

/**
 * Created by catfish on 2016-12-8.
 */
public class CommonContentId{
    public int appId;
    public int itemId;
    public int modelId;
    public int cId;
    public int dId;

    @Override
    public String toString() {
        return appId+"-"+itemId+"-"+modelId+"-"+cId+"-"+dId;
    }

    public CommonContentId(Action action){
        this.appId=action.appId;
        this.itemId= Float.valueOf(action.iId).intValue();
        this.modelId=action.oId;
        this.cId=action.cId;
        this.dId=0;
    }

    public CommonContentId(){}

}
