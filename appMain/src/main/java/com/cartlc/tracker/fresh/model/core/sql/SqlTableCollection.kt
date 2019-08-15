/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataCollectionEquipment
import com.cartlc.tracker.fresh.model.core.data.DataCollectionItem
import com.cartlc.tracker.fresh.model.core.table.TableCollection

import com.cartlc.tracker.fresh.ui.app.TBApplication

import java.util.ArrayList

/**
 * Created by dug on 5/10/17.
 */

abstract class SqlTableCollection(
        internal val mDb: SQLiteDatabase,
        internal val mTableName: String
): TableCollection {

    companion object {

        private const val KEY_ROWID = "_id"
        private const val KEY_COLLECTION_ID = "collection_id"
        private const val KEY_VALUE_ID = "value_id"
        private const val KEY_SERVER_ID = "server_id"
        private const val KEY_IS_BOOT = "is_boot_strap"
    }

    fun clear() {
        try {
            mDb.delete(mTableName, null, null)
        } catch (ex: Exception) {
        }
    }

    fun drop() {
        mDb.execSQL("DROP TABLE IF EXISTS $mTableName")
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(mTableName)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_COLLECTION_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_VALUE_ID)
        sbuf.append(" long, ")
        sbuf.append(KEY_SERVER_ID)
        sbuf.append(" int, ")
        sbuf.append(KEY_IS_BOOT)
        sbuf.append(" bit default 0)")
        mDb.execSQL(sbuf.toString())
    }

    fun count(): Int {
        var count = 0
        try {
            val cursor = mDb.query(mTableName, null, null, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "count()", "db")
        }
        return count
    }

    protected fun countCollection(collection_id: Long): Int {
        var count = 0
        try {
            val columns = arrayOf(KEY_VALUE_ID)
            val selection = "$KEY_COLLECTION_ID=?"
            val selectionArgs = arrayOf(collection_id.toString())
            val cursor = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "query(id)", "db")
        }
        return count
    }

    override fun countValues(valueId: Long): Int {
        var count = 0
        try {
            val where = "$KEY_VALUE_ID=?"
            val whereArgs = arrayOf(valueId.toString())
            val cursor = mDb.query(mTableName, null, where, whereArgs, null, null, null)
            count = cursor.count
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "countValues()", "db")
        }
        return count
    }

    override fun save(collection: DataCollectionEquipment) {
        save(collection.id, collection.equipmentListIds)
    }

    fun save(collectionId: Long, ids: List<Long>) {
        removeByCollectionId(collectionId)
        mDb.beginTransaction()
        try {
            val values = ContentValues()
            for (id in ids) {
                values.clear()
                values.put(KEY_COLLECTION_ID, collectionId)
                values.put(KEY_VALUE_ID, id)
                mDb.insert(mTableName, null, values)
            }
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "saveUploaded()", "db")
        } finally {
            mDb.endTransaction()
        }
    }

    fun addTest(collectionId: Long, ids: List<Long>) {
        mDb.beginTransaction()
        try {
            val values = ContentValues()
            for (id in ids) {
                values.clear()
                values.put(KEY_COLLECTION_ID, collectionId)
                values.put(KEY_VALUE_ID, id)
                values.put(KEY_IS_BOOT, 1)
                mDb.insert(mTableName, null, values)
            }
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "saveUploaded()", "db")
        } finally {
            mDb.endTransaction()
        }
    }

    fun add(collectionId: Long, valueId: Long) {
        mDb.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_COLLECTION_ID, collectionId)
            values.put(KEY_VALUE_ID, valueId)
            mDb.insert(mTableName, null, values)
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "add(id,id)", "db")
        } finally {
            mDb.endTransaction()
        }
    }

    override fun add(item: DataCollectionItem) {
        mDb.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_COLLECTION_ID, item.collection_id)
            values.put(KEY_VALUE_ID, item.value_id)
            values.put(KEY_SERVER_ID, item.server_id)
            values.put(KEY_IS_BOOT, item.isBootstrap)
            item.id = mDb.insert(mTableName, null, values)
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "add(item)", "db")
        } finally {
            mDb.endTransaction()
        }
    }

    override fun update(item: DataCollectionItem) {
        mDb.beginTransaction()
        try {
            val values = ContentValues()
            values.clear()
            values.put(KEY_COLLECTION_ID, item.collection_id)
            values.put(KEY_VALUE_ID, item.value_id)
            values.put(KEY_SERVER_ID, item.server_id)
            values.put(KEY_IS_BOOT, item.isBootstrap)
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(item.id.toString())
            mDb.update(mTableName, values, where, whereArgs)
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "update(item)", "db")
        } finally {
            mDb.endTransaction()
        }
    }

    fun query(collection_id: Long): List<Long> {
        val collection = ArrayList<Long>()
        try {
            val columns = arrayOf(KEY_VALUE_ID)
            val selection = "$KEY_COLLECTION_ID=?"
            val selectionArgs = arrayOf(collection_id.toString())
            val cursor = mDb.query(mTableName, columns, selection, selectionArgs, null, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_VALUE_ID)
            while (cursor.moveToNext()) {
                collection.add(cursor.getLong(idxValue))
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "query(id)", "db")
        }
        return collection
    }

    override fun query(): List<DataCollectionItem> {
        val items = ArrayList<DataCollectionItem>()
        mDb.beginTransaction()
        try {
            val cursor = mDb.query(mTableName, null, null, null, null, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_VALUE_ID)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxServerId = cursor.getColumnIndex(KEY_SERVER_ID)
            val idxCollectionId = cursor.getColumnIndex(KEY_COLLECTION_ID)
            val idxTest = cursor.getColumnIndex(KEY_IS_BOOT)
            while (cursor.moveToNext()) {
                val item = DataCollectionItem()
                item.id = cursor.getLong(idxRowId)
                item.collection_id = cursor.getLong(idxCollectionId)
                item.value_id = cursor.getLong(idxValue)
                item.server_id = cursor.getInt(idxServerId)
                item.isBootstrap = cursor.getShort(idxTest).toInt() != 0
                items.add(item)
            }
            cursor.close()
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "query()", "db")
        } finally {
            mDb.endTransaction()
        }
        return items
    }

    override fun queryByServerId(server_id: Int): DataCollectionItem? {
        var item: DataCollectionItem? = null
        mDb.beginTransaction()
        try {
            val selection = "$KEY_SERVER_ID=?"
            val selectionArgs = arrayOf(server_id.toString())
            val cursor = mDb.query(mTableName, null, selection, selectionArgs, null, null, null, null)
            val idxValue = cursor.getColumnIndex(KEY_VALUE_ID)
            val idxRowId = cursor.getColumnIndex(KEY_ROWID)
            val idxCollectionId = cursor.getColumnIndex(KEY_COLLECTION_ID)
            val idxTest = cursor.getColumnIndex(KEY_IS_BOOT)
            if (cursor.moveToFirst()) {
                item = DataCollectionItem()
                item.id = cursor.getLong(idxRowId)
                item.collection_id = cursor.getLong(idxCollectionId)
                item.value_id = cursor.getLong(idxValue)
                item.server_id = server_id
                item.isBootstrap = cursor.getShort(idxTest).toInt() != 0
            }
            cursor.close()
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "queryByServerId(id)", "db")
        } finally {
            mDb.endTransaction()
        }
        return item
    }

    override fun remove(id: Long) {
        try {
            val where = "$KEY_ROWID=?"
            val whereArgs = arrayOf(id.toString())
            mDb.delete(mTableName, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "remove(id)", "db")
        }
    }

    fun removeByCollectionId(id: Long) {
        try {
            val where = "$KEY_COLLECTION_ID=?"
            val whereArgs = arrayOf(id.toString())
            mDb.delete(mTableName, where, whereArgs)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "removeByCollectionId(id)", "db")
        }
    }

    fun clearUploaded() {
        mDb.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_SERVER_ID, 0)
            mDb.update(mTableName, values, null, null)
            mDb.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableCollection::class.java, "clearUploaded()", "db")
        } finally {
            mDb.endTransaction()
        }
    }

}
