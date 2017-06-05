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
    public boolean disabled;

    @Constraints.Required
    public boolean is_local;

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

    public static boolean has(Company company) {
        if (company.hasAddress()) {
            return find.where()
                            .eq("name", company.name)
                            .eq("street", company.street)
                            .eq("city", company.city)
                            .eq("state", company.state)
                            .findList().size() > 0;
        } else {
            return find.where()
                            .eq("name", company.name)
                            .eq("zipcode", company.zipcode)
                            .findList().size() > 0;
        }
    }

    public static Company parse(String line) throws DataErrorException {
        String [] fields = line.split(",");
        Company company = new Company();
        if (fields.length == 4  || fields.length == 5) {
            company.name = fields[0].trim();
            company.street = fields[1].trim();
            company.city = fields[2].trim();
            State state = State.find(fields[3].trim());
            if (state == null) {
                throw new DataErrorException("Invalid state:" + fields[3]);
            }
            company.state = state.abbr;

            if (fields.length == 5) {
                company.zipcode = fields[4].trim();
            }
            if (company.street.isEmpty()) {
                throw new DataErrorException("Must enter a company street");
            }
            if (company.city.isEmpty()) {
                throw new DataErrorException("Must enter a company city");
            }
        } else if (fields.length == 2) {
            company.name = fields[0].trim();
            company.zipcode = fields[1].trim();
            if (company.zipcode.isEmpty()) {
                throw new DataErrorException("Must enter a company zip code");
            }
        } else {
            throw new DataErrorException("wrong number of fields: " + fields.length);
        }
        if (company.name.isEmpty()) {
            throw new DataErrorException("Must enter a company name");
        }
        return company;
    }

    public String getLine() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(name);

        if (hasAddress()) {
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
        }
        if (zipcode != null && zipcode.length() > 0) {
            sbuf.append(", ");
            sbuf.append(zipcode);
        }
        return sbuf.toString();
    }

    public boolean hasAddress() {
        return (street != null && street.length() > 0) || (city != null && city.length() > 0) || (state != null && state.length() > 0);
    }

}

