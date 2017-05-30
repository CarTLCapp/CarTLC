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
    public long                         truckNumber;
    public boolean                      uploaded;

    public DataEntry() {
    }

    public String getProjectName() {
        return TableProjects.getInstance().query(projectNameId);
    }

    public String getAddressText() {
        DataAddress address = TableAddress.getInstance().query(addressId);
        if (address != null) {
            return address.getBlock();
        }
        return null;
    }

    public List<DataNote> getNotes() {
        return TableCollectionNoteEntry.getInstance().query(id);
    }

    public List<String> getEquipmentNames() {
        if (equipmentCollection != null) {
            return equipmentCollection.getEquipmentNames();
        }
        return null;
    }

    public List<DataPicture> getPictures() {
        return pictureCollection.pictures;
    }

    public void saveNotes() {
        TableCollectionNoteEntry.getInstance().store(projectNameId, id);
    }
}
