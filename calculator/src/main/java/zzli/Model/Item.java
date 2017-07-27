package zzli.Model;


/**
 * Created by catfish on 2016-12-16.
 */
public class Item {
    public long luuid;
    public CommonContentId commonContentId;
    public Item(CommonContentId commonContentId) {
        this.commonContentId = commonContentId;
    }
    public Item(){}
}
