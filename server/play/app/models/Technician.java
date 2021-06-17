/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;
import play.Logger;

import modules.DataErrorException;
import modules.TimeHelper;

/**
 * User entity managed by Ebean
 */
@Entity
public class Technician extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public static String RIP = "Inactive";
    public static int BASE_CODE = 0;

    public static final char RELOAD_CODE_PROJECT        = 'p';
    public static final char RELOAD_CODE_EQUIPMENT      = 'e';
    public static final char RELOAD_CODE_NOTES          = 'n';
    public static final char RELOAD_CODE_COMPANY        = 'c';
    public static final char RELOAD_CODE_TRUCK          = 't';
    public static final char RELOAD_CODE_VEHICLE        = 'v';
    public static final char RELOAD_CODE_FLOW           = 'f';

    @Id
    public Long id;

    @Constraints.Required
    public String first_name;

    @Constraints.Required
    public String last_name;

    @Constraints.Required
    public String device_id;

    @Formats.DateTime(pattern = "yyyy-MM-dd kk:mm")
    public Date last_ping;

    @Constraints.Required
    public boolean disabled;

    @Constraints.Required
    public boolean reset_upload;

    @Constraints.Required
    public String app_version;

    @Constraints.Required
    public String reload_code;

    @Constraints.Required
    public int code;

    public static Finder<Long, Technician> find = new Finder<Long, Technician>(Technician.class);

    public static void fixCodeZeros() {
        List<Technician> missingCode = find.where()
                .eq("code", 0)
                .findList();
        if (missingCode.size() > 0) {
            int lastCode = findLastCode();
            Transaction txn = Ebean.beginTransaction();
            try {
                for (Technician tech : missingCode) {
                    tech.code = ++lastCode;
                    tech.update();
                }
                txn.commit();
            } finally {
                txn.end();
            }
        }
    }

    public static List<Technician> listEnabled() {
        return find.where()
                .eq("disabled", false)
                .findList();
    }

    public static List<Technician> listDisabled() {
        return find.where()
                .eq("disabled", true)
                .findList();
    }

    public static List<Technician> listWithCode(int code) {
        return find.where()
                .eq("code", code)
                .findList();
    }

    public static int findLastCode() {
        List<Technician> hasCode = find.where()
                .ne("code", 0)
                .findList();
        int lastCode = BASE_CODE;
        if (hasCode.size() > 0) {
            for (Technician tech : hasCode) {
                if (tech.code > lastCode) {
                    lastCode = tech.code;
                }
            }
        }
        return lastCode;
    }

    @Transactional
    public static Technician findByName(String first_name, String last_name) {
        List<Technician> items = find.where()
                .eq("first_name", first_name)
                .eq("last_name", last_name)
                .findList();
        if (items.size() == 0) {
            return null;
        }
        if (items.size() > 1) {
            Logger.error("Found more than one technician with the name: " + first_name + ", " + last_name + " -> just using the first encountered");
        }
        return items.get(0);
    }

    public String fullName() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(first_name);
        sbuf.append(" ");
        sbuf.append(last_name);
        return sbuf.toString();
    }

    public String getCode() {
        return String.format("%04d", code);
    }

    public int countEntries() {
        return Entry.countEntriesForTechnician(id);
    }

    public String getAppVersion() {
        if (app_version != null) {
            return app_version;
        }
        return "";
    }

    public String lastPing() {
        return new TimeHelper().getDate(last_ping, null);
    }

    public static void AddReloadCode(long tech_id, char ch) {
        Technician tech = find.byId(tech_id);
        if (tech == null) {
            return;
        }
        tech.addReloadCode(ch);
        tech.update();
    }

    public void addReloadCode(char ch) {
        if (reload_code == null) {
            reload_code = String.valueOf(ch);
        } else {
            reload_code.concat(String.valueOf(ch));
        }
    }

    public static boolean techBeingUsed(long id) {
        if (Company.find.where()
                .eq("created_by", id)
                .eq("created_by_client", false).findList().size() > 0) {
            return true;
        }
        if (Entry.find.where().eq("tech_id", id).findList().size() > 0) {
            return true;
        }
        if (Equipment.find.where()
                .eq("created_by", id)
                .eq("created_by_client", false).findList().size() > 0) {
            return true;
        }
        if (Message.find.where().eq("tech_id", id).findList().size() > 0) {
            return true;
        }
        if (Note.find.where()
                .eq("created_by", id)
                .eq("created_by_client", false).findList().size() > 0) {
            return true;
        }
        if (Truck.find.where()
                .eq("created_by", id)
                .eq("created_by_client", false).findList().size() > 0) {
            return true;
        }
        if (Vehicle.find.where()
                .eq("tech_id", id).findList().size() > 0) {
            return true;
        }
        return false;
    }

    public static List<Long> findMatches(String name) {
        List<Technician> technicians = find.where()
                .disjunction()
                .eq("first_name", name)
                .eq("last_name", name)
                .endJunction()
                .findList();
        List<Long> result = new ArrayList<Long>();
        for (Technician tech : technicians) {
            result.add(tech.id);
        }
        return result;
    }

    public static List<Long> findMatches(String first_name, String last_name) {
        List<Technician> technicians = find.where()
                .eq("first_name", first_name)
                .eq("last_name", last_name)
                .findList();
        List<Long> result = new ArrayList<Long>();
        for (Technician tech : technicians) {
            result.add(tech.id);
        }
        return result;
    }

    public static Technician findMatch(String full_name) {
        String[] names = full_name.split(" ");
        if (names.length != 2) {
            return null;
        }
        List<Long> ids = findMatches(names[0], names[1]);
        if (ids.size() > 0) {
            return find.byId(ids.get(0));
        }
        return null;
    }

    public static boolean isValid(String full_name) {
        return findMatch(full_name) != null;
    }

    public static boolean isDisabled(long tech_id) {
        Technician tech = find.byId(tech_id);
        if (tech == null) {
            return true;
        }
        return tech.disabled;
    }

}

