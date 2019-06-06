/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;

import play.data.format.*;
import play.data.validation.*;
import play.Logger;

import com.avaje.ebean.*;

import modules.DataErrorException;

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
    public int upload_id;

    @Constraints.Required
    public boolean disabled;

    @Constraints.Required
    public boolean created_by_client;

    /**
     * Generic query helper for entity Computer with id Long
     */
    public static Finder<Long, Company> find = new Finder<Long, Company>(Company.class);

    public static Company get(long id) {
        if (id > 0) {
            return find.byId(id);
        } else {
            return null;
        }
    }

    public static void delete(long id) {
        find.ref(id).delete();
    }

    public static List<Company> findByUploadId(int upload_id) {
        return find.where().eq("upload_id", upload_id).findList();
    }

    public static void saveNames() {
        for (Company company : find.where().findList()) {
            CompanyName.save(company.name);
        }
    }

    /**
     * Return a list of companies, but since we are only showing the names of each company
     * filter results such that only the first company is returned with a distinct name.
     *
     * @param order    Sort order (either or asc or desc)
     * @param disabled Show only disabled or non-disabled entries
     */
    public static List<Company> list(String order, boolean disabled) {
        StringBuilder query = new StringBuilder();
        List<Company> items = find.where()
                .eq("disabled", disabled)
                .orderBy("name" + " " + order)
                .findList();
        ArrayList<Company> result = new ArrayList<Company>();
        HashSet<String> set = new HashSet<String>();
        for (Company company : items) {
            if (!set.contains(company.name)) {
                result.add(company);
                set.add(company.name);
            }
        }
        return result;
    }

    public static List<Company> listAddresses(String name, String sortBy, String order, boolean disabled) {
        return find.where()
                .eq("disabled", disabled)
                .eq("name", name)
                .orderBy(sortBy + " " + order)
                .findList();
    }

    public static List<Company> appList(int tech_id) {
        List<Company> items = find.where()
                .eq("disabled", false)
                .findList();
        List<Company> result = new ArrayList<Company>();
        for (Company item : items) {
            if (item.street == null || item.street.trim().isEmpty() ||
                item.city == null || item.city.trim().isEmpty() ||
                item.state == null || item.state.trim().isEmpty()) {
                continue;
            }
            if (item.created_by == 0 || item.created_by == tech_id || item.created_by_client) {
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

    public static List<Company> getCreatedByClient(int client_id) {
        return find.where()
                .eq("created_by", client_id)
                .eq("created_by_client", true)
                .findList();
    }

    public static Company parse(String line) throws DataErrorException {
        String[] fields = line.split(",");
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
        CompanyName.save(company.name);
        return company;
    }

    public int countAddresses() {
        return find.where()
                .eq("disabled", disabled)
                .eq("name", name)
                .findList().size();
    }

    public int countEntries() {
        return Entry.countEntriesForCompany(id);
    }

    public int countNameEntries() {
        List<Company> items = find.where()
                .eq("name", name)
                .findList();
        return Entry.countEntriesForCompanies(items);
    }

    public String getName() {
        return name;
    }

    public String getLine() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(getName());
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
            if (created_by_client) {
                Client client = Client.find.byId((long) created_by);
                if (client != null) {
                    if (upload_id > 0) {
                        return client.name + " Upload";
                    }
                    return client.name;
                }
            } else {
                Technician tech = Technician.find.byId((long) created_by);
                if (tech != null) {
                    return tech.fullName();
                }
            }
        } else if (created_by_client) {
            if (upload_id > 0) {
                return "Admin Upload";
            }
            return "Admin";
        }
        return Technician.RIP;
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

    public static boolean hasDisabled() {
        return find.where().eq("disabled", true).findList().size() > 0;
    }

    public static List<Long> findMatches(String name) {
        List<Company> companies = find.where()
                .disjunction()
                .ilike("name", "%" + name + "%")
                .ilike("street", "%" + name + "%")
                .ilike("city", "%" + name + "%")
                .ilike("state", "%" + name + "%")
                .ilike("zipcode", "%" + name + "%")
                .endJunction()
                .findList();
        List<Long> result = new ArrayList<Long>();
        for (Company company : companies) {
            result.add(company.id);
        }
        return result;
    }

    public static List<Company> findByName(String name) {
        return find.where().eq("name", name).findList();
    }

    public static Company findByAddress(String address) {
        for (Company company : find.where().findList()) {
            if (company.getLine().equals(address)) {
                return company;
            }
        }
        return null;
    }

    public static boolean isValidAddress(String address) {
        return findByAddress(address) != null;
    }
}

