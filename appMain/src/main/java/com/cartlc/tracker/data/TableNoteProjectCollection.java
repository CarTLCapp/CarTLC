package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/16/17.
 */

public class TableNoteProjectCollection extends TableCollection {
    static final String TABLE_NAME = "note_project_collection";
    static TableNoteProjectCollection sInstance;

    static void Init(SQLiteDatabase db) {
        new TableNoteProjectCollection(db);
    }

    public static TableNoteProjectCollection getInstance() {
        return sInstance;
    }

    public TableNoteProjectCollection(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }

    public void addByName(String projectName, List<String> notes) {
        long projectId = TableProjects.getInstance().query(projectName);
        if (projectId < 0) {
            projectId = TableProjects.getInstance().add(projectName);
        }
        addByName(projectId, notes);
    }

    public void addByName(long collectionId, List<String> names) {
        List<Long> list = new ArrayList();
        for (String name : names) {
            long id = TableNote.getInstance().query(name);
            if (id < 0) {
                id = TableNote.getInstance().add(name);
            }
            list.add(id);
        }
        add(collectionId, list);
    }
}
