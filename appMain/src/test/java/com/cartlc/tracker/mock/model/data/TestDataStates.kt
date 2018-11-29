/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.mock.model.data

import com.cartlc.tracker.model.data.DataStates
import com.cartlc.tracker.model.data.DataTruck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TestDataStates {

    @Before
    fun onBefore() {
    }

    @Test
    fun `verify number of states`() {
        assertEquals(50, DataStates.STATES.size)
    }

    @Test
    fun `verify unused states`() {
        val used = listOf("CA", "IL", "Wyoming")
        val unused = DataStates.getUnusedStates(used)
        assertEquals(47, unused.size)
    }

    @Test
    fun `verify operator`() {
        assertEquals("IL", DataStates["IL"]?.abbr)
        assertEquals("IL", DataStates["Illinois"]?.abbr)
        assertEquals("CA", DataStates["california"]?.abbr)
    }

    @Test
    fun `verify getAbbr`() {
        assertEquals("WA", DataStates.getAbbr("washington"))
        assertEquals("IL", DataStates.getAbbr("Illinois"))
        assertEquals("CA", DataStates.getAbbr("ca"))
    }

}