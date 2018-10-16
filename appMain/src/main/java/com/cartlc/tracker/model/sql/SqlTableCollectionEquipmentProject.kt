/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.sql

import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.model.data.DataCollectionEquipmentProject
import com.cartlc.tracker.model.data.DataCollectionItem
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.model.table.TableCollectionEquipmentProject

import java.util.ArrayList

import timber.log.Timber

/**
 * Created by dug on 5/16/17.
 */
class SqlTableCollectionEquipmentProject(
        private val db: DatabaseTable,
        sqlDb: SQLiteDatabase
) : SqlTableCollection(sqlDb, TABLE_NAME), TableCollectionEquipmentProject {

    companion object {
        internal val TABLE_NAME = "project_equipment_collection"
    }

    override fun queryForProject(projectNameId: Long): DataCollectionEquipmentProject {
        val collection = DataCollectionEquipmentProject(db, projectNameId)
        collection.equipmentListIds = query(projectNameId).toMutableList()
        return collection
    }

    fun addByName(projectName: String, equipments: List<String>) {
        var projectNameId = db.projects.queryProjectName(projectName)
        if (projectNameId < 0) {
            projectNameId = db.projects.addTest(projectName)
        }
        addByNameTest(projectNameId, equipments)
    }

    fun addByNameTest(collectionId: Long, names: List<String>) {
        val list = ArrayList<Long>()
        for (name in names) {
            var id = db.equipment.query(name)
            if (id < 0) {
                id = db.equipment.addTest(name)
            }
            list.add(id)
        }
        addTest(collectionId, list)
    }

    override fun addLocal(name: String, projectNameId: Long) {
        val equipId = db.equipment.addLocal(name)
        add(projectNameId, equipId)
    }

    override fun removeIfGone(item: DataCollectionItem) {
        if (item.isBootstrap) {
            if (db.equipment.query(item.value_id) == null) {
                Timber.i("remove(" + item.id + ", " + item.toString() + ")")
                remove(item.id)
            }
        }
    }


}
