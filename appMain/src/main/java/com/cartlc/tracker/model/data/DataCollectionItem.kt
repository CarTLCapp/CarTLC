/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.model.data

/**
 * Created by dug on 5/30/17.
 */

class DataCollectionItem {
    var id: Long = 0 // row_id
    var collection_id: Long = 0 // project_id or collection_id
    var value_id: Long = 0
    var server_id: Int = 0
    var isBootstrap: Boolean = false

    override fun equals(other: Any?): Boolean {
        return if (other is DataCollectionItem) {
            return collection_id == other.collection_id && value_id == other.value_id
        } else super.equals(other)
    }

    override fun toString(): String {
        val sbuf = StringBuilder()
        sbuf.append("[")
        sbuf.append(collection_id)
        sbuf.append(",")
        sbuf.append(value_id)
        sbuf.append(",")
        sbuf.append(server_id)
        sbuf.append("]")
        return sbuf.toString()
    }
}
