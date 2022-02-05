/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import java.lang.Long;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import com.avaje.ebean.*;

import play.Logger;

/**
 * User entity managed by Ebean
 * <p>
 * NOTE: This whole class is an obsolete idea.
 * What has replaced this, is a unique note data value with an associated unique picture. No need
 * for an entire table.
 */
@Entity
public class Truck extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 500;

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
        return find.byId(id);
    }

    public static List<Truck> list() {
        return find.where()
                .orderBy("truck_number asc")
                .findList();
    }

    public static PagedList<Truck> listFiltered(int page) {
        return find.where()
                .disjunction()
                .ne("project_id", 0)
                .ne("company_name_id", 0)
                .endJunction()
                .orderBy("truck_number asc")
                .findPagedList(page, PAGE_SIZE);
    }

    public static PagedList<Truck> listPaged(int page) {
        return find.where()
                .findPagedList(page, PAGE_SIZE);
    }

    public static List<Truck> findByUploadId(int upload_id) {
        return find.where().eq("upload_id", upload_id).findList();
    }

    static List<Truck> findByTruckNumber(long company_name_id, String truck_number) {
        return find.where()
                .eq("company_name_id", company_name_id)
                .eq("truck_number", truck_number)
                .findList();
    }

    static List<Truck> findByLicensePlate(long company_name_id, String license_plate) {
        return find.where()
                .eq("company_name_id", company_name_id)
                .eq("license_plate", license_plate)
                .findList();
    }

    public static List<Truck> findMatching(Truck matching) {
        if (matching.truck_number != null && matching.truck_number.length() > 0) {
            return findByTruckNumber(matching.company_name_id, matching.truck_number);
        }
        if (matching.license_plate != null && matching.license_plate.length() > 0) {
            findByLicensePlate(matching.company_name_id, matching.license_plate);
        }
        return new ArrayList<Truck>();
    }

    public static int countTrucks() {
        return find.where().findRowCount();
    }

    public static List<Truck> findBadTrucks() {
        return find.where()
                .eq("company_name_id", 0)
                .findList();
    }

    /**
     * Trucks not referenced in any entries nor work orders are removed.
     *
     * @return count of trucks deleted.
     * @oaram pageSize to process at a time.
     */
    public static List<Truck> getTrucks(int page, int pageSize) {
        int start = page * pageSize;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM truck LIMIT ");
        query.append(start);
        query.append(", ");
        query.append(pageSize);
        List<SqlRow> rows = Ebean.createSqlQuery(query.toString()).findList();
        ArrayList<Truck> trucks = new ArrayList<Truck>();
        for (SqlRow row : rows) {
            trucks.add(parseRow(row));
        }
        return trucks;
    }

    private static Truck parseRow(SqlRow row) {
        Truck truck = new Truck();
        truck.id = row.getLong("id");
        truck.truck_number = row.getString("truck_number");
        truck.license_plate = row.getString("license_plate");
        truck.upload_id = row.getInteger("upload_id");
        truck.project_id = row.getLong("project_id");
        truck.company_name_id = row.getLong("company_name_id");
        truck.created_by = row.getInteger("created_by");
        truck.created_by_client = row.getBoolean("created_by_client");
        return truck;
    }

    public static Truck findFirst(long project_id, long company_name_id, String truck_number) {
        List<Truck> list = findByTruckNumber(company_name_id, truck_number);
        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    public boolean truckNumberEquals(Truck other) {
        if (truck_number != null && other.truck_number != null) {
            return truck_number.equals(other.truck_number);
        }
        return false;
    }

    public boolean isTheSame(Truck other) {
        if (!getTruckNumber().equals(other.getTruckNumber())) {
            return false;
        }
        if (!getLicensePlate().equals(other.getLicensePlate())) {
            return false;
        }
        if (!getCompanyName().equals(other.getCompanyName())) {
            return false;
        }
        return true;
    }

    @Transactional
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
            if (!StringUtils.isEmpty(truck.truck_number) && !StringUtils.isEmpty(truck_number) && !truck.truck_number.equals(truck_number)) {
                error("TRUCK ID " + truck_id + " mismatch number: " + truck_number + " != " + truck.truck_number);
                truck = null;
            } else if (!StringUtils.isEmpty(truck.license_plate) && !StringUtils.isEmpty(license_plate) && !truck.license_plate.equals(license_plate)) {
                error("TRUCK ID " + truck_id + " mismatch license: " + license_plate + " != " + truck.license_plate);
                truck = null;
            } else if (truck.project_id > 0 && project_id > 0 && truck.project_id != project_id) {
                error("TRUCK ID " + truck_id + " mismatch project: " + project_id + " != " + truck.project_id);
                truck = null;
            } else if (truck.company_name_id > 0 && company_name_id > 0 && truck.company_name_id != company_name_id) {
                error("TRUCK ID " + truck_id + " mismatch company: " + company_name_id + " != " + truck.company_name_id);
                truck = null;
            }
        }
        if (project_id == 0) {
            warn("Truck.add(" + project_id + ", " + company_id + ", " + truck_id + ", " + truck_number + ", " + license_plate + ", " + tech_id + ") CID=" + company_name_id + "[2]");
            warn("No project id entered");
        }
        if (company_name_id == 0) {
            error("Truck.add(" + project_id + ", " + company_id + ", " + truck_id + ", " + truck_number + ", " + license_plate + ", " + tech_id + ") CID=" + company_name_id + "[2]");
            error("No company name entered");
            return null;
        }
        if (StringUtils.isEmpty(truck_number) && StringUtils.isEmpty(license_plate)) {
            error("Truck.add(" + project_id + ", " + company_id + ", " + truck_id + ", " + truck_number + ", " + license_plate + ", " + tech_id + ") CID=" + company_name_id + "[2]");
            error("No truck number nor license_plate entered");
            return null;
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

    private static List<Truck> narrowByProject(List<Truck> items, long project_id) {
        ArrayList<Truck> result = new ArrayList<Truck>();
        for (Truck item : items) {
            if (item.project_id == project_id) {
                result.add(item);
            }
        }
        return result;
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

    public String getRootProjectName() {
        if (project_id == 0) {
            return "";
        }
        Project project = Project.find.byId(project_id);
        if (project == null) {
            return Long.toString(project_id) + "?";
        }
        return project.getRootProjectName();
    }

    public String getSubProjectName() {
        if (project_id == 0) {
            return "";
        }
        Project project = Project.find.byId(project_id);
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

    @Transactional
    public void setCompanyName(long company_name_id) {
        this.company_name_id = company_name_id;
        update();
        Truck recheck = get(id);
    }

    public String getCreatedBy() {
        String name = "";
        if (created_by > 0) {
            if (created_by_client) {
                Client client = Client.find.byId((long) created_by);
                if (client != null) {
                    name = client.name;
                } else {
                    name = Technician.RIP;
                }
            } else {
                Technician tech = Technician.find.byId((long) created_by);
                if (tech != null) {
                    name = tech.fullName();
                } else {
                    name = Technician.RIP;
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
        bit = getRootProjectName();
        if (bit.length() > 0) {
            sbuf.append(", ");
            sbuf.append(bit);
            bit = getSubProjectName();
            if (bit.length() > 0) {
                sbuf.append(" - ");
                sbuf.append(bit);
            }
        } else {
            bit = getSubProjectName();
            if (bit.length() > 0) {
                sbuf.append(", ");
                sbuf.append(bit);
            }
        }
        bit = getCompanyName();
        if (bit.length() > 0) {
            sbuf.append(", ");
            sbuf.append(bit);
        }
        return sbuf.toString();
    }

    public String getID() {
        StringBuilder sbuf = new StringBuilder();
        if (truck_number != null && !truck_number.isEmpty()) {
            sbuf.append(truck_number);
        }
        if (license_plate != null && !license_plate.isEmpty()) {
            if (sbuf.length() > 0) {
                sbuf.append(" : ");
            }
            sbuf.append(license_plate);
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
        sbuf.append("Truck(id=");
        sbuf.append(id);

        if (truck_number != null && !truck_number.isEmpty()) {
            sbuf.append(", number=");
            sbuf.append(truck_number);
        }
        if (license_plate != null && !license_plate.isEmpty()) {
            sbuf.append(", plate=");
            sbuf.append(license_plate);
        }
        if (project_id > 0) {
            sbuf.append(", project='");
            if (getRootProjectName() != null) {
                sbuf.append(getRootProjectName());
                sbuf.append(" - ");
                sbuf.append(getSubProjectName());
            } else {
                sbuf.append(getSubProjectName());
            }
            sbuf.append("'");
        }
        if (company_name_id > 0) {
            sbuf.append(", company=");
            sbuf.append(getCompanyName());
        }
        sbuf.append(")");
        return sbuf.toString();
    }

    public static List<Long> findMatches(String name) {
        List<Truck> trucks = find.where()
                .disjunction()
                .eq("truck_number", name)
                .eq("license_plate", name)
                .endJunction()
                .findList();
        List<Long> result = new ArrayList<Long>();
        for (Truck truck : trucks) {
            result.add(truck.id);
        }
        return result;
    }

    public static Truck getTruckByID(String id) {
        for (Truck truck : list()) {
            if (truck.getID().equals(id)) {
                return truck;
            }
        }
        return null;
    }

    public static boolean isValid(String id) {
        return getTruckByID(id) != null;
    }

    // region Logger

    private static void error(String msg) {
        Logger.error(msg);
    }

    private static void warn(String msg) {
        Logger.warn(msg);
    }

    private static void info(String msg) {
        Logger.info(msg);
    }

    private static void debug(String msg) {
        Logger.debug(msg);
    }

    // endregion Logger
}

