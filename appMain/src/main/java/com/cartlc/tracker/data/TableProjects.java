package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cartlc.tracker.app.TBApplication;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class TableProjects {

    static final String TABLE_NAME = "list_projects";

    static final String KEY_ROWID     = "_id";
    static final String KEY_NAME      = "name";
    static final String KEY_SERVER_ID = "server_id";
    static final String KEY_DISABLED  = "disabled";
    static final String KEY_IS_BOOT   = "is_boot_strap";

    final SQLiteDatabase mDb;

    static TableProjects sInstance;

    static void Init(SQLiteDatabase db) {
        new TableProjects(db);
    }

    public static TableProjects getInstance() {
        return sInstance;
    }

    TableProjects(SQLiteDatabase db) {
        this.mDb = db;
        sInstance = this;
    }

//    public void drop() {
//        mDb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_NAME);
        sbuf.append(" text not null, ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" integer, ");
        sbuf.append(KEY_DISABLED);
        sbuf.append(" bit default 0, ");
        sbuf.append(KEY_IS_BOOT);
        sbuf.append(" bit default 0)");
        mDb.execSQL(sbuf.toString());
    }

    public void clear() {
        try {
            mDb.delete(TABLE_NAME, null, null);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "clear()", "db");
        }
    }

    public void remove(String value) {
        try {
            String where = KEY_NAME + "=?";
            String[] whereArgs = {value};
            mDb.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "remove(value)", "db");
        }
    }

    public void remove(long id) {
        try {
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(id)};
            mDb.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "remove(id)", "db");
        }
    }

    public void add(List<String> list) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (String value : list) {
                values.clear();
                values.put(KEY_NAME, value);
                values.put(KEY_DISABLED, 0);
                mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "add()", "db");
        } finally {
            mDb.endTransaction();
        }
    }

    public long addTest(String item) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, item);
            values.put(KEY_IS_BOOT, 1);
            values.put(KEY_DISABLED, 0);
            id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "addTest()", "db");
        } finally {
            mDb.endTransaction();
        }
        return id;
    }


    public long add(String item, int server_id, boolean disabled) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, item);
            values.put(KEY_SERVER_ID, server_id);
            values.put(KEY_DISABLED, disabled ? 1 : 0);
            id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "add(item)", "db");
        } finally {
            mDb.endTransaction();
        }
        return id;
    }

    public long update(DataProject project) {
        long ret = -1;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, project.name);
            values.put(KEY_SERVER_ID, project.serverId);
            values.put(KEY_DISABLED, project.disabled ? 1 : 0);
            values.put(KEY_IS_BOOT, project.isBootStrap ? 1 : 0);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(project.id)};
            if (mDb.update(TABLE_NAME, values, where, whereArgs) == 0) {
                project.id = mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "update()", "db");
        } finally {
            mDb.endTransaction();
        }
        return ret;
    }

    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "count()", "db");
        }
        return count;
    }

    public List<String> query() {
        return query(false);
    }

    public List<String> query(boolean activeOnly) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_NAME, KEY_DISABLED};
            final String orderBy = KEY_NAME + " ASC";
            // Warning: do not use KEY_DISABLED=0 in selection because I failed to include "default 0"
            // for the column definition above on earlier versions. This means the value is actually NULL.
            Cursor cursor = mDb.query(TABLE_NAME, columns, null, null, null, null, orderBy);
            int idxValue = cursor.getColumnIndex(KEY_NAME);
            int idxDisabled = cursor.getColumnIndex(KEY_DISABLED);
            while (cursor.moveToNext()) {
                String name = cursor.getString(idxValue);
                boolean disabled = cursor.getShort(idxDisabled) == 1;
                if (!activeOnly || !disabled) {
                    list.add(name);
                }
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "query()", "db");
        }
        // Move other to bottom of the list.
        if (list.contains(TBApplication.OTHER)) {
            list.remove(TBApplication.OTHER);
            list.add(TBApplication.OTHER);
        }
        return list;
    }

    public String queryProjectName(long id) {
        String projectName = null;
        try {
            final String[] columns = {KEY_NAME};
            final String selection = KEY_ROWID + "=?";
            final String[] selectionArgs = {Long.toString(id)};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_NAME);
            if (cursor.moveToFirst()) {
                projectName = cursor.getString(idxValue);
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "queryProjectName()", "db");
        }
        return projectName;
    }

    public DataProject queryByServerId(int server_id) {
        final String selection = KEY_SERVER_ID + "=?";
        final String[] selectionArgs = {Integer.toString(server_id)};
        List<DataProject> list = query(selection, selectionArgs);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public DataProject queryById(long id) {
        final String selection = KEY_ROWID + "=?";
        final String[] selectionArgs = {Long.toString(id)};
        List<DataProject> list = query(selection, selectionArgs);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public boolean isDisabled(long id) {
        DataProject project = queryById(id);
        if (project == null) {
            return true;
        }
        return project.disabled;
    }

    public DataProject queryByName(String name) {
        final String selection = KEY_NAME + "=?";
        final String[] selectionArgs = {name};
        List<DataProject> list = query(selection, selectionArgs);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    List<DataProject> query(String selection, String[] selectionArgs) {
        List<DataProject> list = new ArrayList();
        try {
            Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_NAME);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            int idxDisabled = cursor.getColumnIndex(KEY_DISABLED);
            int idxTest = cursor.getColumnIndex(KEY_IS_BOOT);
            while (cursor.moveToNext()) {
                DataProject project = new DataProject();
                project.name = cursor.getString(idxValue);
                project.disabled = cursor.getShort(idxDisabled) != 0;
                project.isBootStrap = cursor.getShort(idxTest) != 0;
                project.serverId = cursor.getShort(idxServerId);
                project.id = cursor.getLong(idxRowId);
                list.add(project);
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "query()", "db");
        }
        return list;
    }

    public long queryProjectName(String name) {
        long rowId = -1L;
        try {
            final String[] columns = {KEY_ROWID};
            final String selection = KEY_NAME + "=?";
            final String[] selectionArgs = {name};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            int idxValue = cursor.getColumnIndex(KEY_ROWID);
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(idxValue);
            }
            cursor.close();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "queryProjectName()", name);
        }
        return rowId;
    }

    public void removeOrDisable(DataProject project) {
        if ((TableEntry.getInstance().countProjects(project.id) == 0) && (TableProjectAddressCombo.getInstance().countProjects(project.id) == 0)) {
            // No entries for this, so just remove.
            Timber.i("remove(" + project.id + ", " + project.name + ")");
            remove(project.id);
        } else {
            Timber.i("disable(" + project.id + ", " + project.name + ")");
            project.disabled = true;
            update(project);
        }
    }

    public void clearUploaded() {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_SERVER_ID, 0);
            if (mDb.update(TABLE_NAME, values, null, null) == 0) {
                Timber.e("TableProjects.clearUploaded(): Unable to update entries");
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableProjects.class, "clearUploaded()", "db");
        } finally {
            mDb.endTransaction();
        }
    }
}
