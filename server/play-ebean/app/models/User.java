package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;

/**
 * User entity managed by Ebean
 */
@Entity 
public class User extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
    @Constraints.Required
    public String first_name;

    @Constraints.Required
    public String last_name;

    /**
     * Generic query helper for entity User with id Long
     */
    public static Find<Long,User> find = new Find<Long,User>(){};

    public static Map<String,String> options() {
        LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
        for(Company c: Company.find.orderBy("name").findList()) {
            options.put(c.id.toString(), c.name);
        }
        return options;
    }

}

