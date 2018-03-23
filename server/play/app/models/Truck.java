package models;

import java.util.*;
import java.lang.Long;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

/**
 * User entity managed by Ebean
 */
@Entity
public class Truck extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public String truck_number;

    @Constraints.Required
    public String license_plate;

    @Constraints.Required
    public int upload_id;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public long company_name_id;

    @Constraints.Required
    public int created_by;

    @Constraints.Required
    public boolean created_by_client;

    public static Finder<Long, Truck> find = new Finder<Long, Truck>(Truck.class);

    public static Truck get(long id) {
        if (id > 0) {
            return find.ref(id);
        }
        return null;
    }

    public static List<Truck> list() {
        return find.where()
                .orderBy("truck_number asc")
                .findList();
    }

    public static List<Truck> listFiltered() {
        return find.where()
                .disjunction()
                .ne("project_id", 0)
                .ne("company_name_id", 0)
                .endJunction()
                .orderBy("truck_number asc")
                .findList();
    }

    public static List<Truck> findByUploadId(int upload_id) {
        return find.where().eq("upload_id", upload_id).findList();
    }

    static List<Truck> findBy(long project_id, long company_name_id, String truck_number) {
        List<Truck> list;
        list = find.where()
                .eq("project_id", project_id)
                .eq("company_name_id", company_name_id)
                .eq("truck_number", truck_number)
                .findList();
        if (list.size() == 0) {
            list = null;
        } else if (list.size() > 1) {
            Logger.error("Found too many trucks with truck number="
                    + truck_number + ", project_id=" + project_id + ", company_name_id=" + company_name_id);
        }
        return list;
    }

    public static int cleanup() {
        StringBuilder sbuf = new StringBuilder();
        List<Truck> list = find.all();
        int count = 0;
        for (Truck truck : list) {
            if (truck.getProjectName().length() > 0) {
                continue;
            }
            if (truck.getCompanyName().length() > 0) {
                continue;
            }
            if (truck.countEntries() > 0) {
                Logger.info("Cannot delete truck, has entries: " + truck.toString());
                continue;
            }
            Logger.info("Deleting truck: " + truck.toString());
            truck.delete();
            count++;
        }
        Logger.info("Deleted " + count + " trucks");
        return count;
    }

    public static Truck findFirst(long project_id, long company_name_id, String truck_number) {
        List<Truck> list = findBy(project_id, company_name_id, truck_number);
        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    public static Truck add(long project_id, long company_id, long truck_id, String truck_number, String license_plate, int tech_id) {
        long company_name_id = 0;
        if (company_id > 0) {
           Company company = Company.get(company_id);
           if (company != null) {
               company_name_id = CompanyName.save(company.name);
           }
        }
        Truck truck = null;
        List<Truck> list;
        if (truck_id > 0) {
            truck = get(truck_id);
            if (truck.truck_number != null && truck_number != null && !truck.truck_number.equals(truck_number)) {
                Logger.error("TRUCK ID " + truck_id + " mismatch number: " + truck_number + " != " + truck.truck_number);
                truck = null;
            } else if (truck.license_plate != null && license_plate != null && !truck.license_plate.equals(license_plate)) {
                Logger.error("TRUCK ID " + truck_id + " mismatch license: " + license_plate + " != " + truck.license_plate);
                truck = null;
            } else if (truck.project_id > 0 && project_id > 0 && truck.project_id != project_id) {
                Logger.error("TRUCK ID " + truck_id + " mismatch project: " + project_id + " != " + truck.project_id);
                truck = null;
            } else if (truck.company_name_id > 0 && company_name_id > 0 && truck.company_name_id != company_name_id) {
                Logger.error("TRUCK ID " + truck_id + " mismatch company: " + company_name_id + " != " + truck.company_name_id);
                truck = null;
            }
        }
        if (truck == null) {
            if (truck_number != null) {
                if (project_id > 0 && company_name_id > 0) {
                    list = find.where()
                            .eq("project_id", project_id)
                            .eq("company_name_id", company_name_id)
                            .eq("truck_number", truck_number)
                            .findList();
                } else if (project_id > 0) {
                    list = find.where()
                            .eq("project_id", project_id)
                            .eq("truck_number", truck_number)
                            .findList();
                } else if (company_name_id > 0) {
                    list = find.where()
                            .eq("company_name_id", company_name_id)
                            .eq("truck_number", truck_number)
                            .findList();
                } else {
                    list = find.where()
                            .eq("truck_number", truck_number)
                            .findList();
                }
            } else {
                if (project_id > 0 && company_name_id > 0) {
                    list = find.where()
                            .eq("project_id", project_id)
                            .eq("company_name_id", company_name_id)
                            .eq("license_plate", license_plate)
                            .findList();
                } else if (project_id > 0) {
                    list = find.where()
                            .eq("project_id", project_id)
                            .eq("license_plate", license_plate)
                            .findList();
                } else if (company_name_id > 0) {
                    list = find.where()
                            .eq("company_name_id", company_name_id)
                            .eq("license_plate", license_plate)
                            .findList();
                } else {
                    list = find.where()
                            .eq("license_plate", license_plate)
                            .findList();
                }
            }
            if (list != null) {
                if (list.size() > 1) {
                    Logger.error("Found too many trucks with "
                            + truck_number + ", " + license_plate + ", project_id=" + project_id + ", company_name_id=" + company_name_id);
                    for (Truck t : list) {
                        Logger.error(t.toString());
                    }
                    truck = list.get(0);
                } else if (list.size() == 1) {
                    truck = list.get(0);
                }
            }
        }
        if (truck == null) {
            truck = new Truck();
            truck.truck_number = truck_number;
            truck.project_id = project_id;
            truck.company_name_id = company_name_id;
            truck.license_plate = license_plate;
            truck.created_by = tech_id;
            truck.save();
        } else {
            if (truck_number != null && !truck_number.equals(truck.truck_number)) {
                truck.truck_number = truck_number;
            }
            if (license_plate != null && !license_plate.equals(truck.license_plate)) {
                truck.license_plate = license_plate;
            }
            if (project_id != 0) {
                truck.project_id = project_id;
            }
            if (company_name_id != 0) {
                truck.company_name_id = company_name_id;
            }
            if (truck.created_by == 0) {
                truck.created_by = tech_id;
                truck.created_by_client = false;
            }
            truck.update();
        }
        return truck;
    }

    public String getTruckNumber() {
        if (truck_number != null) {
            return truck_number;
        } else {
            return "";
        }
    }

    public String getLicensePlate() {
        if (license_plate != null) {
            return license_plate;
        }
        return "";
    }

    public String getProjectName() {
        if (project_id == 0) {
            return "";
        }
        Project project = Project.find.ref(project_id);
        if (project == null) {
            return Long.toString(project_id) + "?";
        }
        return project.name;
    }

    public String getCompanyName() {
        if (company_name_id == 0) {
            return "";
        }
        String company = CompanyName.get(company_name_id);
        if (company == null) {
            return Long.toString(company_name_id) + "?";
        }
        return company;
    }

    public String getCompanyNameNullOkay() {
        if (company_name_id == 0) {
            return null;
        }
        return CompanyName.get(company_name_id);
    }

    public String getCreatedBy() {
        String name = "";
        if (created_by > 0) {
            if (created_by_client) {
                Client client = Client.find.byId((long) created_by);
                if (client != null) {
                    name = client.name;
                }
            } else {
                Technician tech = Technician.find.byId((long) created_by);
                if (tech != null) {
                    name = tech.fullName();
                }
            }
        }
        return name;
    }

    public String getLine() {
        StringBuilder sbuf = new StringBuilder();
        String bit = getTruckNumber();
        if (bit.length() > 0) {
            sbuf.append(bit);
        }
        bit = getLicensePlate();
        if (bit.length() > 0) {
            sbuf.append(" : ");
            sbuf.append(bit);
        }
        bit = getProjectName();
        if (bit.length() > 0) {
            sbuf.append(", ");
            sbuf.append(bit);
        }
        bit = getCompanyName();
        if (bit.length() > 0) {
            sbuf.append(", ");
            sbuf.append(bit);
        }
        return sbuf.toString();
    }

    public int countEntries() {
        return Entry.countEntriesForTruck(id);
    }

    public int countWorkOrders() {
        return WorkOrder.countWorkOrdersForTruck(id);
    }

    public boolean canDelete() {
        return countEntries() == 0 && countWorkOrders() == 0;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ID ");
        sbuf.append(id);
        sbuf.append(" ");
        if (truck_number != null) {
            sbuf.append(truck_number);
        }
        if (license_plate != null) {
            sbuf.append(" : ");
            sbuf.append(license_plate);
        }
        if (project_id > 0) {
            sbuf.append(", PROJECT ");
            sbuf.append(getProjectName());
        }
        if (company_name_id > 0) {
            sbuf.append(", COMPANY ");
            sbuf.append(getCompanyName());
        }
        return sbuf.toString();
    }
}

