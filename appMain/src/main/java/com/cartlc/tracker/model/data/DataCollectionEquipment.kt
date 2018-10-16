/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

import com.cartlc.tracker.model.table.DatabaseTable

/**
 * Created by dug on 5/10/17.
 */
abstract class DataCollectionEquipment(
        private val db: DatabaseTable,
        var id: Long // projectId or collectionId
) {
    var equipmentListIds: MutableList<Long> = mutableListOf()

    val equipment: List<DataEquipment>
        get() {
            val list = mutableListOf<DataEquipment>()
            for (e in equipmentListIds) {
                val item = db.equipment.query(e)
                if (item != null) {
                    list.add(item)
                }
            }
            return list
        }

    val equipmentNames: List<String>?
        get() {
            val list = mutableListOf<String>()
            for (e in equipment) {
                list.add(e.name)
            }
            return list
        }

    fun add(equipmentId: Long) {
        equipmentListIds.add(equipmentId)
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        var first = true
        for (name in equipmentNames!!) {
            if (first) {
                first = false
            } else {
                sbuf.append(",")
            }
            sbuf.append(name)
        }
        return sbuf.toString()
    }

}
