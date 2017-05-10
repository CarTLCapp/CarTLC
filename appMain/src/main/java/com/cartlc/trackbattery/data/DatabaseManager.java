package com.cartlc.trackbattery.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class DatabaseManager {

    static final String DATABASE_NAME = "cartcl.mDb";
    static final int DATABASE_VERSION = 1;

    public static void Init(Context ctx) {
        new DatabaseManager(ctx);
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            init(db);
            try {
                TableAddress.getInstance().create();
                TableProjects.getInstance().create();
            } catch (Exception ex) {
                Timber.e(ex);
            }
        }

        void init(SQLiteDatabase db) {
            TableAddress.Init(db);
            TableProjects.Init(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            init(db);
        }
    }

    static DatabaseManager sInstance;

    final DatabaseHelper mDbHelper;
    final Context mCtx;
    SQLiteDatabase mDb;

    DatabaseManager(Context ctx) {
        mCtx = ctx;
        mDbHelper = new DatabaseHelper(ctx);
        mDb = mDbHelper.getWritableDatabase();
        sInstance = this;
    }
}
