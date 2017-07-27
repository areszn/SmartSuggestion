package zzli.Dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by catfish on 2016-11-29.
 */

@Entity
@Table(name = "my_content_share")
public class ContentShare {
    public int id;
    public String title;
    public int modelid;
    public int cid;
    @Id
    public int syncid;
    public int act;
}
