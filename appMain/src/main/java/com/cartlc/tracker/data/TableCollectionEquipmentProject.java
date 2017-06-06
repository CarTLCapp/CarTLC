package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/16/17.
 */
public class TableCollectionEquipmentProject extends TableCollection {
    static final String TABLE_NAME = "project_equipment_collection";

    static TableCollectionEquipmentProject sInstance;

    static void Init(SQLiteDatabase db) {
        new TableCollectionEquipmentProject(db);
    }

    public static TableCollectionEquipmentProject getInstance() {
        return sInstance;
    }

    public TableCollectionEquipmentProject(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }

    public DataCollectionEquipmentProject queryForProject(long projectNameId) {
        DataCollectionEquipmentProject collection = new DataCollectionEquipmentProject(projectNameId);
        collection.equipmentListIds = query(projectNameId);
        return collection;
    }

    public void addByName(String projectName, List<String> equipments) {
        long projectNameId = TableProjects.getInstance().queryProjectName(projectName);
        if (projectNameId < 0) {
            projectNameId = TableProjects.getInstance().addTest(projectName);
        }
        addByNameTest(projectNameId, equipments);
    }

    public void addByNameTest(long collectionId, List<String> names) {
        List<Long> list = new ArrayList();
        for (String name : names) {
            long id = TableEquipment.getInstance().query(name);
            if (id < 0) {
                id = TableEquipment.getInstance().addTest(name);
            }
            list.add(id);
        }
        addTest(collectionId, list);
    }

    public void addLocal(String name, long projectNameId) {
        long equipId = TableEquipment.getInstance().addLocal(name);
        add(projectNameId, equipId);
    }

}
