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
    public long upload_id;

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

    public static WorkOrder has(WorkOrder order) {
        List<WorkOrder> list = find.where()
                .eq("client_id", order.client_id)
                .eq("project_id", order.project_id)
                .eq("company_id", order.company_id)
                .eq("truck_id", order.truck_id).findList();
        if (list != null & list.size() > 1) {
            return list.get(0);
        }
        return null;
    }

    public String getClientName() {
        Client client = Client.get(client_id);
        if (client == null) {
            return "";
        }
        return client.name;
    }

    public String getProjectLine() {
        Project project = Project.get(project_id);
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
        Truck truck = Truck.get(truck_id);
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

    static List<WorkOrder> findByUploadId(long upload_id) {
        return find.where().eq("upload_id", upload_id).findList();
    }

    public static List<WorkOrder> getLastUploaded() {
        int upload_id = Version.get(Version.NEXT_UPLOAD_ID) - 1;
        List<WorkOrder> list;
        for (; upload_id > 0; upload_id--) {
            list = findByUploadId(upload_id);
            if (list.size() > 0) {
                return list;
            }
        }
        return null;
    }

    public static int deleteLastUploaded() {
        List<WorkOrder> list = getLastUploaded();
        if (list != null) {
            for (WorkOrder order : list) {
                order.delete();
            }
        }
        return list.size();
    }

    public static int lastUploadCount() {
        List<WorkOrder> list = getLastUploaded();
        if (list == null) {
            return 0;
        }
        return list.size();
    }
}

