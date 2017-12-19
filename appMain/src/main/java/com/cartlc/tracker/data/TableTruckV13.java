package com.cartlc.tracker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.cartlc.tracker.app.TBApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 8/31/17.
 */

public class TableTruckV13 {

    static final String TABLE_NAME = "table_trucks";

    static final String KEY_ROWID         = "_id";
    static final String KEY_TRUCK_NUMBER  = "truck_number";
    static final String KEY_LICENSE_PLATE = "license_plate";
    static final String KEY_SERVER_ID     = "server_id";
    static final String KEY_PROJECT_ID    = "project_id";
    static final String KEY_COMPANY_NAME  = "company_name";

    static TableTruckV13 sInstance;

    static void Init(SQLiteDatabase db) {
        new TableTruckV13(db);
    }

    public static TableTruckV13 getInstance() {
        return sInstance;
    }

    final SQLiteDatabase mDb;

    TableTruckV13(SQLiteDatabase db) {
        sInstance = this;
        this.mDb = db;
    }

    public void upgrade11() {
        try {
            mDb.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_PROJECT_ID + " int default 0");
            mDb.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + KEY_COMPANY_NAME + " varchar(256)");
        } catch (Exception ex) {
            TBApplication.ReportError(ex, TableTruck.class, "upgrade11()", "db");
        }
    }

    public void transfer() {
        Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null, null, null);
        DataTruck truck;
        while (cursor.moveToNext()) {
            final int idxId = cursor.getColumnIndex(KEY_ROWID);
            final int idxTruckNumber = cursor.getColumnIndex(KEY_TRUCK_NUMBER);
            final int idxLicensePlate = cursor.getColumnIndex(KEY_LICENSE_PLATE);
            final int idxServerId = cursor.getColumnIndex(KEY_SERVER_ID);
            final int idxProjectId = cursor.getColumnIndex(KEY_PROJECT_ID);
            final int idxCompanyName = cursor.getColumnIndex(KEY_COMPANY_NAME);
            truck = new DataTruck();
            truck.id = cursor.getLong(idxId);
            truck.truckNumber = Integer.toString(cursor.getInt(idxTruckNumber));
            truck.licensePlateNumber = cursor.getString(idxLicensePlate);
            truck.serverId = cursor.getLong(idxServerId);
            truck.projectNameId = cursor.getInt(idxProjectId);
            truck.companyName = cursor.getString(idxCompanyName);
            TableTruck.getInstance().save(truck);
        }
        cursor.close();
        mDb.delete(TABLE_NAME, null, null);
    }

}
