/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.ebean.Transactional;

@Entity 
public class Strings extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public static Finder<Long, Strings> find = new Finder<Long, Strings>(Strings.class);

    @Id
    public Long id;

    @Constraints.Required
    public String string_value;

    public static List<Strings> list() {
        return find.all();
    }

    @Transactional
    public static Long get(String key) {
        List<Strings> items = find.where().eq("string_value", key).findList();
        if (items.size() > 0) {
            return items.get(0).id;
        }
        Strings strings = new Strings();
        strings.string_value = key;
        strings.save();
        return strings.id;
    }

    public static String get(Long id) {
        List<Strings> items = find.where().eq("id", id).findList();
        if (items.size() > 0) {
            return items.get(0).string_value;
        }
        return null;
    }
}
