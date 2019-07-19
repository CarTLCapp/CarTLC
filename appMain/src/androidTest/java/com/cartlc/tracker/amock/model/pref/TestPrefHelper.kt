/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.amock.model.pref

import android.content.Context
import com.cartlc.tracker.fresh.model.pref.PrefHelper
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cartlc.tracker.fresh.model.core.data.DataAddress
import com.cartlc.tracker.fresh.model.core.data.DataProjectAddressCombo
import com.cartlc.tracker.fresh.model.core.table.*
import com.cartlc.tracker.model.table.*
import com.cartlc.tracker.fresh.model.table.*
import com.cartlc.tracker.ui.app.TBApplication
import com.nhaarman.mockito_kotlin.any
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.never

@RunWith(AndroidJUnit4::class)
class TestPrefHelper {

    companion object {
        private const val ROOT_NAME = "RootName"
        private const val SUB_NAME = "SubProject"
        private const val PROJECT_ID = 5L
    }
    lateinit var context: Context

    @Mock
    lateinit var db: DatabaseTable

    @Mock
    lateinit var tableProjects: TableProjects

    @Mock
    lateinit var tableAddress: TableAddress

    @Mock
    lateinit var tableProjectAddressCombo: TableProjectAddressCombo

    @Mock
    lateinit var tableNote: TableNote

    @Mock
    lateinit var tablePictureCollection: TablePictureCollection

    @Mock
    lateinit var tableEquipment: TableEquipment

    @Mock
    lateinit var tableEntry: TableEntry

    lateinit var prefHelper: PrefHelper

    @Before
    fun onBefore() {
        TBApplication.DEBUG_TREE = true
        MockitoAnnotations.initMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        prefHelper = PrefHelper(context, db)
        Mockito.`when`(db.tableNote).thenReturn(tableNote)
        Mockito.`when`(db.tablePictureCollection).thenReturn(tablePictureCollection)
        Mockito.`when`(db.tableEquipment).thenReturn(tableEquipment)
        prefHelper.clearCurProject()
    }

    @Test
    fun verifyProjectID() {
        Mockito.`when`(db.tableProjects).thenReturn(tableProjects)
        Mockito.`when`(tableProjects.queryProjectName(anyLong())).thenReturn(Pair(ROOT_NAME, SUB_NAME))
        Mockito.`when`(tableProjects.queryProjectId(ROOT_NAME, SUB_NAME)).thenReturn(PROJECT_ID)

        prefHelper.projectRootName = ROOT_NAME
        assertEquals(PROJECT_ID, prefHelper.projectId)
        prefHelper.projectRootName = null
        assertEquals(null, prefHelper.projectId)
        prefHelper.projectRootName = "whatever"
        assertEquals(null, prefHelper.projectId)

        prefHelper.projectRootName = ROOT_NAME
        prefHelper.projectSubName = null
        assertEquals(null, prefHelper.projectId)
        prefHelper.projectSubName = "whatever"
        assertEquals(null, prefHelper.projectId)
    }

    @Test
    fun verifyCurrentProjectGroup() {
        Mockito.`when`(db.tableProjects).thenReturn(tableProjects)
        Mockito.`when`(tableProjects.queryProjectId(ROOT_NAME, SUB_NAME)).thenReturn(PROJECT_ID)
        Mockito.`when`(db.tableAddress).thenReturn(tableAddress)
        val address = DataAddress("Company", "Street", "City", "IL", "60626")
        Mockito.`when`(tableAddress.query(10)).thenReturn(address)
        Mockito.`when`(db.tableProjectAddressCombo).thenReturn(tableProjectAddressCombo)
        val projectGroup = DataProjectAddressCombo(db, 2, 5, 10)
        Mockito.`when`(tableProjectAddressCombo.query(2)).thenReturn(projectGroup)

        prefHelper = PrefHelper(context, db)
        prefHelper.currentProjectGroup = projectGroup
        assertEquals("Street", prefHelper.currentProjectGroup?.address?.street)
        assertEquals("Company\nStreet\nCity, IL 60626", prefHelper.address)
    }

    @Test
    fun verifyTruckValue() {
        prefHelper.truckNumber = "123"
        prefHelper.licensePlate = "ABC"
        assertEquals("123 : ABC", prefHelper.truckValue)

        prefHelper.truckNumber = null
        prefHelper.licensePlate = "ABC"
        assertEquals("ABC", prefHelper.truckValue)

        prefHelper.truckNumber = ""
        prefHelper.licensePlate = "ABC"
        assertEquals("ABC", prefHelper.truckValue)

        prefHelper.truckNumber = "123"
        prefHelper.licensePlate = ""
        assertEquals("123", prefHelper.truckValue)

        prefHelper.truckNumber = "123"
        prefHelper.licensePlate = null
        assertEquals("123", prefHelper.truckValue)
    }

    @Test
    fun verifySaveProjectAndAddressComboNoModifyCurrentNoAddress() {
        assertFalse(prefHelper.saveProjectAndAddressCombo(false))
        prefHelper.projectRootName = ROOT_NAME
        prefHelper.projectSubName = SUB_NAME
        assertFalse(prefHelper.saveProjectAndAddressCombo(false))
        val address = DataAddress("Company", "Street", "City", "IL", "60626")
        prefHelper.company = address.company
        assertFalse(prefHelper.saveProjectAndAddressCombo(false))
        prefHelper.state = address.state
        prefHelper.street = address.street
        prefHelper.city = address.city
        prefHelper.zipCode = address.zipcode

        Mockito.`when`(db.tableProjects).thenReturn(tableProjects)
        Mockito.`when`(db.tableAddress).thenReturn(tableAddress)
        Mockito.`when`(tableAddress.queryAddressId(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(-1)
        Mockito.`when`(tableProjects.queryProjectId(ROOT_NAME, SUB_NAME)).thenReturn(-1)

        assertFalse(prefHelper.saveProjectAndAddressCombo(false))
        Mockito.verify(tableAddress).add(any<DataAddress>())
        prefHelper.clearCurProject()
    }

    @Test
    fun verifySaveProjectAndAddressComboNoModifyCurrentWithAddressNoGroup() {
        prefHelper.projectRootName = ROOT_NAME
        prefHelper.projectSubName = SUB_NAME
        val address = DataAddress("Company", "Street", "City", "IL", "60626")
        prefHelper.company = address.company
        prefHelper.state = address.state
        prefHelper.street = address.street
        prefHelper.city = address.city
        prefHelper.zipCode = address.zipcode

        Mockito.`when`(db.tableProjects).thenReturn(tableProjects)
        Mockito.`when`(db.tableAddress).thenReturn(tableAddress)
        Mockito.`when`(db.tableProjectAddressCombo).thenReturn(tableProjectAddressCombo)
        Mockito.`when`(tableAddress.queryAddressId(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(5)
        Mockito.`when`(tableProjects.queryProjectId(ROOT_NAME, SUB_NAME)).thenReturn(PROJECT_ID)
        Mockito.`when`(db.tableProjectAddressCombo.queryProjectGroupId(PROJECT_ID, 5)).thenReturn(-1)

        assertTrue(prefHelper.saveProjectAndAddressCombo(false))
        Mockito.verify(tableAddress, never()).add(any<DataAddress>())
        Mockito.verify(tableProjectAddressCombo).add(any())
        prefHelper.clearCurProject()
    }

    @Test
    fun verifySaveProjectAndAddressComboNoModifyCurrentWithAddressAndGroup() {
        prefHelper.projectRootName = ROOT_NAME
        prefHelper.projectSubName = SUB_NAME
        val address = DataAddress("Company", "Street", "City", "IL", "60626")
        prefHelper.company = address.company
        prefHelper.state = address.state
        prefHelper.street = address.street
        prefHelper.city = address.city
        prefHelper.zipCode = address.zipcode

        Mockito.`when`(db.tableProjects).thenReturn(tableProjects)
        Mockito.`when`(db.tableAddress).thenReturn(tableAddress)
        Mockito.`when`(db.tableProjectAddressCombo).thenReturn(tableProjectAddressCombo)
        Mockito.`when`(tableAddress.queryAddressId(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(5)
        Mockito.`when`(tableProjects.queryProjectId(ROOT_NAME, SUB_NAME)).thenReturn(PROJECT_ID)
        Mockito.`when`(db.tableProjectAddressCombo.queryProjectGroupId(PROJECT_ID, 5)).thenReturn(3)

        assertTrue(prefHelper.saveProjectAndAddressCombo(false))
        Mockito.verify(tableProjectAddressCombo, never()).add(any())
        Mockito.verify(tableProjectAddressCombo).updateUsed(3)
        prefHelper.clearCurProject()
    }

    @Test
    fun verifySaveProjectAndAddressComboModifyCurrentNoProjectGroup() {
        prefHelper.projectRootName = ROOT_NAME
        prefHelper.projectSubName = SUB_NAME
        val address = DataAddress("Company", "Street", "City", "IL", "60626")
        prefHelper.company = address.company
        prefHelper.state = address.state
        prefHelper.street = address.street
        prefHelper.city = address.city
        prefHelper.zipCode = address.zipcode
        prefHelper.currentProjectGroupId = -1

        Mockito.`when`(db.tableProjects).thenReturn(tableProjects)
        Mockito.`when`(db.tableAddress).thenReturn(tableAddress)
        Mockito.`when`(db.tableProjectAddressCombo).thenReturn(tableProjectAddressCombo)
        Mockito.`when`(tableAddress.queryAddressId(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(5)
        Mockito.`when`(tableProjects.queryProjectId(ROOT_NAME, SUB_NAME)).thenReturn(PROJECT_ID)

        assertFalse(prefHelper.saveProjectAndAddressCombo(true))
        prefHelper.clearCurProject()
    }

    @Test
    fun verifySaveProjectAndAddressComboModifyCurrentWithProjectGroupFailedSave() {
        prefHelper.projectRootName = ROOT_NAME
        prefHelper.projectSubName = SUB_NAME
        val address = DataAddress("Company", "Street", "City", "IL", "60626")
        prefHelper.company = address.company
        prefHelper.state = address.state
        prefHelper.street = address.street
        prefHelper.city = address.city
        prefHelper.zipCode = address.zipcode
        prefHelper.currentProjectGroupId = 10

        val combo = DataProjectAddressCombo(db, 10, 4, 5)

        Mockito.`when`(db.tableProjects).thenReturn(tableProjects)
        Mockito.`when`(db.tableAddress).thenReturn(tableAddress)
        Mockito.`when`(db.tableProjectAddressCombo).thenReturn(tableProjectAddressCombo)
        Mockito.`when`(tableAddress.queryAddressId(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(combo.addressId)
        Mockito.`when`(tableProjects.queryProjectId(anyString(), anyString())).thenReturn(combo.projectNameId)
        Mockito.`when`(db.tableProjectAddressCombo.query(combo.id)).thenReturn(combo)
        Mockito.`when`(db.tableProjectAddressCombo.save(combo)).thenReturn(false)

        assertFalse(prefHelper.saveProjectAndAddressCombo(true))
        prefHelper.clearCurProject()
    }


    @Test
    fun verifySaveProjectAndAddressComboModifyCurrentWithProjectGroup() {
        prefHelper.projectRootName = ROOT_NAME
        prefHelper.projectSubName = SUB_NAME
        val address = DataAddress("Company", "Street", "City", "IL", "60626")
        prefHelper.company = address.company
        prefHelper.state = address.state
        prefHelper.street = address.street
        prefHelper.city = address.city
        prefHelper.zipCode = address.zipcode
        prefHelper.currentProjectGroupId = 10

        val combo = DataProjectAddressCombo(db, 10, 4, 5)

        Mockito.`when`(db.tableProjects).thenReturn(tableProjects)
        Mockito.`when`(db.tableAddress).thenReturn(tableAddress)
        Mockito.`when`(db.tableEntry).thenReturn(tableEntry)
        Mockito.`when`(db.tableProjectAddressCombo).thenReturn(tableProjectAddressCombo)
        Mockito.`when`(tableAddress.queryAddressId(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(combo.addressId)
        Mockito.`when`(tableProjects.queryProjectId(anyString(), anyString())).thenReturn(combo.projectNameId)
        Mockito.`when`(db.tableProjectAddressCombo.query(combo.id)).thenReturn(combo)
        Mockito.`when`(db.tableProjectAddressCombo.save(combo)).thenReturn(true)

        assertTrue(prefHelper.saveProjectAndAddressCombo(true))

        Mockito.verify(tableProjectAddressCombo).updateUsed(combo.id)
        prefHelper.clearCurProject()
    }
}