package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

import com.avaje.ebean.*;
/**
 * User entity managed by Ebean
 */
@Entity
public class WorkOrder extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE = 100;

    @Id
    public Long id;

    @Constraints.Required
    public long client_id;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public long company_id;

    @Constraints.Required
    public long truck_id;

    public static Finder<Long, WorkOrder> find = new Finder<Long, WorkOrder>(WorkOrder.class);

    public static List<WorkOrder> list() {
        return find.all();
    }

    public static PagedList<WorkOrder> list(int page, String sortBy, String order) {
        return find.where()
                .orderBy(sortBy + " " + order)
                .findPagedList(page, PAGE_SIZE);
    }

    public String getClientName() {
        Client client = Client.find.ref(client_id);
        if (client == null) {
            return "";
        }
        return client.name;
    }

    public String getProjectLine() {
        Project project = Project.find.ref(project_id);
        if (project == null) {
            return "";
        }
        return project.name;
    }


    public String getCompany() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "";
        }
        return company.name;
    }

    public String getStreet() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "";
        }
        return company.street;
    }

    public String getState() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "";
        }
        return company.state;
    }

    public String getCity() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "";
        }
        return company.city;
    }

    public String getZipCode() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "";
        }
        return company.zipcode;
    }

    public String getTruckLine() {
        Truck truck = Truck.find.ref(truck_id);
        if (truck == null) {
            return null;
        }
        if (truck.license_plate != null) {
            return truck.license_plate;
        }
        return Integer.toString(truck.truck_number);
    }

    public String getFulfilledDate() {
        List<Entry> list = Entry.getFulfilledBy(this);
        if (list == null || list.size() <= 0) {
            return "";
        }
        Entry entry = list.get(0);

        return entry.getDate();
    }

    public boolean isFulfilled() {
        List<Entry> list = Entry.getFulfilledBy(this);
        return list != null && list.size() > 1;
    }
}

