/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.mock.model.data

import com.cartlc.tracker.fresh.model.core.data.DataTruck
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TestDataTruck {

    companion object {
        const val TRUCK_NUMBER = "12345"
        const val PROJECT_NAME_ID = 44L
        const val COMPANY_NAME = "Welmont"
        const val LICENSE_PLATE = "FR112"

        const val TRUCK_NUMBER2 = "1234"
        const val PROJECT_NAME_ID2 = 4L
        const val COMPANY_NAME2 = "Welmonty"
        const val LICENSE_PLATE2 = "FR113"
    }

    @Before
    fun onBefore() {
    }

    @Test
    fun `equality with truck number, project id, company name, and license plate`() {
        val truck1 = DataTruck(0, TRUCK_NUMBER, LICENSE_PLATE, PROJECT_NAME_ID, COMPANY_NAME)
        val truck2 = DataTruck(1, TRUCK_NUMBER, LICENSE_PLATE, PROJECT_NAME_ID, COMPANY_NAME)
        assertTrue(truck1 == truck2)
    }

    @Test
    fun `inequality with truck number, project id, company name, and license plate`() {
        val truck1 = DataTruck(0, TRUCK_NUMBER, LICENSE_PLATE, PROJECT_NAME_ID, COMPANY_NAME)
        val truck2 = DataTruck(1, TRUCK_NUMBER2, LICENSE_PLATE2, PROJECT_NAME_ID2, COMPANY_NAME2)
        assertTrue(truck1 != truck2)
    }

    @Test
    fun `inequality with truck number, project id, company name`() {
        val truck1 = DataTruck(0, TRUCK_NUMBER, LICENSE_PLATE, PROJECT_NAME_ID, COMPANY_NAME)
        val truck2 = DataTruck(1, TRUCK_NUMBER2, LICENSE_PLATE, PROJECT_NAME_ID2, COMPANY_NAME2)
        assertTrue(truck1 != truck2)
    }

    @Test
    fun `inequality with truck number, project id`() {
        val truck1 = DataTruck(0, TRUCK_NUMBER, LICENSE_PLATE, PROJECT_NAME_ID, COMPANY_NAME)
        val truck2 = DataTruck(1, TRUCK_NUMBER2, LICENSE_PLATE, PROJECT_NAME_ID2, COMPANY_NAME)
        assertTrue(truck1 != truck2)
    }

    @Test
    fun `inequality with truck number`() {
        val truck1 = DataTruck(0, TRUCK_NUMBER, LICENSE_PLATE, PROJECT_NAME_ID, COMPANY_NAME)
        val truck2 = DataTruck(1, TRUCK_NUMBER2, LICENSE_PLATE, PROJECT_NAME_ID, COMPANY_NAME)
        assertTrue(truck1 != truck2)
    }

    @Test
    fun `equality with just ID`() {
        val truck1 = DataTruck(1L, TRUCK_NUMBER, LICENSE_PLATE, PROJECT_NAME_ID, COMPANY_NAME)
        assertTrue(truck1.equals(1L))
    }
}