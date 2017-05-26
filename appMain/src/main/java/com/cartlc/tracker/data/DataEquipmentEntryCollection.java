package com.cartlc.tracker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/10/17.
 */

public class DataEquipmentEntryCollection extends DataEquipmentCollection {

    public DataEquipmentEntryCollection(long collectionId) {
        super(collectionId);
    }

    public void addChecked() {
        equipmentListIds = TableEquipment.getInstance().queryChecked();
    }
}
