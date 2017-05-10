package com.cartlc.trackbattery.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/10/17.
 */

public class EquipmentCollection {
    public final long id;
    public List<Long> equipmentList = new ArrayList();

    public EquipmentCollection() {
        id = PrefHelper.getInstance().genNextEquipmentCollectionId();
        equipmentList = new ArrayList();
    }

    public EquipmentCollection(long collectionId) {
        this.id = collectionId;
    }

    public void add(long equipmentId) {
        equipmentList.add(equipmentId);
    }
}
