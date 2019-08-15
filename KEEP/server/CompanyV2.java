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

    public static HashMap<Long, Long> transferMap = new HashMap<Long,Long>();

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
            transferMap.put(c2.id, company.id);
        }
    }

    public static void cleanup() {
        List<CompanyV2> list = find.findList();
        for (CompanyV2 c2 : list) {
            c2.delete();
        }
    }

    public static long transfer(long address_id) {
        if (transferMap.containsKey(address_id)) {
            return transferMap.get(address_id);
        }
        CompanyV2 company = find.ref(address_id);
        if (company != null) {
            List<Company> list = Company.find.where()
                    .eq("street", company.street)
                    .eq("city", company.city)
                    .eq("state", company.state)
                    .eq("zipcode", company.zipcode)
                    .findList();
            if (list.size() > 0) {
                return list.get(0).id;
            }
        }
        return 0;
    }

}

