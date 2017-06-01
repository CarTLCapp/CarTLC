package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

/**
 * User entity managed by Ebean
 */
@Entity 
public class Client extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public static Finder<Long,Client> find = new Finder<Long,Client>(Client.class);

    public static List<Client> list() {
        return find.all();
    }

    public static Client findByDeviceId(String device_id) throws DataErrorException {
        List<Client> items = find.where()
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
    public static Client findByName(String first_name, String last_name) throws DataErrorException {
        List<Client> items = find.where()
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

    @Formats.DateTime(pattern="yyyy-MM-dd kk:mm")
    public Date last_ping;

    @Constraints.Required
    public boolean disabled;

    @Constraints.Required
    public boolean reset_upload;

    public String fullName() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(first_name);
        sbuf.append(" " );
        sbuf.append(last_name);
        return sbuf.toString();
    }
}

