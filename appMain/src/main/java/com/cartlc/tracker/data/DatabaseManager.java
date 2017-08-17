package com.cartlc.tracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;

/**
 * Created by dug on 4/17/17.
 */

public class DatabaseManager {

    static final String DATABASE_NAME    = "cartcl.db";
    static final int    DATABASE_VERSION = 3;

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
                TableEntry.getInstance().create();
                TableEquipment.getInstance().create();
                TableCollectionEquipmentEntry.getInstance().create();
                TableCollectionEquipmentProject.getInstance().create();
                TableNote.getInstance().create();
                TableCollectionNoteEntry.getInstance().create();
                TableCollectionNoteProject.getInstance().create();
                TablePictureCollection.getInstance().create();
                TableProjectAddressCombo.getInstance().create();
                TableProjects.getInstance().create();
                TableCrash.getInstance().create();
            } catch (Exception ex) {
                Timber.e(ex);
            }
        }

        void init(SQLiteDatabase db) {
            TableAddress.Init(db);
            TableEntry.Init(db);
            TableEquipment.Init(db);
            TableCollectionEquipmentEntry.Init(db);
            TableCollectionEquipmentProject.Init(db);
            TableNote.Init(db);
            TableCollectionNoteEntry.Init(db);
            TableCollectionNoteProject.Init(db);
            TablePictureCollection.Init(db);
            TableProjectAddressCombo.Init(db);
            TableProjects.Init(db);
            TableCrash.Init(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1) {
                if (newVersion >= 2) {
                    init(db);
                    TableEntry.upgrade(db);
                    if (newVersion == 3) {
                        TableCrash.getInstance().create();
                    }
                }
            } else if (oldVersion == 2) {
                if (newVersion == 3) {
                    TableCrash.getInstance().create();
                }
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            init(db);
        }
    }

    public static DatabaseManager getInstance() {
        return sInstance;
    }

    static DatabaseManager sInstance;

    final DatabaseHelper mDbHelper;
    final Context        mCtx;
    SQLiteDatabase mDb;

    DatabaseManager(Context ctx) {
        mCtx = ctx;
        mDbHelper = new DatabaseHelper(ctx);
        mDb = mDbHelper.getWritableDatabase();
        sInstance = this;
    }

    public void clearUploaded() {
        PrefHelper.getInstance().clearUploaded();
        TableEntry.getInstance().clearUploaded();
        TableEquipment.getInstance().clearUploaded();
        TableNote.getInstance().clearUploaded();
        TablePictureCollection.getInstance().clearUploaded();
        TableProjects.getInstance().clearUploaded();
        TableAddress.getInstance().clearUploaded();
        TableCollectionEquipmentEntry.getInstance().clearUploaded();
        TableCollectionEquipmentProject.getInstance().clearUploaded();
        TableCollectionNoteProject.getInstance().clearUploaded();
        TableCrash.getInstance().clearUploaded();
    }
}
