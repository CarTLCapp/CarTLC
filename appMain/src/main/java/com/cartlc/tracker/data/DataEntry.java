package com.cartlc.tracker.data;

import java.util.List;

/**
 * Created by dug on 5/13/17.
 */

public class DataEntry {
    public long                         id;
    public long                         date;
    public long                         projectNameId;
    public long                         addressId;
    public DataCollectionEquipmentEntry equipmentCollection;
    public DataPictureCollection        pictureCollection;
    public long                         noteCollectionId;
    public long                         truckNumber;
    public boolean                      uploaded;

    public DataEntry() {
    }

    public String getProjectName() {
        return TableProjects.getInstance().queryProjectName(projectNameId);
    }

    public DataProject getProject() {
        return TableProjects.getInstance().queryById(projectNameId);
    }

    public DataAddress getAddress() {
        return TableAddress.getInstance().query(addressId);
    }

    public String getAddressText() {
        DataAddress address = TableAddress.getInstance().query(addressId);
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
        TableCollectionNoteEntry.getInstance().store(projectNameId, noteCollectionId);
    }
}
