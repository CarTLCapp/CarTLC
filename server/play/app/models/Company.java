package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

/**
 * Company entity managed by Ebean
 */
@Entity
public class Company extends Model {

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
    public static Finder<Long,Company> find = new Finder<Long,Company>(Company.class);

    /**
     * Return a paged list of companies
     *
     * @param page Page to display
     * @param pageSize Number of companies per page
     * @param sortBy Property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static PagedList<Company> list(int page, int pageSize, String sortBy, String order, String filter) {
        return
                find.where()
                        .ilike("name", "%" + filter + "%")
                        .orderBy(sortBy + " " + order)
                        .findPagedList(page, pageSize);
    }

    public static List<Company> appList(int tech_id) {
        List<Company> items = find.where().eq("disabled", false).findList();
        List<Company> result = new ArrayList<Company>();
        for (Company item : items) {
            if (item.created_by == 0 || item.created_by == tech_id) {
                result.add(item);
            }
        }
        return result;
    }

    public static boolean has(Company company) {
        return find.where()
                .eq("name", company.name)
                .eq("street", company.street)
                .eq("city", company.city)
                .eq("state", company.state)
                .eq("zipcode", company.zipcode)
                .findList().size() > 0;
    }

    public static Company parse(String line) throws DataErrorException {
        String [] fields = line.split(",");
        Company company = new Company();
        if (fields.length == 1) {
            company.name = fields[0].trim();
        } else if (fields.length != 5) {
            throw new DataErrorException("wrong number of fields: " + fields.length);
        } else {
            company.name = fields[0].trim();
            company.street = fields[1].trim();
            company.city = fields[2].trim();
            company.zipcode = fields[4].trim();
            State state = State.find(fields[3].trim());
            if (state == null) {
                throw new DataErrorException("Invalid state:" + fields[3]);
            }
            company.state = state.abbr;
            if (company.street.isEmpty()) {
                throw new DataErrorException("Must enter a company street");
            }
            if (company.city.isEmpty()) {
                throw new DataErrorException("Must enter a company city");
            }
        }
        if (company.name.isEmpty()) {
            throw new DataErrorException("Must enter a company name");
        }
        return company;
    }

    public String getLine() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(name);
        sbuf.append(", ");
        if (street != null) {
            sbuf.append(street);
        }
        sbuf.append(", ");
        if (city != null) {
            sbuf.append(city);
        }
        sbuf.append(", ");
        if (state != null) {
            sbuf.append(state);
        }
        sbuf.append(", ");
        if (zipcode != null) {
            sbuf.append(zipcode);
        }
        return sbuf.toString();
    }

    public String getCreatedBy() {
        if (created_by != 0) {
            Client client = Client.find.byId((long) created_by);
            if (client != null) {
                return client.fullName();
            }
        }
        return "";
    }

    public boolean hasAddress() {
        return (street != null && street.length() > 0) || (city != null && city.length() > 0) || (state != null && state.length() > 0);
    }

}

