package com.cartlc.tracker.data;

import android.text.TextUtils;

import com.cartlc.tracker.etc.EntryStatus;
import com.cartlc.tracker.etc.TruckStatus;
import com.cartlc.tracker.event.EventPingDone;

import java.util.List;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * Created by dug on 5/13/17.
 */

public class DataEntry {
    public long                         id;
    public long                         date;
    public DataProjectAddressCombo      projectAddressCombo;
    public DataCollectionEquipmentEntry equipmentCollection;
    public DataPictureCollection        pictureCollection;
    public long                         noteCollectionId;
    public long                         truckId;
    public TruckStatus                  status;
    public int                          serverId;
    public boolean                      uploadedMaster;
    public boolean                      uploadedAws;

    public DataEntry() {
    }

    public String getProjectName() {
        return projectAddressCombo.getProjectName();
    }

    public DataProject getProject() {
        return projectAddressCombo.getProject();
    }

    public DataAddress getAddress() {
        return projectAddressCombo.getAddress();
    }

    public String getAddressBlock() {
        DataAddress address = getAddress();
        if (address != null) {
            return address.getBlock();
        }
        return null;
    }

    public String getAddressLine() {
        DataAddress address = getAddress();
        if (address != null) {
            return address.getLine();
        }
        return "Invalid";
    }

    public List<DataNote> getNotes() {
        return TableCollectionNoteEntry.getInstance().query(noteCollectionId);
    }

    public String getNotesLine() {
        StringBuilder sbuf = new StringBuilder();
        for (DataNote note : getNotes()) {
            if (!TextUtils.isEmpty(note.value)) {
                if (sbuf.length() > 0) {
                    sbuf.append(", ");
                }
                sbuf.append(note.value);
            }
        }
        return sbuf.toString();
    }

    public List<String> getEquipmentNames() {
        if (equipmentCollection != null) {
            return equipmentCollection.getEquipmentNames();
        }
        return null;
    }

    public List<DataEquipment> getEquipment() {
        if (equipmentCollection != null) {
            return equipmentCollection.getEquipment();
        }
        return null;
    }

    public List<DataPicture> getPictures() {
        return pictureCollection.pictures;
    }

    public void saveNotes(long collectionId) {
        noteCollectionId = collectionId;
        TableCollectionNoteEntry.getInstance().save(projectAddressCombo.projectNameId, noteCollectionId);
    }

    public void saveNotes() {
        TableCollectionNoteEntry.getInstance().save(projectAddressCombo.projectNameId, noteCollectionId);
    }

    public void checkPictureUploadComplete() {
        for (DataPicture item : pictureCollection.pictures) {
            if (!item.uploaded) {
                return;
            }
        }
        uploadedAws = true;
        TableEntry.getInstance().save(this);
        EventBus.getDefault().post(new EventPingDone());
    }

    public DataTruck getTruck() {
        return TableTruck.getInstance().query(truckId);
    }

    public EntryStatus computeStatus() {
        return new EntryStatus(this);
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("ID=");
        sbuf.append(id);
        sbuf.append(", ADDRESS=");
        sbuf.append(projectAddressCombo == null ? "NULL" : projectAddressCombo.id);
        sbuf.append(", EQUIPID=");
        sbuf.append(equipmentCollection == null ? "NULL" : equipmentCollection.id);
        sbuf.append(", NOTEID=");
        sbuf.append(noteCollectionId);
        sbuf.append(", TRUCKID=");
        sbuf.append(truckId);
        sbuf.append(", SERVERID=");
        sbuf.append(serverId);
        return sbuf.toString();
    }
}
