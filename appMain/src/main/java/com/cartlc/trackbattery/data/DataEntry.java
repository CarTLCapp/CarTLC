package com.cartlc.trackbattery.data;

import java.util.List;

/**
 * Created by dug on 5/13/17.
 */

public class DataEntry {
    public long id;
    public long date;
    public long projectNameId;
    public long addressId;
    public DataEquipmentCollection equipmentCollection;
    public long pictureCollectionId;
    public long truckNumber;
    public long notesId;
    public boolean uploaded;

    public DataEntry() {
    }

    public String getProjectName() {
        return TableProjects.getInstance().query(projectNameId);
    }

    public String getAddressLine() {
        DataAddress address = TableAddress.getInstance().query(addressId);
        if (address != null) {
            return address.getLine();
        }
        return null;
    }

    public String getNotes() {
        return TableNotes.getInstance().query(notesId);
    }

    public List<String> getEquipmentNames() {
        if (equipmentCollection != null) {
            return equipmentCollection.getEquipmentNames();
        }
        return null;
    }
}
