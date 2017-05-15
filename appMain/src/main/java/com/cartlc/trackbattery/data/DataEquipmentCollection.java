package com.cartlc.trackbattery.data;

import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/10/17.
 */

public class DataEquipmentCollection {
    public final long id;
    public long projectNameId;
    public List<Long> equipmentListIds = new ArrayList();

    public DataEquipmentCollection(long id, long projectId) {
        this.id = id;
        this.projectNameId = projectId;
    }

    public DataEquipmentCollection(long id) {
        this.id = id;
        this.projectNameId = -1L;
    }

    public void add(long equipmentId) {
        equipmentListIds.add(equipmentId);
    }

    public void addChecked() {
        equipmentListIds = TableEquipment.getInstance().queryChecked(projectNameId);
    }

    public List<DataEquipment> getEquipment() {
        ArrayList<DataEquipment> list = new ArrayList();
        for (long e : equipmentListIds) {
            list.add(TableEquipment.getInstance().query(e));
        }
        return list;
    }

    public List<String> getEquipmentNames() {
        List<DataEquipment> equipments = getEquipment();
        if (equipments != null) {
            ArrayList<String> list = new ArrayList();
            for (DataEquipment e : equipments) {
                list.add(e.name);
            }
            return list;
        }
        return null;
    }

    public long getProjectID() {
        if (projectNameId >= 0) {
            if (equipmentListIds.size() > 0) {
                DataEquipment eq = TableEquipment.getInstance().query(equipmentListIds.get(0));
                projectNameId =  eq.projectId;
            }
        }
        return projectNameId;
    }
}
