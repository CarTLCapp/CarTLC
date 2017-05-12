package com.cartlc.trackbattery.data;

/**
 * Created by dug on 5/12/17.
 */

public class DataEquipment {
    public final String name;
    public final long projectId;

    public DataEquipment(String name, long projectId) {
        this.name = name;
        this.projectId = projectId;
    }

    public DataEquipment(String projectName, String name) {
        this.name = name;
        this.projectId = TableProjects.getInstance().query(projectName);
    }
}
