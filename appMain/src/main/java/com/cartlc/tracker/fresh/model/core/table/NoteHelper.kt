/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataNote

interface NoteHelper {

    fun getNotesFromCurrentFlowElementId(elementId: Long?): List<DataNote>
    fun getNotesOverlaidFrom(elementId: Long, entry: DataEntry?): List<DataNote>
    fun getPendingNotes(projectNameId: Long): List<DataNote>

}