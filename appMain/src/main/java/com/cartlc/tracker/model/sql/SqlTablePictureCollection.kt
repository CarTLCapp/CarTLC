/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.model.data.DataPicture
import com.cartlc.tracker.model.data.DataPictureCollection

import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.model.table.TablePictureCollection

import java.io.File
import java.util.ArrayList

import timber.log.Timber

/**
 * Created by dug on 5/10/17.
 */

class SqlTablePictureCollection(
        private val dbSql: SQLiteDatabase
) : TablePictureCollection {

    companion object {

        internal val TABLE_NAME = "picture_collection"

        internal val KEY_ROWID = "_id"
        internal val KEY_COLLECTION_ID = "collection_id"
        internal val KEY_PICTURE_FILENAME = "picture_filename"
        internal val KEY_UPLOADING_FILENAME = "uploading_filename"
        internal val KEY_NOTE = "picture_note"
        internal val KEY_UPLOADED = "uploaded"

        fun upgrade3(db: DatabaseTable, dbSql: SQLiteDatabase) {
            try {
                dbSql.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $KEY_NOTE text")
            } catch (ex: Exception) {
                db.reportError(ex, SqlTablePictureCollection::class.java, "upgrade3()", "db")
            }
        }
    }

    fun clear() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
        }
    }

    //    public void drop() {
    //        dbSql.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    //    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_COLLECTION_ID)
        sbuf.append(" long default 0, ")
        sbuf.append(KEY_PICTURE_FILENAME)
        sbuf.append(" text, ")
        sbuf.append(KEY_UPLOADING_FILENAME)
        sbuf.append(" text, ")
        sbuf.append(KEY_NOTE)
        sbuf.append(" text, ")
        sbuf.append(KEY_UPLOADED)
        sbuf.append(" bit default 0)")
        dbSql.execSQL(sbuf.toString())
    }

    override fun add(picture: File, collection_id: Long?): DataPicture {
        val item = DataPicture(unscaledFilename = picture.absolutePath)
        update(item, collection_id)
        return item
    }

    override fun add(collection: DataPictureCollection) {
        dbSql.beginTransaction()
        try {
            for (ele in collection.pictures) {
                update(ele, collection.id)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePictureCollection::class.java, "add()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun query(collection_id: Long): DataPictureCollection {
        val selection = "$KEY_COLLECTION_ID =?"
        val selectionArgs = arrayOf(java.lang.Long.toString(collection_id))
        val collection = DataPictureCollection(collection_id)
        collection.pictures = query(selection, selectionArgs).toMutableList()
        return collection
    }

    override fun queryPictures(collection_id: Long): List<DataPicture> {
        val selection = "$KEY_COLLECTION_ID=?"
        val selectionArgs = arrayOf(java.lang.Long.toString(collection_id))
        return query(selection, selectionArgs)
    }

    fun query(selection: String, selectionArgs: Array<String>?): List<DataPicture> {
        val list = mutableListOf<DataPicture>()
        try {
            val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxPicture = cursor.getColumnIndex(KEY_PICTURE_FILENAME)
            val idxUploading = cursor.getColumnIndex(KEY_UPLOADING_FILENAME)
            val idxUploaded = cursor.getColumnIndex(KEY_UPLOADED)
            val idxNote = cursor.getColumnIndex(KEY_NOTE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idxRowId)
                val filename = cursor.getString(idxPicture)
                val uploading = cursor.getString(idxUploading)
                val note = cursor.getString(idxNote)
                val uploaded = cursor.getShort(idxUploaded).toInt() != 0
                val picture = DataPicture(id, filename, uploading, note, uploaded)
                list.add(picture)
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePictureCollection::class.java, "query()", "db")
        }
        return list
    }

    override fun removeNonExistant(list: List<DataPicture>): List<DataPicture> {
        val filtered = ArrayList<DataPicture>()
        for (item in list) {
            if (item.existsUnscaled || item.existsScaled) {
                filtered.add(item)
            } else {
                remove(item)
            }
        }
        return filtered
    }

    @Synchronized
    override fun clearUploadedUnscaledPhotos() {
        val selection = "$KEY_UPLOADED=1"
        val list = query(selection, null)
        for (item in list) {
            if (item.existsUnscaled) {
                item.unscaledFile.delete()
            }
        }
    }

    override fun countPictures(collection_id: Long): Int {
        var count = 0
        try {
            val columns = arrayOf(KEY_ROWID, KEY_PICTURE_FILENAME, KEY_UPLOADING_FILENAME)
            val selection = "$KEY_COLLECTION_ID=?"
            val selectionArgs = arrayOf(java.lang.Long.toString(collection_id))
            val cursor = dbSql.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null)
            val idxPicture = cursor.getColumnIndex(KEY_PICTURE_FILENAME)
            val idxUploading = cursor.getColumnIndex(KEY_UPLOADING_FILENAME)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val delete = ArrayList<Long>()
            while (cursor.moveToNext()) {
                val unscaled = cursor.getString(idxPicture)
                val uploading = cursor.getString(idxUploading)
                val unscaledFile: File?
                val uploadingFile: File?
                if (unscaled != null) {
                    unscaledFile = File(unscaled)
                } else {
                    unscaledFile = null
                }
                if (uploading != null) {
                    uploadingFile = File(uploading)
                } else {
                    uploadingFile = null
                }
                if (unscaledFile != null && unscaledFile.exists() || uploadingFile != null && uploadingFile.exists()) {
                    count++
                } else {
                    delete.add(cursor.getLong(idxRowId))
                }
            }
            cursor.close()
            val where = "$KEY_ROWID=?"
            for (id in delete) {
                val whereArgs = arrayOf(java.lang.Long.toString(id))
                dbSql.delete(TABLE_NAME, where, whereArgs)
            }
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePictureCollection::class.java, "countPictures()", "db")
        }
        return count
    }

    override fun createCollectionFromPending(nextPictureCollectionID: Long): DataPictureCollection {
        val collection = DataPictureCollection(nextPictureCollectionID)
        collection.pictures = removeNonExistant(queryPictures(0)).toMutableList()
        return collection
    }

    override fun clearPendingPictures() {
        try {
            val where = "$KEY_COLLECTION_ID =0"
            dbSql.delete(TABLE_NAME, where, null)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePictureCollection::class.java, "clearPendingPictures()", "db")
        }
    }

    fun remove(item: DataPicture) {
        try {
            val where = "$KEY_COLLECTION_ID=?"
            val whereArgs = arrayOf(java.lang.Long.toString(item.id))
            dbSql.delete(TABLE_NAME, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePictureCollection::class.java, "remove()", "db")
        }
    }

    @Synchronized
    override fun setUploaded(item: DataPicture) {
        item.uploaded = true
        update(item, null)
    }

    fun update(item: DataPicture, collection_id: Long?) {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_PICTURE_FILENAME, item.unscaledFilename)
            values.put(KEY_UPLOADING_FILENAME, item.scaledFilename)
            values.put(KEY_NOTE, item.note)
            values.put(KEY_UPLOADED, if (item.uploaded) 1 else 0)
            if (collection_id != null) {
                values.put(KEY_COLLECTION_ID, collection_id)
            }
            if (item.id > 0) {
                val where = "$KEY_ROWID=?"
                val whereArgs = arrayOf(java.lang.Long.toString(item.id))
                if (dbSql.update(TABLE_NAME, values, where, whereArgs) == 0) {
                    Timber.e("Mysterious failure updating picture ID " + item.id)
                    item.id = 0
                }
            }
            if (item.id == 0L) {
                item.id = dbSql.insert(TABLE_NAME, null, values)
            }
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePictureCollection::class.java, "update()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    fun clearUploaded() {
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_UPLOADED, 0)
            dbSql.update(TABLE_NAME, values, null, null)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTablePictureCollection::class.java, "clearUploaded()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

}
