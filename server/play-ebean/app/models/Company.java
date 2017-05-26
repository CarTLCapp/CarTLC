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

    public class MalformedFieldException extends Exception {
        public MalformedFieldException(String message) {
            super(message);
        }
    }

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
    public static Find<Long,Company> find = new Find<Long,Company>(){};

    /**
     * Return a paged list of companies
     *
     * @param page Page to display
     * @param pageSize Number of companies per page
     * @param sortBy Property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static List<Company> page(int page, int pageSize, String sortBy, String order, String filter) {
        return
                find.where()
                        .ilike("name", "%" + filter + "%")
                        .orderBy(sortBy + " " + order)
                        .fetch("company")
                        .findPagedList(page, pageSize);
    }

    public static List<Company> list() { return find.all(); }

    public static Company parse(String line) throws MalformedFieldException {
        List<Company> companies = find.where()
                .eq("name", name)
                .findList();
        Company company;
        if (companies.size() == 0) {
            company = new Company();
        } else {
            company = companies.get(0);
        }
        String [] fields = line.split(",");
        if (fields.size() != 4) {
            throw new MalformedFieldException("Invalid number of fields, expected 4: " + line);
        }
        company.name = fields[0].trim();
        company.street = fields[1].trim();
        company.city = fields[2].trim();
        State state = State.find(fields[3].trim());
        if (state == null) {
            throw new MalformedFieldException("Invalid state:" + fields[3]);
        }
        company.state = state.abbr;
        return company;
    }

}

