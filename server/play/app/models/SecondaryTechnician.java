/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import java.lang.Long;

import javax.persistence.*;
import com.avaje.ebean.*;
import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import play.Logger;

/**
 * User entity managed by Ebean
 */
@Entity
public class SecondaryTechnician extends com.avaje.ebean.Model {

    private static final long serialVersionUID = 1L;

    @Id
    public Long id;

    @Constraints.Required
    public long entry_id;

    @Constraints.Required
    public long secondary_tech_id;

    public static Finder<Long, SecondaryTechnician> find = new Finder<Long, SecondaryTechnician>(SecondaryTechnician.class);

    public static long findSecondaryTechIdByEntryId(long entry_id) {
        List<SecondaryTechnician> list;
        list = find.where().eq("entry_id", entry_id).findList();
        if (list.size() == 0) {
            return 0L;
        }
        if (list.size() > 1) {
            Logger.error("Found too many secondary tech id rows with the same entry_id="
                    + entry_id);
        }
        SecondaryTechnician row = list.get(0);
        return row.secondary_tech_id;
    }

    public static Technician findSecondaryTechByEntryId(long entry_id) {
        long tech_id = findSecondaryTechIdByEntryId(entry_id);
        if (tech_id == 0) {
            return null;
        }
        return Technician.find.ref(tech_id);
    }

    public static void save(long entry_id, long secondary_tech_id) {
        List<SecondaryTechnician> list = find.where().eq("entry_id", entry_id).findList();
        if (list.size() > 0) {
            for (SecondaryTechnician ele : list) {
                Logger.info("Delete secondary_technician for entry " + entry_id);
                ele.delete();
            }
        }
        SecondaryTechnician item = new SecondaryTechnician();
        item.entry_id = entry_id;
        item.secondary_tech_id = secondary_tech_id;
        item.save();

        Logger.info("Added secondary_technician for " + entry_id + " of " + secondary_tech_id);
    }

    /**
     * @param ids: list of tech_ids to scan for.
     * @return list of entry id's with matches.
     */
    public static List<Long> findMatches(List<Long> ids) {
        List<Long> result = new ArrayList<Long>();
        if (ids.size() == 0) {
            return result;
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT ste.entry_id, ste.secondary_tech_id");
        query.append(" FROM secondary_technician AS ste");
        query.append(" WHERE ste.secondary_tech_id IN (");
        boolean first = true;
        for (long id: ids) {
            if (first) {
                first = false;
            } else {
                query.append(", ");
            }
            query.append(id);
        }
        query.append(")");
        List<SqlRow> entries = Ebean.createSqlQuery(query.toString()).findList();
        long entry_id;
        for (SqlRow row : entries) {
            result.add(row.getLong("entry_id"));
        }
        return result;
    }

}

