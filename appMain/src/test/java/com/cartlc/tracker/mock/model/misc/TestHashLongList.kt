/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.mock.model.misc

import com.cartlc.tracker.model.data.DataString
import com.cartlc.tracker.model.misc.HashLongList
import com.cartlc.tracker.model.table.TableString
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TestHashLongList {

    @Mock
    lateinit var db: TableString

    @Before
    fun onBefore() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `verify add and set`() {
        for (i in 1..10) {
            Mockito.`when`(db.add("Sample$i")).thenReturn(i.toLong())
        }
        val hashLongList = HashLongList(db)
        hashLongList.add("Sample1")
        hashLongList.add("Sample2")
        hashLongList.set("Sample3", true)
        hashLongList.add("Sample4")
        hashLongList.set("Sample4", false)

        assertTrue(hashLongList.has("Sample1"))
        assertTrue(hashLongList.has("Sample2"))
        assertTrue(hashLongList.has("Sample3"))
        assertFalse(hashLongList.has("Sample4"))
        assertFalse(hashLongList.has("Sample5"))
    }

    @Test
    fun `verify expand`() {
        val hashLongList = HashLongList(db)
        for (i in 1..10) {
            Mockito.`when`(db.add("Sample$i")).thenReturn(i.toLong())
            Mockito.`when`(db.query(i.toLong())).thenReturn(DataString(i.toLong(), "Sample$i"))
            hashLongList.add("Sample$i")
        }
        val result = hashLongList.expand()
        for (i in 1..10) {
            assertTrue(result.contains("Sample$i"))
        }
    }

    @Test
    fun `verify mash`() {
        var expected = ""
        var comma = false
        val hashLongList = HashLongList(db)
        for (i in 1..10) {
            if (comma) {
                expected += ","
            } else {
                comma = true
            }
            Mockito.`when`(db.add("Sample$i")).thenReturn(i.toLong())
            hashLongList.add("Sample$i")
            expected += "$i"
        }
        assertEquals(expected, hashLongList.mash())
    }

    @Test
    fun `verify unmash`() {
        val hashLongList = HashLongList(db)
        for (i in 1..10) {
            Mockito.`when`(db.add("Sample$i")).thenReturn(i.toLong())
            hashLongList.add("Sample$i")
        }
        val unmashedList = HashLongList(db, hashLongList.mash())
        assertEquals(10, unmashedList.size)
        for (i in 1..10) {
            assertTrue(unmashedList.has("Sample$i"))
        }
    }

    @Test
    fun `verify server mash`() {
        val hashLongList = HashLongList(db)
        var expected = ""
        var comma = false
        for (i in 1..10) {
            Mockito.`when`(db.add("Sample$i")).thenReturn(i.toLong())
            Mockito.`when`(db.query(i.toLong())).thenReturn(DataString(i.toLong(), "Sample$i", 100+i.toLong()))
            hashLongList.add("Sample$i")
            if (comma) {
                expected += ","
            } else {
                comma = true
            }
            expected += (100+i).toLong().toString()
        }
        val mashed = hashLongList.serverMash()
        assertEquals(expected, mashed)
    }

    @Test
    fun `verify server mash strings with missing server id`() {
        val hashLongList = HashLongList(db)
        var expected = ""
        var comma = false
        for (i in 1..5) {
            val text = "Sample%02d".format(i)
            Mockito.`when`(db.add(text)).thenReturn(i.toLong())
            Mockito.`when`(db.query(i.toLong())).thenReturn(DataString(i.toLong(), text, 100+i.toLong()))
            hashLongList.add(text)
            if (comma) {
                expected += ","
            } else {
                comma = true
            }
            expected += text
        }
        for (i in 7..10) {
            val text = "Sample%02d".format(i)
            Mockito.`when`(db.add(text)).thenReturn(i.toLong())
            Mockito.`when`(db.query(i.toLong())).thenReturn(DataString(i.toLong(), text))
            hashLongList.add(text)
            expected += ","
            expected += text
        }
        val mashed = hashLongList.serverMash()
        assertEquals(expected, mashed)
    }

}