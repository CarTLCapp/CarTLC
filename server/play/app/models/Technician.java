package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;
import modules.DataErrorException;
import play.Logger;

/**
 * User entity managed by Ebean
 */
@Entity
public class Technician extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public static Finder<Long, Technician> find = new Finder<Long, Technician>(Technician.class);

    public static List<Technician> list() {
        return find.all();
    }

    @Transactional
    public static Technician findByName(String first_name, String last_name, String device_id) {
        List<Technician> items = find.where()
                .eq("first_name", first_name)
                .eq("last_name", last_name)
                .eq("device_id", device_id)
                .findList();
        if (items.size() == 0) {
            items = find.where()
                    .eq("first_name", first_name)
                    .eq("last_name", last_name)
                    .findList();
            if (items.size() == 0) {
                return null;
            } else if (items.size() > 1) {
                Logger.error("Found more than one technician with the name: " + first_name + ", " + last_name + " -> just using the first encountered");
            }
        } else if (items.size() > 1) {
            Logger.error("Found more than one technician with the name: " + first_name + ", " + last_name + ", " + device_id + " -> just using the first encountered");
        }
        return items.get(0);
    }

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

    public String fullName() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(first_name);
        sbuf.append(" ");
        sbuf.append(last_name);
        return sbuf.toString();
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

    public static void AddReloadCode(long tech_id, char ch) {
        Technician tech = Technician.find.byId(tech_id);
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

    public static boolean canDelete(long id) {
        if (Company.find.where()
                .eq("created_by", id)
                .eq("created_by_client", false).findList().size() > 0) {
            return false;
        }
        if (Entry.find.where().eq("tech_id", id).findList().size() > 0) {
            return false;
        }
        if (Equipment.find.where()
                .eq("created_by", id)
                .eq("created_by_client", false).findList().size() > 0) {
            return false;
        }
        if (Message.find.where().eq("tech_id", id).findList().size() > 0) {
            return false;
        }
        if (Note.find.where()
                .eq("created_by", id)
                .eq("created_by_client", false).findList().size() > 0) {
            return false;
        }
        if (Truck.find.where()
                .eq("created_by", id)
                .eq("created_by_client", false).findList().size() > 0) {
            return false;
        }
        return true;
    }

}

