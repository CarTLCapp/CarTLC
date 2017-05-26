package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;

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

    public static Client findByImei(String imei) throws DataErrorException {
        List<Client> items = find.where()
                .eq("imei", imei)
                .findList();
        if (items.size() == 1) {
            return items.get(0);
        } else if (items.size() > 1) {
            new DataErrorException("Too many clients with: " + imei);
        }
        return null;
    }

    @Id
    public Long id;
    
    @Constraints.Required
    public String first_name;

    @Constraints.Required
    public String last_name;

    @Constraints.Required
    public String imei;

    @Constraints.Required
    public boolean disabled;

    public String fullName() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(first_name);
        sbuf.append(" " );
        sbuf.append(last_name);
        return sbuf.toString();
    }
}

