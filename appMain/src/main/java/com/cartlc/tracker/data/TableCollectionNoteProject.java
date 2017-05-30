package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/16/17.
 */

public class TableCollectionNoteProject extends TableCollection {

    static final String TABLE_NAME = "note_project_collection";

    static TableCollectionNoteProject sInstance;

    static void Init(SQLiteDatabase db) {
        new TableCollectionNoteProject(db);
    }

    public static TableCollectionNoteProject getInstance() {
        return sInstance;
    }

    public TableCollectionNoteProject(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }

    public void addByName(String projectName, List<DataNote> notes) {
        long projectNameId = TableProjects.getInstance().query(projectName);
        if (projectNameId < 0) {
            projectNameId = TableProjects.getInstance().add(projectName);
        }
        addByName(projectNameId, notes);
    }

    void addByName(long projectNameId, List<DataNote> notes) {
        List<Long> list = new ArrayList();
        for (DataNote note : notes) {
            long id = TableNote.getInstance().query(note.name);
            if (id < 0) {
                id = TableNote.getInstance().add(note);
            }
            list.add(id);
        }
        add(projectNameId, list);
    }

    public List<DataNote> getNotes(long projectNameId) {
        List<Long> noteIds = query(projectNameId);
        List<DataNote> list = new ArrayList();
        for (Long noteId : noteIds) {
            DataNote note = TableNote.getInstance().query(noteId);
            if (note == null) {
                Timber.e("Could not find note with ID " + noteId);
            } else {
                list.add(note);
            }
        }
        return list;
    }
}
