/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.mainlist.adapter

import com.cartlc.tracker.fresh.model.core.data.DataNote

interface NoteListEntryUseCase {

    val numNotes: Int
    val notes: List<DataNote>

    fun onNoteDataChanged()

}