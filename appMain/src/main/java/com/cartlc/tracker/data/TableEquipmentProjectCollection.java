package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/16/17.
 */
public class TableEquipmentProjectCollection extends TableCollection {
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
        addByName(projectId, equipments);
    }

    public void addByName(long collectionId, List<String> names) {
        List<Long> list = new ArrayList();
        for (String name : names) {
            long id = TableEquipment.getInstance().query(name);
            if (id < 0) {
                id = TableEquipment.getInstance().add(name);
            }
            list.add(id);
        }
        add(collectionId, list);
    }

}
