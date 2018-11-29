/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.mock.model.data

import com.cartlc.tracker.model.data.DataAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TestDataAddress {

    companion object {
        const val COMPANY = "Company"
        const val STREET = "1555 W. Pratt Blvd"
        const val CITY = "Chicago"
        const val STATE = "Illinois"
        const val ZIPCODE = "60626"
    }

    @Before
    fun onBefore() {
    }

    @Test
    fun `block with just company`() {
        val dataAddress = DataAddress(COMPANY)
        val block = dataAddress.block
        assertEquals(COMPANY, block)
    }

    @Test
    fun `block with just company and street`() {
        val dataAddress = DataAddress(COMPANY, STREET, "", "", "")
        val block = dataAddress.block
        assertEquals(COMPANY, block)
    }

    @Test
    fun `block with just company, street and city`() {
        val dataAddress = DataAddress(COMPANY, STREET, CITY, "", "")
        val block = dataAddress.block
        assertEquals(COMPANY, block)
    }

    @Test
    fun `block with company, street and city & state`() {
        val dataAddress = DataAddress(COMPANY, STREET, CITY, STATE, "")
        val block = dataAddress.block
        assertTrue(block.contains(COMPANY))
        assertTrue(block.contains(STREET))
        assertTrue(block.contains(CITY))
        assertTrue(block.contains(STATE))
    }

    @Test
    fun `block with company, street and city, state && zipcode`() {
        val dataAddress = DataAddress(COMPANY, STREET, CITY, STATE, ZIPCODE)
        val block = dataAddress.block
        assertTrue(block.contains(COMPANY))
        assertTrue(block.contains(STREET))
        assertTrue(block.contains(CITY))
        assertTrue(block.contains(STATE))
        assertTrue(block.contains(ZIPCODE))
    }

    @Test
    fun `reversed city & state specification`() {
        val dataAddress = DataAddress(COMPANY, STREET, STATE, CITY, "")
        dataAddress.fix()
        assertEquals(CITY, dataAddress.city)
        assertEquals(STATE, dataAddress.state)
    }

    @Test
    fun `simple equality`() {
        val dataAddress = DataAddress(1, 1, COMPANY, STREET, CITY, STATE, ZIPCODE)
        val dataAddress2 = DataAddress(0, 0, COMPANY, STREET, CITY, STATE, ZIPCODE)
        assertTrue(dataAddress == dataAddress2)
    }

    @Test
    fun `ID equality`() {
        val dataAddress = DataAddress(1, 1, COMPANY, STREET, CITY, STATE, ZIPCODE)
        assertTrue(dataAddress.equals(1L))
    }

}