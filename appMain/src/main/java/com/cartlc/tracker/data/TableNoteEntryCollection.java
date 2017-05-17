package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/16/17.
 */

public class TableNoteEntryCollection {

    static final String TABLE_NAME = "note_entry_collection";

    static final String KEY_ROWID    = "_id";
    static final String KEY_ENTRY_ID = "entry_id";
    static final String KEY_NOTE_ID  = "note_id";
    static final String KEY_VALUE    = "value";

    static TableNoteEntryCollection sInstance;

    static void Init(SQLiteDatabase db) {
        new TableNoteEntryCollection(db);
    }

    public static TableNoteEntryCollection getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    public TableNoteEntryCollection(SQLiteDatabase db) {
        mDb = db;
        sInstance = this;
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_ENTRY_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_NOTE_ID);
        sbuf.append(" long, ");
        sbuf.append(KEY_VALUE);
        sbuf.append(" text)");

        mDb.execSQL(sbuf.toString());
    }

    public void store(long projectNameId, long entryId) {
        List<DataNote> notes = TableNoteProjectCollection.getInstance().getNotes(projectNameId);
        mDb.beginTransaction();
        try {
            String where = KEY_ENTRY_ID + "=?";
            String[] whereArgs = new String[]{Long.toString(entryId)};
            mDb.delete(TABLE_NAME, where, whereArgs);
            ContentValues values = new ContentValues();
            for (DataNote note : notes) {
                if (!TextUtils.isEmpty(note.value)) {
                    values.clear();
                    values.put(KEY_ENTRY_ID, entryId);
                    values.put(KEY_NOTE_ID, note.id);
                    values.put(KEY_VALUE, note.value);
                    mDb.insert(TABLE_NAME, null, values);
                }
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public List<DataNote> query(long entryId) {
        List<DataNote> list = new ArrayList();
        try {
            final String[] columns = {KEY_ROWID, KEY_NOTE_ID, KEY_VALUE};
            final String selection = KEY_ENTRY_ID + " =?";
            final String[] selectionArgs = {Long.toString(entryId)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxNoteId = cursor.getColumnIndex(KEY_NOTE_ID);
            int idxValueId = cursor.getColumnIndex(KEY_VALUE);
            DataNote note;
            while (cursor.moveToNext()) {
                note = new DataNote();
                note.id = cursor.getLong(idxRowId);
                note.value = cursor.getString(idxValueId);
                note.name = TableNote.getInstance().getName(cursor.getLong(idxNoteId));
                list.add(note);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        Log.d("MYDEBUG", "QUERIED " + list.size());
        return list;
    }

    public void deleteNotes(long entryId) {
        try {
            String where = KEY_ENTRY_ID + "=?";
            String[] whereArgs = new String[]{Long.toString(entryId)};
            mDb.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

}
