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

    @Id
    public Long id;
    
    @Constraints.Required
    public String first_name;

    @Constraints.Required
    public String last_name;

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

