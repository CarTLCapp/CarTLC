package com.cartlc.tracker.etc;

import android.content.Context;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataCollectionEquipmentProject;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataEquipment;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TablePictureCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 9/1/17.
 */

class EntryStatus {

    final List<DataEquipment> allEquipment;
    final List<DataEquipment> checkedEquipment;
    final TruckStatus         status;
    final int                 countPictures;
    final boolean             isComplete;
    final boolean             isCompleteEquip;
    final boolean             isCompletePicture;

    public EntryStatus(DataEntry entry) {
        this.allEquipment = removeOther(TableCollectionEquipmentProject.getInstance().queryForProject(entry.projectAddressCombo.projectNameId).getEquipment());
        this.checkedEquipment = removeOther(entry.getEquipment());
        this.status = entry.status;
        countPictures = entry.getPictures().size();
        isCompletePicture = countPictures >= allEquipment.size();
        isCompleteEquip = checkedEquipment.size() >= allEquipment.size();
        isComplete = isCompleteEquip && isCompletePicture;
    }

    public EntryStatus() {
        DataProjectAddressCombo curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        if (curGroup != null) {
            DataCollectionEquipmentProject collection = TableCollectionEquipmentProject.getInstance().queryForProject(curGroup.projectNameId);
            allEquipment = removeOther(collection.getEquipment());
        } else {
            allEquipment = new ArrayList<>();
        }
        checkedEquipment = removeOther(TableEquipment.getInstance().queryChecked());
        long picture_collection_id = PrefHelper.getInstance().getCurrentPictureCollectionId();
        countPictures = TablePictureCollection.getInstance().countPictures(picture_collection_id);
        status = PrefHelper.getInstance().getStatus();
        isCompletePicture = countPictures >= allEquipment.size();
        isCompleteEquip = checkedEquipment.size() >= allEquipment.size();
        isComplete = isCompleteEquip && isCompletePicture;
    }

    public int getNumPicturesTaken() {
        return countPictures;
    }

    public int getNumPicturesNeeded() {
        return allEquipment.size();
    }

    public String getString(Context ctx) {
        if (status != null) {
            if (status == TruckStatus.MISSING_TRUCK) {
                return ctx.getString(R.string.status_missing_truck);
            } else if (status == TruckStatus.NEEDS_REPAIR) {
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

    public boolean isCompleteEquipment() {
        return isCompleteEquip;
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
