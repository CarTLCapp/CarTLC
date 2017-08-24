package com.cartlc.tracker.data;

import com.cartlc.tracker.event.EventPingDone;

import java.util.List;

import de.greenrobot.event.EventBus;

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
    public long                         truckNumber;
    public String                       licensePlateNumber;
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

    public String getAddressText() {
        DataAddress address = getAddress();
        if (address != null) {
            return address.getBlock();
        }
        return null;
    }

    public List<DataNote> getNotes() {
        return TableCollectionNoteEntry.getInstance().query(noteCollectionId);
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
        TableCollectionNoteEntry.getInstance().store(projectAddressCombo.projectNameId, noteCollectionId);
    }

    public void checkPictureUploadComplete() {
        for (DataPicture item : pictureCollection.pictures) {
            if (!item.uploaded) {
                return;
            }
        }
        TableEntry.getInstance().setUploadedAws(this, true);
        EventBus.getDefault().post(new EventPingDone());
    }
}
