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

    public static boolean has(Company company) {
        List<Company> companies =
                find.where()
                        .eq("name", company.name)
                        .eq("street", company.street)
                        .eq("city", company.city)
                        .eq("state", company.state)
                        .findList();
        return companies.size() > 0;
    }

    public static Company parse(String line) throws DataErrorException {
        String [] fields = line.split(",");
        if (fields.length != 4) {
            throw new DataErrorException("Invalid number of fields, expected 4: " + line);
        }
        List<Company> companies = find.where()
                .eq("name", fields[0].trim())
                .findList();
        Company company = new Company();
        company.name = fields[0].trim();
        company.street = fields[1].trim();
        company.city = fields[2].trim();
        State state = State.find(fields[3].trim());
        if (state == null) {
            throw new DataErrorException("Invalid state:" + fields[3]);
        }
        company.state = state.abbr;

        if (company.name.isEmpty()) {
            throw new DataErrorException("Must enter a company name");
        }
        if (company.street.isEmpty()) {
            throw new DataErrorException("Must enter a company street");
        }
        if (company.city.isEmpty()) {
            throw new DataErrorException("Must enter a company city");
        }
        return company;
    }

}
