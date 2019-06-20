package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataCollectionItem
import com.cartlc.tracker.fresh.model.core.data.DataNote

interface TableCollectionNoteProject : TableCollection {
    fun getNotes(projectNameId: Long): List<DataNote>
    fun removeIfGone(item: DataCollectionItem)
}