package modules;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.validation.*;
import play.db.ebean.Transactional;
import play.data.format.*;

import com.avaje.ebean.*;

import models.*;
import play.Logger;

public class EntryStatus {

    Entry           entry;
    List<Equipment> checkedEquipments;
    List<Equipment> allEquipments;
    int             countPictures;
    boolean         complete;
    boolean         completePictures;
    boolean         completeEquipments;

    public EntryStatus(Entry entry) {
        this.entry = entry;
        checkedEquipments = removeOther(EntryEquipmentCollection.findEquipments(entry.equipment_collection_id));
        allEquipments = removeOther(ProjectEquipmentCollection.findEquipments(entry.project_id));
        countPictures = entry.getPictures().size();
        completeEquipments = checkedEquipments.size() >= allEquipments.size();
        completePictures = countPictures >= allEquipments.size();
        complete = completeEquipments && completePictures;
    }

    public String getShortLine() {
        if (entry.status != null) {
            if (entry.status == Entry.Status.MISSING) {
                return "Missing Truck";
            } else if (entry.status == Entry.Status.NEEDS_REPAIR) {
                return "Needs Repair";
            }
        }
        if (complete) {
            return "Complete";
        }
        return "Partial Install";
    }

    public Entry.Status getStatus() {
        if (entry.status != null) {
            return entry.status;
        }
        if (complete) {
            return Entry.Status.COMPLETE;
        }
        return Entry.Status.PARTIAL;
    }

    public String getLongLine() {
        StringBuilder sbuf = new StringBuilder();
        if (entry.status != null) {
            if (entry.status == Entry.Status.MISSING) {
                sbuf.append("Missing Truck");
            } else if (entry.status == Entry.Status.NEEDS_REPAIR) {
                sbuf.append("Needs Repair");
            }
        }
        if (sbuf.length() > 0) {
            sbuf.append("\n");
        }
        if (complete) {
            sbuf.append("Complete");
        } else {
            sbuf.append("Partial Install:");
        }
        sbuf.append("\n");
        sbuf.append(checkedEquipments.size());
        sbuf.append(" of ");
        sbuf.append(allEquipments.size());
        sbuf.append(" equipments checked.");

        if (!completeEquipments) {
            sbuf.append("\nEquipments Checked: ");
            ArrayList<Equipment> unprocessed = new ArrayList<Equipment>();
            for (Equipment equipment : allEquipments) {
                unprocessed.add(equipment);
            }
            boolean comma = false;
            for (Equipment equipment : checkedEquipments) {
                if (comma) {
                    sbuf.append(", ");
                } else {
                    comma = true;
                }
                sbuf.append(equipment.name);
                unprocessed.remove(equipment);
            }
            sbuf.append("\nEquipments Still Needed: ");
            comma = false;
            for (Equipment equipment : unprocessed) {
                if (comma) {
                    sbuf.append(", ");
                } else {
                    comma = true;
                }
                sbuf.append(equipment.name);
            }
        }
        sbuf.append("\n");
        sbuf.append(countPictures);
        sbuf.append(" of ");
        sbuf.append(allEquipments.size());
        sbuf.append(" pictures taken");
        return sbuf.toString();
    }

    List<Equipment> removeOther(List<Equipment> list) {
        ArrayList<Equipment> revised = new ArrayList<Equipment>();
        for (Equipment equipment : list) {
            if (!equipment.isOther()) {
                revised.add(equipment);
            }
        }
        if (revised.size() != list.size()) {
            return revised;
        }
        return list;
    }
}