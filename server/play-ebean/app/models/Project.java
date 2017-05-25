package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import play.Logger;

import com.avaje.ebean.*;

/**
 * Project entity managed by Ebean
 */
@Entity 
public class Project extends Model {

    private static final long serialVersionUID = 1L;

    public static Finder<Long,Project> find = new Finder<Long,Project>(Project.class);

    public static List<Project> list() { return find.all(); }

    public static Project findByName(String name) {
        List<Project> projects = find.where()
                .eq("name", name)
                .findList();
        if (projects.size() == 1) {
            return projects.get(0);
        } else if (projects.size() > 1) {
            Logger.error("Too many projects named: " + name);
        }
        return null;
    }

	@Id
    public Long id;
    
    @Constraints.Required
    public String name;

    @Constraints.Required
    public boolean disabled;

}

