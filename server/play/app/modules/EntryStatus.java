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

    Entry   entry;
    int     countCheckedEquipments;
    int     countAllEquipments;
    int     countPictures;
    boolean complete;
    boolean completePictures;
    boolean completeEquipments;

    public EntryStatus(Entry entry) {
        this.entry = entry;
        countCheckedEquipments = EntryEquipmentCollection.findEquipments(entry.equipment_collection_id).size();
        countAllEquipments = ProjectEquipmentCollection.findEquipments(entry.project_id).size();
        countPictures = entry.getPictures().size();
        completeEquipments = countCheckedEquipments >= countAllEquipments;
        completePictures = countPictures >= countAllEquipments;
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
            sbuf.append("Partial Install");
        }
        sbuf.append("\n");
        sbuf.append(countCheckedEquipments);
        sbuf.append(" of ");
        sbuf.append(countAllEquipments);
        sbuf.append(" equipments checked.");
        sbuf.append("\n");
        sbuf.append(countPictures);
        sbuf.append(" of ");
        sbuf.append(countAllEquipments);
        sbuf.append(" pictures taken");
        return sbuf.toString();
    }
}