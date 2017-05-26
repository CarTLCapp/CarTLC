package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;

@Entity 
public class Version extends com.avaje.ebean.Model {

    public static final String PROJECT = "project";
    public static final String COMPANY = "company";
    public static final String EQUIPMENT = "equipment";
    public static final String NOTE = "note";

    private static final long serialVersionUID = 1L;

    public static Finder<Long,Version> find = new Finder<Long,Version>(Version.class);

    @Id
    public Long id;

    @Constraints.Required
    public String skey;

    @Constraints.Required
    public int ivalue;

    public static int get(String key) {
        List<Version> items = find.where().eq("skey", key).findList();
        if (items.size() > 0) {
            return items.get(0).ivalue;
        }
        return 0;
    }

    public static Version get_(String key) {
        List<Version> items = find.where().eq("skey", key).findList();
        if (items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    @Transactional
    public static void inc(String key) {
        Version version = get_(key);
        if (version == null) {
            version = new Version();
            version.skey = key;
            version.ivalue = 0;
        }
        version.ivalue++;
        version.save();
    }
}
