package com.cartlc.tracker.data;

/**
 * Created by dug on 5/12/17.
 */

public class DataEquipment {
    public long id;
    public final String name;
    public boolean isChecked;
    public boolean isLocal;

    public DataEquipment(long id, String name, boolean isChecked, boolean isLocal) {
        this.id = id;
        this.name = name;
        this.isChecked = isChecked;
        this.isLocal = isLocal;
    }

    public DataEquipment(String name) {
        this.name = name;
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
        return sbuf.toString();
    }
}