package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;
import modules.DataErrorException;

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

    public static Technician findByDeviceId(String device_id) throws DataErrorException {
        List<Technician> items = find.where()
                .eq("device_id", device_id)
                .findList();
        if (items.size() == 1) {
            return items.get(0);
        } else if (items.size() > 1) {
            // THEN enhance this function to scan for disabled.
            new DataErrorException("Too many clients with: " + device_id);
        }
        return null;
    }

    @Transactional
    public static Technician findByName(String first_name, String last_name) throws DataErrorException {
        List<Technician> items = find.where()
                .eq("first_name", first_name)
                .eq("last_name", last_name)
                .findList();
        if (items.size() == 0) {
            return null;
        }
        if (items.size() > 1) {
            // Get rid of others.
            // TODO: ONLY REMOVE IF IT IS SAFE TO DO SO.
            // THAT IS, there are NO ENTRIES of this TECH-ID.
            // INSTEAD, set the DISABLED flag.

            // THEN enhance this function to scan for disabled.
            for (int i = 1; i < items.size(); i++) {
                items.get(i).delete();
            }
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

