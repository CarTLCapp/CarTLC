package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by dug on 5/16/17.
 */
public class TableEquipmentProjectCollection extends TableEquipmentCollection {
    static final String TABLE_NAME = "project_equipment_collection";

    static TableEquipmentProjectCollection sInstance;

    static void Init(SQLiteDatabase db) {
        new TableEquipmentProjectCollection(db);
    }

    public static TableEquipmentProjectCollection getInstance() {
        return sInstance;
    }

    public TableEquipmentProjectCollection(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }

    public DataEquipmentProjectCollection queryForProject(long projectId) {
        DataEquipmentProjectCollection collection = new DataEquipmentProjectCollection(projectId);
        collection.equipmentListIds = query(projectId);
        return collection;
    }

    public void addByName(String projectName, List<String> equipments) {
        long projectId = TableProjects.getInstance().query(projectName);
        if (projectId < 0) {
            projectId = TableProjects.getInstance().add(projectName);
        }
        super.addByName(projectId, equipments);
    }

}
