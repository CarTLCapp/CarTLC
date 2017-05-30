package com.cartlc.tracker.data;

/**
 * Created by dug on 5/10/17.
 */

public class DataCollectionEquipmentEntry extends DataCollectionEquipment {

    public DataCollectionEquipmentEntry(long collectionId) {
        super(collectionId);
    }

    public void addChecked() {
        equipmentListIds = TableEquipment.getInstance().queryChecked();
    }
}
