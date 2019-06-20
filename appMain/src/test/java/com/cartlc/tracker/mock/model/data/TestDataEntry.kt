/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.mock.model.data

import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.core.table.TableCollectionNoteEntry
import com.cartlc.tracker.fresh.model.core.table.TableCollectionNoteProject
import com.cartlc.tracker.fresh.model.core.table.TableNote
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TestDataEntry {

    @Mock
    private lateinit var db: DatabaseTable

    @Mock
    private lateinit var tableNote: TableNote

    @Mock
    private lateinit var tableCollectionNoteProject: TableCollectionNoteProject

    @Mock
    private lateinit var tableCollectionNoteEntry: TableCollectionNoteEntry

    private lateinit var entry: DataEntry
    private val listNotes = listOf(
            DataNote(1, "note1", DataNote.Type.TEXT, "value1"),
            DataNote(2, "note2", DataNote.Type.TEXT, "bad"),
            DataNote(3, "note3", DataNote.Type.TEXT, "value3")
    )
    private val listNotesWithValues = listOf(
            DataNote(2, "note2", DataNote.Type.TEXT, "value2"),
            DataNote(4, "note4", DataNote.Type.TEXT, "value4")
    )

    @Before
    fun onBefore() {
        MockitoAnnotations.initMocks(this)
        entry = DataEntry(db)
        entry.projectAddressCombo = DataProjectAddressCombo(db, 0, 0)
    }

    @Test
    fun `call to notesAllWithValuesOverlaid`() {
        `when`(db.tableCollectionNoteProject).thenReturn(tableCollectionNoteProject)
        `when`(db.tableCollectionNoteEntry).thenReturn(tableCollectionNoteEntry)
        `when`(db.tableNote).thenReturn(tableNote)
        `when`(tableCollectionNoteProject.getNotes(ArgumentMatchers.anyLong())).thenReturn(listNotes)
        `when`(tableCollectionNoteEntry.query(ArgumentMatchers.anyLong())).thenReturn(listNotesWithValues)

        val list = entry.notesAllWithValuesOverlaid

        assertEquals(3, list.size)
        assertEquals("value1", list[0].value)
        assertEquals("value2", list[1].value)
        assertEquals("value3", list[2].value)
    }

}