/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.model.core.data

class DataFlowElementNote(
        var id: Long = 0,
        var flowElementId: Long = 0,
        var noteId: Long = 0
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataFlowElementNote

        if (flowElementId != other.flowElementId) return false
        if (noteId != other.noteId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flowElementId.hashCode()
        result = 31 * result + noteId.hashCode()
        return result
    }

    override fun toString(): String {
        return "DataFlowElementNote(id=$id, flowElementId=$flowElementId, noteId=$noteId)"
    }

}