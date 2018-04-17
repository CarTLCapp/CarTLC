/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.data;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */

public abstract class DataCollectionEquipment {
    public long id; // projectId or collectionId
    public List<Long> equipmentListIds = new ArrayList();

    public DataCollectionEquipment(long id) {
        this.id = id;
    }

    public void add(long equipmentId) {
        equipmentListIds.add(equipmentId);
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

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        boolean first = true;
        for (String name : getEquipmentNames()) {
            if (first) {
                first = false;
            } else {
                sbuf.append(",");
            }
            sbuf.append(name);
        }
        return sbuf.toString();
    }

}
