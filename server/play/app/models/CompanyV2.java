package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

import modules.DataErrorException;

/**
 * Company entity managed by Ebean
 */
@Entity
public class CompanyV2 extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String street;

    @Constraints.Required
    public String city;

    @Constraints.Required
    public String state;

    @Constraints.Required
    public String zipcode;

    @Constraints.Required
    public int created_by;

    @Constraints.Required
    public boolean disabled;

    /**
     * Generic query helper for entity Computer with id Long
     */
    public static Finder<Long, CompanyV2> find = new Finder<Long, CompanyV2>(CompanyV2.class);

    // TODO: Once data has been transfered, this code can be removed
    // and the database can be cleaned up by removing this table.
    public static void transfer() {
        List<CompanyV2> list = find.findList();
        for (CompanyV2 c2 : list) {
            Company company = new Company();
            company.name_id = (int) CompanyName.save(c2.name);
            company.street = c2.street;
            company.city = c2.city;
            company.state = c2.state;
            company.zipcode = c2.zipcode;
            company.created_by = c2.created_by;
            company.disabled = c2.disabled;
            company.save();
            c2.delete();
        }
    }

}

