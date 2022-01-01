/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

import play.db.ebean.Transactional;

/**
 * Project entity managed by Ebean
 */
@Entity
public class Repaired extends Model {

    private static final long serialVersionUID = 1L;
    private static final int FLAG_DUP_TRUCK_ID = 0x01;

    @Id
    public Long id;

    @Constraints.Required
    public Long entry_id;

    @Constraints.Required
    public int instance_id;

    @Constraints.Required
    public int flags;

    public static Finder<Long, Repaired> find = new Finder<Long, Repaired>(Repaired.class);

    public static List<Repaired> list() {
        return find.all();
    }

    public static int count() {
        return find.where().findRowCount();
    }

    public static Repaired get(long id) {
        if (id > 0) {
            return find.byId(id);
        }
        return null;
    }

    public static boolean wasRepaired(long entry_id) {
        return find.where().eq("entry_id", entry_id).findRowCount() > 0;
    }

    public static List<Repaired> findByEntryId(long entry_id) {
        return find.where().eq("entry_id", entry_id).findList();
    }

    public static List<Repaired> findByInstanceId(int instanceId) {
        return find.where().eq("instance_id", instanceId).findList();
    }

    public static void addDupTruckId(long entry_id, int instance_id) {
        add(entry_id, instance_id, FLAG_DUP_TRUCK_ID);
    }

    @Transactional
    private static void add(long entry_id, int instance_id, int flag) {
        Repaired repaired = new Repaired();
        repaired.entry_id = entry_id;
        repaired.instance_id = instance_id;
        repaired.setFlag(flag, true);
        repaired.save();
    }

    @Transactional
    public void setDupTruckId(boolean value) {
        setFlag(FLAG_DUP_TRUCK_ID, value);
        update();
    }

    public boolean isDupTruck() {
        return hasFlag(FLAG_DUP_TRUCK_ID);
    }

    private void setFlag(int flag, boolean value) {
        if (value) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }
    }

    private boolean hasFlag(int flag) {
        return (flags & flag) == flag;
    }

}

