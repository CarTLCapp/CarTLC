package com.cartlc.trackbattery.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/10/17.
 */

public class DataEquipmentCollection {
    public final long id;
    public List<Long> equipmentList = new ArrayList();

    public DataEquipmentCollection(long projectId) {
        id = PrefHelper.getInstance().genNextEquipmentCollectionId();
        equipmentList = TableEquipment.getInstance().queryChecked(projectId);
    }

    public void add(long equipmentId) {
        equipmentList.add(equipmentId);
    }
}
