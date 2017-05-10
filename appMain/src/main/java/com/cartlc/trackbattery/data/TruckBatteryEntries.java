package com.cartlc.trackbattery.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dug on 4/17/17.
 */

public class TruckBatteryEntries {

    static final String TABLE_NAME = "truck_battery_entries";
    static final String KEY_ROWID = "_id";
    static final String KEY_DATE = "date";
    static final String KEY_TRUCK_ID = "truck";
    static final String KEY_BATTERY_ID = "battery";
    static final String KEY_TECH_ID = "tech_id";

    static TableState sInstance;

    static void Init(SQLiteDatabase db) {
        new TruckBatteryEntries(db);
    }

    public static TableState getInstance() {
        return sInstance;
    }

    final SQLiteDatabase db;

    TruckBatteryEntries(SQLiteDatabase db) {
        this.db = db;
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("create table ");
        sbuf.append(TABLE_NAME);
        sbuf.append(" (");
        sbuf.append(KEY_ROWID);
        sbuf.append(" integer primary key autoincrement, ");
        sbuf.append(KEY_DATE);
        sbuf.append(" long, ");
        sbuf.append(KEY_TRUCK_ID);
        sbuf.append(" text, ");
        sbuf.append(KEY_BATTERY_ID);
        sbuf.append(" text, ");
        sbuf.append(KEY_TECH_ID);
        sbuf.append(" long)");
        db.execSQL(sbuf.toString());
    }
}
