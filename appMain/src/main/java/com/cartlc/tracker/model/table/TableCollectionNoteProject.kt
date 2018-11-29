package com.cartlc.tracker.model.table

import com.cartlc.tracker.model.data.DataCollectionItem
import com.cartlc.tracker.model.data.DataNote

interface TableCollectionNoteProject : TableCollection {
    fun getNotes(projectNameId: Long): List<DataNote>
    fun removeIfGone(item: DataCollectionItem)
}