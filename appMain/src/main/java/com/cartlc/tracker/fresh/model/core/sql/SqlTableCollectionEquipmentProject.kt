/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.sql

import android.database.sqlite.SQLiteDatabase
import com.cartlc.tracker.fresh.model.core.data.DataCollectionEquipmentProject
import com.cartlc.tracker.fresh.model.core.data.DataCollectionItem
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableCollectionEquipmentProject

import timber.log.Timber

/**
 * Created by dug on 5/16/17.
 */
class SqlTableCollectionEquipmentProject(
        private val db: DatabaseTable,
        sqlDb: SQLiteDatabase
) : SqlTableCollection(sqlDb, TABLE_NAME), TableCollectionEquipmentProject {

    companion object {
        internal const val TABLE_NAME = "project_equipment_collection"
    }

    override fun queryForProject(projectNameId: Long): DataCollectionEquipmentProject {
        val collection = DataCollectionEquipmentProject(db, projectNameId)
        collection.equipmentListIds = query(projectNameId).toMutableList()
        return collection
    }

//    fun addByName(projectName: String, equipments: List<String>) {
//        var projectNameId = db.tableProjects.queryProjectId(projectName)
//        if (projectNameId < 0) {
//            projectNameId = db.tableProjects.addTest(projectName)
//        }
//        addByNameTest(projectNameId, equipments)
//    }
//
//    fun addByNameTest(collectionId: Long, names: List<String>) {
//        val list = ArrayList<Long>()
//        for (name in names) {
//            var id = db.tableEquipment.query(name)
//            if (id < 0) {
//                id = db.tableEquipment.addTest(name)
//            }
//            list.add(id)
//        }
//        addTest(collectionId, list)
//    }

    override fun addLocal(name: String, projectNameId: Long) {
        val equipId = db.tableEquipment.addLocal(name)
        add(projectNameId, equipId)
    }

    override fun removeIfGone(item: DataCollectionItem) {
        if (item.isBootstrap) {
            if (db.tableEquipment.query(item.value_id) == null) {
                Timber.i("remove(${item.id}, $item)")
                remove(item.id)
            }
        }
    }


}
