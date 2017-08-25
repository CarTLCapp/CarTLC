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
    private static final int PAGE_SIZE = 30;

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
    private static Finder<Long,Company> find = new Finder<Long,Company>(Company.class);

    public static Company get(long id) {
        return find.byId(id);
    }

    public static void delete(long id) {
        find.ref(id).delete();
    }

    /**
     * Return a paged list of companies
     *
     * @param page Page to display
     * @param pageSize Number of companies per page
     * @param sortBy Property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static PagedList<Company> list(int page, String sortBy, String order, String filter, boolean disabled) {
        return
                find.where()
                        .eq("disabled", disabled)
                        .ilike("name", "%" + filter + "%")
                        .orderBy(sortBy + " " + order)
                        .findPagedList(page, PAGE_SIZE);
    }

    public static List<Company> appList(int tech_id) {
        List<Company> items = find.where().eq("disabled", false).findList();
        List<Company> result = new ArrayList<Company>();
        for (Company item : items) {
            if (item.created_by == 0 || item.created_by == tech_id) {
                result.add(item);
            } else if (Entry.hasEntryForCompany(tech_id, item.id)) {
                result.add(item);
            }
        }
        return result;
    }

    public static Company has(Company company) {
        List<Company> items = find.where()
                .eq("name", company.name)
                .eq("street", company.street)
                .eq("city", company.city)
                .eq("state", company.state)
                .eq("zipcode", company.zipcode)
                .findList();
        if (items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    public static Company parse(String line) throws DataErrorException {
        String [] fields = line.split(",");
        Company company = new Company();
        if (fields.length > 0) {
            company.name = fields[0].trim();
            if (company.name.isEmpty()) {
                throw new DataErrorException("No company name entered as first field: '" + line + "'");
            }
            if (fields.length > 1) {
                company.street = fields[1].trim();
                if (company.street.isEmpty()) {
                    throw new DataErrorException("Must enter a valid street name as second field: '" + line + "'");
                }
                if (fields.length > 2) {
                    company.city = fields[2].trim();
                    if (company.city.isEmpty()) {
                        throw new DataErrorException("Must enter a valid city as third field: '" + line + "'");
                    }
                    if (fields.length > 3) {
                        State state = State.find(fields[3].trim());
                        if (state == null) {
                            throw new DataErrorException("Invalid state:" + fields[3] + " from '" + line + "'");
                        }
                        company.state = state.abbr;
                        if (fields.length > 4) {
                            company.zipcode = fields[4].trim();
                        }
                    }
                }
            }
        } else {
            throw new DataErrorException("Must at least enter a company name");
        }
        return company;
    }

    public int countEntries() {
        return Entry.countEntriesForCompany(id);
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
            Technician tech = Technician.find.byId((long) created_by);
            if (tech != null) {
                return tech.fullName();
            }
        }
        return "";
    }

    public boolean hasAddress() {
        return (street != null && street.length() > 0) || (city != null && city.length() > 0) || (state != null && state.length() > 0);
    }

    public static boolean isDisabled(long id) {
        Company company = get(id);
        if (company == null) {
            return false;
        }
        return company.disabled;
    }

}

