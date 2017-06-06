package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 5/10/17.
 */
public class TableAddress {

    class SelectionArgs {

        String   selection;
        String[] selectionArgs;

        SelectionArgs(String company, String street, String city, String state, String zipcode) {
            StringBuilder sbuf = new StringBuilder();
            ArrayList<String> args = new ArrayList();

            if (company != null && !TextUtils.isEmpty(company)) {
                sbuf.append(KEY_COMPANY);
                sbuf.append("=?");
                args.add(company);
            }
            if (zipcode != null && !TextUtils.isEmpty(zipcode)) {
                if (sbuf.length() > 0) {
                    sbuf.append(" AND ");
                }
                sbuf.append(KEY_ZIPCODE);
                sbuf.append("=?");
                args.add(zipcode);
            }
            if (state != null && !TextUtils.isEmpty(state)) {
                if (sbuf.length() > 0) {
                    sbuf.append(" AND ");
                }
                sbuf.append(KEY_STATE);
                sbuf.append("=?");
                args.add(state);
            }
            if (city != null && !TextUtils.isEmpty(city)) {
                if (sbuf.length() > 0) {
                    sbuf.append(" AND ");
                }
                sbuf.append(KEY_CITY);
                sbuf.append("=?");
                args.add(city);
            }
            if (street != null && !TextUtils.isEmpty(street)) {
                if (sbuf.length() > 0) {
                    sbuf.append(" AND ");
                }
                sbuf.append(KEY_STREET);
                sbuf.append("=?");
                args.add(street);
            }
            if (sbuf.length() > 0) {
                selection = sbuf.toString();
                selectionArgs = args.toArray(new String[args.size()]);
            } else {
                selection = null;
                selectionArgs = null;
            }
        }
    }

    static TableAddress sInstance;

    static void Init(SQLiteDatabase db) {
        new TableAddress(db);
    }

    public static TableAddress getInstance() {
        return sInstance;
    }

    static final String TABLE_NAME = "list_address";

    static final String KEY_ROWID     = "_id";
    static final String KEY_COMPANY   = "company";
    static final String KEY_STREET    = "street";
    static final String KEY_CITY      = "city";
    static final String KEY_STATE     = "state";
    static final String KEY_ZIPCODE   = "zipcode";
    static final String KEY_SERVER_ID = "server_id";
    static final String KEY_DISABLED  = "disabled";
    static final String KEY_LOCAL     = "local";
    static final String KEY_IS_TEST   = "is_test";

    final SQLiteDatabase mDb;

    public TableAddress(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void clear() {
        try {
            mDb.delete(TABLE_NAME, null, null);
        } catch (Exception ex) {
        }
    }

    public void drop() {
        mDb.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_COMPANY);
        sbuf.append(" text, ");
        sbuf.append(KEY_STREET);
        sbuf.append(" text, ");
        sbuf.append(KEY_CITY);
        sbuf.append(" text, ");
        sbuf.append(KEY_STATE);
        sbuf.append(" text, ");
        sbuf.append(KEY_ZIPCODE);
        sbuf.append(" text, ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" int, ");
        sbuf.append(KEY_DISABLED);
        sbuf.append(" bit, ");
        sbuf.append(KEY_LOCAL);
        sbuf.append(" bit, ");
        sbuf.append(KEY_IS_TEST);
        sbuf.append(" bit)");
        mDb.execSQL(sbuf.toString());
    }

    public void add(List<DataAddress> list) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (DataAddress address : list) {
                values.clear();
                values.put(KEY_COMPANY, address.company);
                values.put(KEY_STREET, address.street);
                values.put(KEY_CITY, address.city);
                values.put(KEY_STATE, address.state);
                values.put(KEY_ZIPCODE, address.zipcode);
                values.put(KEY_SERVER_ID, address.server_id);
                values.put(KEY_DISABLED, address.disabled ? 1 : 0);
                values.put(KEY_LOCAL, address.isLocal ? 1 : 0);
                values.put(KEY_IS_TEST, address.isTest ? 1 : 0);
                mDb.insert(TABLE_NAME, null, values);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public long add(DataAddress address) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_COMPANY, address.company);
            values.put(KEY_STREET, address.street);
            values.put(KEY_CITY, address.city);
            values.put(KEY_STATE, address.state);
            values.put(KEY_ZIPCODE, address.zipcode);
            values.put(KEY_SERVER_ID, address.server_id);
            values.put(KEY_DISABLED, address.disabled ? 1 : 0);
            values.put(KEY_LOCAL, address.isLocal ? 1 : 0);
            values.put(KEY_IS_TEST, address.isTest ? 1 : 0);
            id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return id;
    }

    public long add(String company) {
        long id = -1L;
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_COMPANY, company);
            values.put(KEY_LOCAL, 1);
            id = mDb.insert(TABLE_NAME, null, values);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
        return id;
    }

    public void update(DataAddress address) {
        mDb.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(KEY_COMPANY, address.company);
            values.put(KEY_STREET, address.street);
            values.put(KEY_CITY, address.city);
            values.put(KEY_STATE, address.state);
            values.put(KEY_ZIPCODE, address.zipcode);
            values.put(KEY_SERVER_ID, address.server_id);
            values.put(KEY_DISABLED, address.disabled ? 1 : 0);
            values.put(KEY_LOCAL, address.isLocal ? 1 : 0);
            values.put(KEY_IS_TEST, address.isTest ? 1 : 0);
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(address.id)};
            mDb.update(TABLE_NAME, values, where, whereArgs);
            mDb.setTransactionSuccessful();
        } catch (Exception ex) {
            Timber.e(ex);
        } finally {
            mDb.endTransaction();
        }
    }

    public int count() {
        int count = 0;
        try {
            Cursor cursor = mDb.query(true, TABLE_NAME, null, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return count;
    }

    public boolean isLocalCompanyOnly(String company) {
        List<DataAddress> list = TableAddress.getInstance().queryByCompanyName(company);
        if (list.size() == 0) {
            return false;
        }
        for (DataAddress address : list) {
            if (!address.isLocal) {
                return false;
            }
        }
        return true;
    }

    public DataAddress query(long id) {
        final String orderBy = KEY_COMPANY + " ASC";
        final String selection = KEY_ROWID + " =?";
        final String[] selectionArgs = {Long.toString(id)};
        List<DataAddress> list = query(selection, selectionArgs, orderBy);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<DataAddress> queryByCompanyName(String name) {
        final String selection = KEY_COMPANY + " =?";
        final String[] selectionArgs = {name};
        return query(selection, selectionArgs, null);
    }

    public List<DataAddress> query() {
        final String orderBy = KEY_COMPANY + " ASC";
        return query(null, null, orderBy);
    }

    public DataAddress queryByServerId(int serverId) {
        final String selection = KEY_SERVER_ID + "=?";
        final String[] selectionArgs = {Integer.toString(serverId)};
        List<DataAddress> list = query(selection, selectionArgs, null);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    List<DataAddress> query(String selection, String[] selectionArgs, String orderBy) {
        ArrayList<DataAddress> list = new ArrayList();
        try {
            final String[] columns = {KEY_ROWID, KEY_COMPANY, KEY_STATE, KEY_CITY, KEY_STREET, KEY_ZIPCODE, KEY_SERVER_ID, KEY_DISABLED, KEY_LOCAL, KEY_IS_TEST};
            Cursor cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy, null);
            int idxCompany = cursor.getColumnIndex(KEY_COMPANY);
            int idxState = cursor.getColumnIndex(KEY_STATE);
            int idxCity = cursor.getColumnIndex(KEY_CITY);
            int idxStreet = cursor.getColumnIndex(KEY_STREET);
            int idxZipCode = cursor.getColumnIndex(KEY_ZIPCODE);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            int idxDisabled = cursor.getColumnIndex(KEY_DISABLED);
            int idxLocal = cursor.getColumnIndex(KEY_LOCAL);
            int idxTest = cursor.getColumnIndex(KEY_IS_TEST);
            while (cursor.moveToNext()) {
                DataAddress address;
                list.add(address = new DataAddress(
                        cursor.getLong(idxRowId),
                        cursor.getInt(idxServerId),
                        cursor.getString(idxCompany),
                        cursor.getString(idxStreet),
                        cursor.getString(idxCity),
                        cursor.getString(idxState),
                        cursor.getString(idxZipCode)));
                address.disabled = cursor.getShort(idxDisabled) == 1;
                address.isLocal = cursor.getShort(idxLocal) == 1;
                address.isTest = cursor.getShort(idxTest) == 1;
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryZipCodes(String company) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_ZIPCODE};
            final String orderBy = KEY_ZIPCODE + " ASC";
            SelectionArgs args = new SelectionArgs(company, null, null, null, null);
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, args.selection, args.selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_ZIPCODE);
            String zipcode;
            while (cursor.moveToNext()) {
                zipcode = cursor.getString(idxValue);
                if (!TextUtils.isEmpty(zipcode)) {
                    list.add(zipcode);
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryStates(String company, String zipcode) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_STATE};
            final String orderBy = KEY_STATE + " ASC";
            SelectionArgs args = new SelectionArgs(company, null, null, null, zipcode);
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, args.selection, args.selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_STATE);
            String state;
            String zip;
            while (cursor.moveToNext()) {
                state = cursor.getString(idxValue);
                if (state != null) {
                    list.add(state);
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryCities(String company, String zipcode, String state) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_CITY};
            final String orderBy = KEY_CITY + " ASC";
            SelectionArgs args = new SelectionArgs(company, null, null, state, zipcode);
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, args.selection, args.selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_CITY);
            while (cursor.moveToNext()) {
                String city = cursor.getString(idxValue);
                if (city != null) {
                    list.add(city);
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryStreets(String company, String city, String state, String zipcode) {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_STREET};
            final String orderBy = KEY_STREET + " ASC";
            SelectionArgs args = new SelectionArgs(company, null, city, state, zipcode);
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, args.selection, args.selectionArgs, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_STREET);
            while (cursor.moveToNext()) {
                String street = cursor.getString(idxValue);
                if (street != null) {
                    list.add(street);
                }
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }

    public List<String> queryCompanies() {
        ArrayList<String> list = new ArrayList();
        try {
            final String[] columns = {KEY_COMPANY};
            final String orderBy = KEY_COMPANY + " ASC";
            Cursor cursor = mDb.query(true, TABLE_NAME, columns, null, null, null, null, orderBy, null);
            int idxValue = cursor.getColumnIndex(KEY_COMPANY);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(idxValue));
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return list;
    }


    public long queryAddressId(String company, String street, String city, String state, String zipcode) {
        long id = -1L;
        try {
            final String[] columns = {KEY_ROWID};
            SelectionArgs args = new SelectionArgs(company, street, city, state, zipcode);
            Cursor cursor = mDb.query(TABLE_NAME, columns, args.selection, args.selectionArgs, null, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            if (cursor.moveToFirst()) {
                id = cursor.getLong(idxRowId);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return id;
    }

    public long queryAddressId(String company, String zipcode) {
        long id = -1L;
        try {
            final String[] columns = {KEY_ROWID};
            SelectionArgs args = new SelectionArgs(company, null, null, null, zipcode);
            Cursor cursor = mDb.query(TABLE_NAME, columns, args.selection, args.selectionArgs, null, null, null, null);
            int idxRowId = cursor.getColumnIndex(KEY_ROWID);
            if (cursor.moveToFirst()) {
                id = cursor.getLong(idxRowId);
            }
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return id;
    }

    public boolean hasCompanyName(String company) {
        boolean has = false;
        try {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(KEY_COMPANY);
            sbuf.append(" =?");
            final String selection = sbuf.toString();
            final String[] selectionArgs = {company};
            Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            has = cursor.getCount() > 0;
            cursor.close();
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return has;
    }

    public void remove(long id) {
        try {
            String where = KEY_ROWID + "=?";
            String[] whereArgs = {Long.toString(id)};
            mDb.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    public void removeOrDisable(DataAddress item) {
        if (TableEntry.getInstance().countAddresses(item.id) == 0) {
            // No entries for this, so just remove.
            remove(item.id);
        } else {
            item.disabled = true;
            update(item);
        }
    }

    public void removeTest() {
        try {
            String where = KEY_IS_TEST + "=1";
            mDb.delete(TABLE_NAME, where, null);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }
}
