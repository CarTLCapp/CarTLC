/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataNote

interface NoteHelper {

    fun getPendingNotes(projectNameId: Long): List<DataNote>

}