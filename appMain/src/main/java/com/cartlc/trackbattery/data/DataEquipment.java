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

    public DataEquipment(long id, String name, long projectId, boolean isChecked) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.isChecked = isChecked;
    }

    public DataEquipment(String projectName, String name) {
        this.name = name;
        this.projectId = TableProjects.getInstance().query(projectName);
        if (projectId < 0) {
            Log.d("MYDEBUG", "ProjectId=" + projectId + " from " + projectName);
            for (String n : TableProjects.getInstance().query()) {
                Log.d("MYDEBUG", "NAME=" + n);
            }
        }
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
        return sbuf.toString();
    }
}
