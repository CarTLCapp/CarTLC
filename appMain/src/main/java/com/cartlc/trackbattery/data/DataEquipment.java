package com.cartlc.trackbattery.data;

import android.util.Log;

/**
 * Created by dug on 5/12/17.
 */

public class DataEquipment {
    public long id;
    public final String name;
    public final long projectId;
    public boolean isChecked;
    public boolean isLocal;

    public DataEquipment(long id, String name, long projectId, boolean isChecked, boolean isLocal) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.isChecked = isChecked;
        this.isLocal = isLocal;
    }

    public DataEquipment(String projectName, String name) {
        this.name = name;
        this.projectId = TableProjects.getInstance().query(projectName);
    }

    public String toString()
    {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("NAME=");
        sbuf.append(name);
        sbuf.append(", PROJ=");
        sbuf.append(TableProjects.getInstance().query(projectId));
        sbuf.append(", checked=");
        sbuf.append(isChecked);
        sbuf.append(", local=");
        sbuf.append(isLocal);
        return sbuf.toString();
    }
}
