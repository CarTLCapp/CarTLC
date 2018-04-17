/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.data;

import android.support.annotation.NonNull;

/**
 * Created by dug on 5/12/17.
 */

public class DataEquipment implements Comparable<DataEquipment> {
    public       long    id;
    public final String  name;
    public       long    serverId;
    public       boolean isChecked;
    public       boolean isLocal;
    public       boolean isBootStrap;
    public       boolean disabled;

    public DataEquipment(long id, String name, boolean isChecked, boolean isLocal) {
        this.id = id;
        this.name = name;
        this.isChecked = isChecked;
        this.isLocal = isLocal;
    }

    public DataEquipment(String name, int server_id) {
        this.name = name;
        this.serverId = server_id;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("NAME=");
        sbuf.append(name);
        sbuf.append(", checked=");
        sbuf.append(isChecked);
        sbuf.append(", local=");
        sbuf.append(isLocal);
        sbuf.append(", test=");
        sbuf.append(isBootStrap);
        return sbuf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataEquipment) {
            return equals((DataEquipment) obj);
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(@NonNull DataEquipment o) {
        return name.compareTo(o.name);
    }

    public boolean equals(DataEquipment item) {
        return name.equals(item.name);
    }
}
