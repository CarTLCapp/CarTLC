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
    public int upload_id;

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

    public static List<WorkOrder> findByClientId(long client_id) {
        return find.where().eq("client_id", client_id).findList();
    }

    public static WorkOrder findFirstByTruckId(long truck_id) {
        List<WorkOrder> list = find.where().eq("truck_id", truck_id).findList();
        if (list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("Found too many work orders with the same truck id ");
            sbuf.append(truck_id);
            Truck truck = Truck.find.byId(truck_id);
            if (truck != null) {
                sbuf.append("=");
                sbuf.append(truck.toString());
            }
            Logger.error(sbuf.toString());
        }
        return list.get(0);
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

    public static int countWorkOrdersForTruck(long truck_id) {
        return find.where().eq("truck_id", truck_id).findList().size();
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

    public Company getCompany() {
        return Company.get(company_id);
    }

    public String getCompanyName() {
        Company company = Company.get(company_id);
        if (company == null) {
            return "";
        }
        return company.getName();
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

    public Truck getTruck() {
        return Truck.get(truck_id);
    }

    public String getTruckLine() {
        Truck truck = Truck.get(truck_id);
        if (truck == null) {
            return null;
        }
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(truck.truck_number);
        if (truck.license_plate != null) {
            sbuf.append(":");
            sbuf.append(truck.license_plate);
        }
        return sbuf.toString();
    }

    public int getTruckNumber() {
        Truck truck = Truck.get(truck_id);
        if (truck == null) {
            return 0;
        }
        return truck.truck_number;
    }

    public String getFulfilledDate() {
        Entry entry  = Entry.getFulfilledBy(this);
        if (entry == null) {
            return "";
        }
        return entry.getDate();
    }

    public boolean isFulfilled() {
        return Entry.getFulfilledBy(this) != null;
    }

    public String getStatus() {
        Entry entry  = Entry.getFulfilledBy(this);
        if (entry == null) {
            return "";
        }
        return entry.getStatus();
    }

    public String getCellColor() {
        Entry entry  = Entry.getFulfilledBy(this);
        if (entry == null) {
            return "";
        }
        return entry.getCellColor();
    }

    static List<WorkOrder> findByUploadId(long upload_id, Client client) {
        if (client == null || client.is_admin) {
            return find.where()
                    .eq("upload_id", upload_id)
                    .findList();
        } else {
            return find.where()
                    .eq("upload_id", upload_id)
                    .eq("client_id", client.id)
                    .findList();
        }
    }

    public static List<WorkOrder> getLastUploaded(Client client) {
        int upload_id = Version.get(Version.NEXT_UPLOAD_ID);
        List<WorkOrder> list;
        for (; upload_id > 0; upload_id--) {
            list = findByUploadId(upload_id, client);
            if (list.size() > 0) {
                return list;
            }
        }
        return null;
    }

    public static int deleteLastUploaded(Client client) {
        List<WorkOrder> list = getLastUploaded(client);
        if (list == null) {
            return 0;
        }
        if (list.size() > 0) {
            int upload_id = list.get(0).upload_id;
            for (WorkOrder order : list) {
                order.delete();
            }
            if (upload_id > 0) {
                List<Company> clist = Company.findByUploadId(upload_id);
                for (Company company : clist) {
                    if (!Entry.hasEntryForCompany(company.id)) {
                        company.delete();
                    }
                }
                List<Truck> tlist = Truck.findByUploadId(upload_id);
                for (Truck truck : tlist) {
                    if (!Entry.hasEntryForTruck(truck.id)) {
                        truck.delete();
                    }
                }
            }
        }
        return list.size();
    }

    public static int lastUploadCount(Client client) {
        List<WorkOrder> list = getLastUploaded(client);
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public static void fixTrucks() {
        List<WorkOrder> list = list();
        boolean didOne = false;
        for (WorkOrder order : list) {
            Truck truck = Truck.get(order.truck_id);
            boolean changed = false;
            if (truck.project_id == 0) {
                truck.project_id = order.project_id;
                changed = true;
            }
            if (truck.company_name_id == 0) {
                String companyName = order.getCompanyName();
                truck.company_name_id = CompanyName.save(companyName);
                changed = true;
            }
            if (changed) {
                truck.update();
                didOne = true;
            }
        }
        if (didOne) {
            Version.inc(Version.VERSION_TRUCK);
        }
    }
}

