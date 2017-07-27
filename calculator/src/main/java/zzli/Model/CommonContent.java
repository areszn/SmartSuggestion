package zzli.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by catfish on 2016-11-8.
 */
public class CommonContent {

    public String uuId;
    public CommonContentId commonContentId;
    public String text;
    private int act;

    @JsonIgnore
    public int getAct() {
        return act;
    }
    public void setAct(int act) {
        this.act = act;
    }


}
