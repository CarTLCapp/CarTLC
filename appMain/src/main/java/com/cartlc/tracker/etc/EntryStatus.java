package com.cartlc.tracker.etc;

import android.content.Context;

import com.cartlc.tracker.R;
import com.cartlc.tracker.act.EquipmentSelectListAdapter;
import com.cartlc.tracker.data.DataCollectionEquipmentProject;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataEquipment;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 9/1/17.
 */

public class EntryStatus {

    final DataEntry mEntry;

    boolean             isComplete;
    boolean             isCompleteEquip;
    boolean             isCompletePicture;
    List<DataEquipment> allEquipment;
    List<DataEquipment> checkedEquipment;
    int                 countPictures;

    public EntryStatus(DataEntry entry) {
        mEntry = entry;
        DataCollectionEquipmentProject collection = TableCollectionEquipmentProject.getInstance().queryForProject(mEntry.projectAddressCombo.projectNameId);
        allEquipment = removeOther(collection.getEquipment());
        checkedEquipment = removeOther(mEntry.getEquipment());
        countPictures = mEntry.getPictures().size();
        isCompletePicture = countPictures >= allEquipment.size();
        isCompleteEquip = checkedEquipment.size() >= allEquipment.size();
        isComplete = isCompleteEquip && isCompletePicture;
    }

    public String getString(Context ctx) {
        if (mEntry.status != null) {
            if (mEntry.status == TruckStatus.MISSING_TRUCK) {
                return ctx.getString(R.string.status_missing_truck);
            } else if (mEntry.status == TruckStatus.NEEDS_REPAIR) {
                return ctx.getString(R.string.status_needs_repair);
            }
        }
        if (isComplete) {
            return ctx.getString(R.string.status_complete);
        }
        return ctx.getString(R.string.status_partial_install);
    }

    public String getLongString(Context ctx) {
        StringBuilder sbuf = new StringBuilder();
        if (isComplete) {
            sbuf.append(ctx.getString(R.string.status_complete));
        } else {
            sbuf.append(ctx.getString(R.string.status_partial_install));
            sbuf.append(":");
            if (!isCompleteEquip) {
                sbuf.append("\n");
                sbuf.append(ctx.getString(R.string.status_partial_install_equipments, checkedEquipment.size(), allEquipment.size()));
            }
            if (!isCompletePicture) {
                sbuf.append("\n");
                sbuf.append(ctx.getString(R.string.status_partial_install_pictures, countPictures, allEquipment.size()));
            }
        }
        return sbuf.toString();
    }

    public String getLine(Context ctx) {
        StringBuilder sbuf = new StringBuilder();
        if (isComplete) {
            sbuf.append(ctx.getString(R.string.status_complete));
        } else {
            sbuf.append(ctx.getString(R.string.status_partial_install));
            sbuf.append(": ");
            if (!isCompleteEquip) {
                sbuf.append(ctx.getString(R.string.status_partial_install_equipments2, checkedEquipment.size(), allEquipment.size()));
            }
            if (!isCompletePicture) {
                sbuf.append(" ");
                sbuf.append(ctx.getString(R.string.status_partial_install_pictures2, countPictures, allEquipment.size()));
            }
        }
        return sbuf.toString();
    }

    public String getEquipmentNeeded() {
        StringBuilder sbuf = new StringBuilder();
        if (!isCompleteEquip) {
            boolean comma = false;
            for (DataEquipment equipment : allEquipment) {
                if (!checkedEquipment.contains(equipment)) {
                    if (comma) {
                        sbuf.append(", ");
                    } else {
                        comma = true;
                    }
                    sbuf.append(equipment.name);
                }
            }
        }
        return sbuf.toString();
    }

    List<DataEquipment> removeOther(List<DataEquipment> list) {
        ArrayList<DataEquipment> revised = new ArrayList();
        for (DataEquipment equipment : list) {
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
