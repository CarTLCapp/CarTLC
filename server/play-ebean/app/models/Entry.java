package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import com.avaje.ebean.*;

/**
 * Entry entity managed by Ebean
 */
@Entity 
public class Entry extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    public static Finder<Long,Entry> find = new Finder<Long,Entry>(Entry.class);

    public static PagedList<Entry> list(int page, int pageSize, String sortBy, String order) {
        return
                find.where()
                        .orderBy(sortBy + " " + order)
                        .findPagedList(page, pageSize);
    }

    @Id
    public Long id;

    @Constraints.Required
    public int tech_id;

    @Formats.DateTime(pattern="yyyy-MM-dd kk:mm")
    public Date entry_time;

    @Constraints.Required
    public long project_id;

    @Constraints.Required
    public long address_id;

    @Constraints.Required
    public long equipment_collection_id;

    @Constraints.Required
    public long picture_collection_id;

    @Constraints.Required
    public long note_collection_id;

    @Constraints.Required
    public int truck_number;

    public String getTechName() {
        Client client = Client.find.byId((long) tech_id);
        if (client == null) {
            return "NOT FOUND: " + tech_id;
        }
        return client.fullName();
    }

    public String getProjectLine() {
        Project project = Project.find.byId(project_id);
        if (project == null) {
            return "NOT FOUND: " + project_id;
        }
        return project.name;
    }

    public String getAddressLine() {
        Company company = Company.find.byId(address_id);
        if (company == null) {
            return "NOT FOUND: " + address_id;
        }
        return company.getLine();
    }

    public String getTruckLine() {
        return Integer.toString(truck_number);
    }

    public String getEquipmentLine() {
        List<Equipment> equipments = EntryEquipmentCollection.findEquipments(equipment_collection_id);
        StringBuilder sbuf = new StringBuilder();
        for (Equipment equipment : equipments) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(equipment.name);
        }
        return sbuf.toString();
    }
}

