package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 8/31/17.
 */

public class TableTruck {

    static final String TABLE_NAME = "table_trucks";

    static final String KEY_ROWID         = "_id";
    static final String KEY_TRUCK_NUMBER  = "truck_number";
    static final String KEY_LICENSE_PLATE = "license_plate";
    static final String KEY_SERVER_ID     = "server_id";

    static TableTruck sInstance;

    static void Init(SQLiteDatabase db) {
        new TableTruck(db);
    }

    public static TableTruck getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    TableTruck(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void create() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_TRUCK_NUMBER);
        sbuf.append(" int default 0, ");
        sbuf.append(KEY_LICENSE_PLATE);
        sbuf.append(" varchar(128), ");
        sbuf.append(KEY_SERVER_ID);
        sbuf.append(" integer)");
        mDb.execSQL(sbuf.toString());
    }

    // Will ensure the said truck and license plate are stored in the database.
    // Trys to take care of existing entries smartly. Prefers license plate entry
    // to truck number entries in case of duplication.
    // @Returns id of newly saved truck.
    public long save(long truckNumber, String licensePlate) {
        String selection;
        String[] selectionArgs;
        Cursor cursor;
        ContentValues values = new ContentValues();
        DataTruck truck = new DataTruck();
        if (!TextUtils.isEmpty(licensePlate)) {
            selection = KEY_LICENSE_PLATE + "=?";
            selectionArgs = new String[]{licensePlate};
            cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
            if (cursor.moveToNext()) {
                int idxId = cursor.getColumnIndex(KEY_ROWID);
                int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
                int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
                truck.truckNumber = cursor.getInt(idxTruckNumber);
                truck.licensePlateNumber = cursor.getString(idxLicensePlate);
                truck.id = cursor.getLong(idxId);
                if ((truckNumber != truck.truckNumber) && truckNumber > 0) {
                    String where = KEY_ROWID + "=?";
                    String[] whereArgs = new String[]{Long.toString(truck.id)};
                    values.put(KEY_TRUCK_NUMBER, truckNumber);
                    mDb.update(TABLE_NAME, values, where, whereArgs);
                }
            } else {
                if (truckNumber > 0) {
                    cursor.close();
                    selection = KEY_TRUCK_NUMBER + "=?";
                    selectionArgs = new String[]{Long.toString(truckNumber)};
                    String[] columns = {KEY_ROWID};
                    cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
                    if (cursor.moveToNext()) {
                        int idxId = cursor.getColumnIndex(KEY_ROWID);
                        truck.id = cursor.getLong(idxId);
                        String where = KEY_ROWID + "=?";
                        String[] whereArgs = new String[]{Long.toString(truck.id)};
                        values.put(KEY_LICENSE_PLATE, licensePlate);
                        mDb.update(TABLE_NAME, values, where, whereArgs);
                    } else {
                        values.put(KEY_TRUCK_NUMBER, truckNumber);
                        values.put(KEY_LICENSE_PLATE, licensePlate);
                        truck.id = mDb.insert(TABLE_NAME, null, values);
                    }
                } else {
                    values.put(KEY_LICENSE_PLATE, licensePlate);
                    truck.id = mDb.insert(TABLE_NAME, null, values);
                }
            }
        } else {
            if (truckNumber == 0) {
                Timber.e("Invalid truck entry ignored");
                return 0;
            }
            String[] columns = {KEY_ROWID};
            selection = KEY_TRUCK_NUMBER + "=?";
            selectionArgs = new String[]{Long.toString(truckNumber)};
            cursor = mDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
            if (cursor.getCount() == 0) {
                values.put(KEY_TRUCK_NUMBER, truckNumber);
                truck.id = mDb.insert(TABLE_NAME, null, values);
            } else {
                truck.id = cursor.getLong(cursor.getColumnIndex(KEY_ROWID));
            }
        }
        cursor.close();
        return truck.id;
    }

    public void save(DataTruck truck) {
        ContentValues values = new ContentValues();
        values.put(KEY_TRUCK_NUMBER, truck.truckNumber);
        values.put(KEY_LICENSE_PLATE, truck.licensePlateNumber);
        values.put(KEY_SERVER_ID, truck.serverId);
        if (truck.id > 0) {
            String where = KEY_ROWID + "=?";
            String[] whereArgs = new String[]{Long.toString(truck.id)};
            mDb.update(TABLE_NAME, values, where, whereArgs);
        } else {
            truck.id = mDb.insert(TABLE_NAME, null, values);
        }
    }

    public DataTruck query(long id) {
        String selection = KEY_ROWID + "=?";
        String[] selectionArgs = new String[]{Long.toString(id)};
        Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        DataTruck truck = new DataTruck();
        if (cursor.moveToNext()) {
            int idxId = cursor.getColumnIndex(KEY_ROWID);
            int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
            int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getInt(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
        } else {
            truck = null;
        }
        return truck;
    }

    public List<DataTruck> query() {
        Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null, null, null);
        int idxId = cursor.getColumnIndex(KEY_ROWID);
        int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
        int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
        int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
        List<DataTruck> list = new ArrayList();
        while (cursor.moveToNext()) {
            DataTruck truck = new DataTruck();
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getInt(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
            list.add(truck);
        }
        return list;
    }

    public DataTruck queryByServerId(long id) {
        String selection = KEY_SERVER_ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(id)};
        Cursor cursor = mDb.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        DataTruck truck = new DataTruck();
        if (cursor.moveToNext()) {
            int idxId = cursor.getColumnIndex(KEY_ROWID);
            int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
            int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
            int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = cursor.getInt(idxTruckNumber);
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
        } else {
            truck = null;
        }
        return truck;
    }

    void remove(long id) {
        String where = KEY_ROWID + "=?";
        String[] whereArgs = {Long.toString(id)};
        mDb.delete(TABLE_NAME, where, whereArgs);
    }

    public void removeIfUnused(DataTruck truck) {
        if (TableEntry.getInstance().countTrucks(truck.id) == 0) {
            Timber.i("remove(" + truck.toString() + ")");
            remove(truck.id);
        }
    }

}
