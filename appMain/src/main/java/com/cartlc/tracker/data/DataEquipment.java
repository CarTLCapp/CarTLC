package com.cartlc.tracker.data;

/**
 * Created by dug on 5/12/17.
 */

public class DataEquipment {
    public long id;
    public final String name;
    public long server_id;
    public boolean isChecked;
    public boolean isLocal;
    public boolean isTest;

    public DataEquipment(long id, String name, boolean isChecked, boolean isLocal) {
        this.id = id;
        this.name = name;
        this.isChecked = isChecked;
        this.isLocal = isLocal;
    }

    public DataEquipment(String name, int server_id) {
        this.name = name;
        this.server_id = server_id;
    }

    public String toString()
    {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("NAME=");
        sbuf.append(name);
        sbuf.append(", checked=");
        sbuf.append(isChecked);
        sbuf.append(", local=");
        sbuf.append(isLocal);
        sbuf.append(", test=");
        sbuf.append(isTest);
        return sbuf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataEquipment) {
            return equals((DataEquipment) obj);
        }
        return super.equals(obj);
    }

    public boolean equals(DataEquipment item) {
        return name.equals(item.name);
    }
}
