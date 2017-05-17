package com.cartlc.tracker.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dug on 5/16/17.
 */

public class TableNoteEntryCollection extends TableNote {

    static final String TABLE_NAME = "note_entry_collection";

    static TableNoteEntryCollection sInstance;

    static void Init(SQLiteDatabase db) {
        new TableNoteEntryCollection(db);
    }

    public static TableNoteEntryCollection getInstance() {
        return sInstance;
    }

    public TableNoteEntryCollection(SQLiteDatabase db) {
        super(db, TABLE_NAME);
        sInstance = this;
    }

}
