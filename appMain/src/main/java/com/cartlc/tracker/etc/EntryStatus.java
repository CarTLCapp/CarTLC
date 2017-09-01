package com.cartlc.tracker.etc;

import android.content.Context;

import com.cartlc.tracker.R;
import com.cartlc.tracker.data.DataCollectionEquipmentProject;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataEquipment;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;

import java.util.List;

/**
 * Created by dug on 9/1/17.
 */

public class EntryStatus {

    final DataEntry mEntry;

    boolean isComplete;
    boolean isCompleteEquip;
    boolean isCompletePicture;
    int     countAllEquipment;
    int     countCheckedEquipment;
    int     countPictures;

    public EntryStatus(DataEntry entry) {
        mEntry = entry;
        DataCollectionEquipmentProject collection = TableCollectionEquipmentProject.getInstance().queryForProject(mEntry.projectAddressCombo.projectNameId);
        countAllEquipment = collection.getEquipment().size();
        countCheckedEquipment = mEntry.getEquipment().size();
        countPictures = mEntry.getPictures().size();
        isCompletePicture = countPictures >= countAllEquipment;
        isCompleteEquip = countCheckedEquipment >= countAllEquipment;
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
                sbuf.append(ctx.getString(R.string.status_partial_install_equipments, countCheckedEquipment, countAllEquipment));
            }
            if (!isCompletePicture) {
                sbuf.append("\n");
                sbuf.append(ctx.getString(R.string.status_partial_install_pictures, countPictures, countAllEquipment));
            }
        }
        return sbuf.toString();
    }
}
