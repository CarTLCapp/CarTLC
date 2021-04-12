/**
 * Copyright 2017-2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataZipCode
import com.cartlc.tracker.fresh.model.core.table.TableZipCode

import com.cartlc.tracker.fresh.ui.app.TBApplication

/**
 * Created by dug on 8/24/17.
 */

class SqlTableZipCode(
        private val dbSql: SQLiteDatabase
): TableZipCode {

    companion object {

        private const val TABLE_NAME = "zipcodes"

        private const val KEY_ROWID = "_id"
        private const val KEY_ZIPCODE = "zipcode"
        private const val KEY_STATE_LONG = "state_long"
        private const val KEY_STATE_SHORT = "state_short"
        private const val KEY_CITY = "city"
    }

    override fun clearAll() {
        try {
            dbSql.delete(TABLE_NAME, null, null)
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableZipCode::class.java, "clear()", "db")
        }
    }

    fun create() {
        val sbuf = StringBuilder()
        sbuf.append("create table ")
        sbuf.append(TABLE_NAME)
        sbuf.append(" (")
        sbuf.append(KEY_ROWID)
        sbuf.append(" integer primary key autoincrement, ")
        sbuf.append(KEY_ZIPCODE)
        sbuf.append(" varchar(64), ")
        sbuf.append(KEY_STATE_LONG)
        sbuf.append(" varchar(64), ")
        sbuf.append(KEY_STATE_SHORT)
        sbuf.append(" varchar(16), ")
        sbuf.append(KEY_CITY)
        sbuf.append(" varchar(1024))")
        dbSql.execSQL(sbuf.toString())
    }

    override fun add(data: DataZipCode) {
        if (query(data.zipCode) != null) {
            return
        }
        dbSql.beginTransaction()
        try {
            val values = ContentValues()
            values.put(KEY_ZIPCODE, data.zipCode)
            values.put(KEY_STATE_LONG, data.stateLongName)
            values.put(KEY_STATE_SHORT, data.stateShortName)
            values.put(KEY_CITY, data.city)
            dbSql.insert(TABLE_NAME, null, values)
            dbSql.setTransactionSuccessful()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableZipCode::class.java, "add()", "db")
        } finally {
            dbSql.endTransaction()
        }
    }

    override fun query(zipCode: String?): DataZipCode? {
        if (zipCode == null) {
            return null
        }
        var data: DataZipCode? = null
        try {
            val selection = "$KEY_ZIPCODE=?"
            val selectionArgs = arrayOf(zipCode)
            val cursor = dbSql.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
            if (cursor.moveToFirst() && cursor.count > 0) {
                val idxZipCode = cursor.getColumnIndex(KEY_ZIPCODE)
                val idxStateLong = cursor.getColumnIndex(KEY_STATE_LONG)
                val idxStateShort = cursor.getColumnIndex(KEY_STATE_SHORT)
                val idxCity = cursor.getColumnIndex(KEY_CITY)
                if (cursor.moveToNext()) {
                    data = DataZipCode()
                    data.zipCode = cursor.getString(idxZipCode)
                    data.stateShortName = cursor.getString(idxStateShort)
                    data.stateLongName = cursor.getString(idxStateLong)
                    data.city = cursor.getString(idxCity)
                }
            }
            cursor.close()
        } catch (ex: Exception) {
            TBApplication.ReportError(ex, SqlTableZipCode::class.java, "query()", "db")
        }
        return data
    }

    override fun queryState(zipCode: String): String? {
        val data = query(zipCode)
        return data?.stateLongName
    }

    override fun queryCity(zipCode: String): String? {
        val data = query(zipCode)
        return data?.city
    }

}
