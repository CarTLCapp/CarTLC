package com.cartlc.tracker.data;

/**
 * Created by dug on 5/12/17.
 */

public class DataEquipment {
    public long id;
    public final String name;
    public boolean isLocal;

    public DataEquipment(long id, String name, boolean isLocal) {
        this.id = id;
        this.name = name;
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
        sbuf.append(", local=");
        sbuf.append(isLocal);
        return sbuf.toString();
    }

    public boolean isChecked() {
        return PrefHelper.getInstance().getEquipmentId() == id;
    }

    public void setChecked() {
        PrefHelper.getInstance().setEquipmentId(id);
    }
}
