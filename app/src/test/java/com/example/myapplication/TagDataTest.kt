package com.example.myapplication

import android.nfc.tech.MifareClassic
import com.example.myapplication.test.*
import org.junit.Assert.*
import org.junit.Test


@ExperimentalUnsignedTypes
class TagDataTest {
    @Test
    fun empty() {
        val data = TagData(ubyteArrayOf(1u))
        assertEquals(1, data.getAtWithDefault(0))
        assertEquals(1, data.bytes.size)

        // should return 0x0 if index is out of bounds
        assertEquals(0, data.getAtWithDefault(1))
    }

    @Test
    fun genericGetters() {
        val data = TagData(byteArrayOf(0, 1, 2, 3, 4).toUByteArray())

        assertEquals(0, data.getAtWithDefault(0))
        assertEquals(1, data.getAtWithDefault(1))
        assertEquals(4, data.getAtWithDefault(4))

        // should return 0x0 if index is out of bounds
        assertEquals(0, data.getAtWithDefault(5))
        assertEquals(0, data.getAtWithDefault(500))
    }

    @Test
    fun tonuinoGetters() {
        val data = TagData(byteArrayOf(0, 0, 0, 0, 4, 5, 6, 7, 8).toUByteArray())
        assertEquals(4, data.version)
        assertEquals(5, data.folder)
        assertEquals(6, data.mode)
        assertEquals(7, data.special)
        assertEquals(8, data.special2)
    }

    @Test
    fun toFixedLengthBuffer() {
        // this logic is used to ensure that a block written with MifareClassic  has the correct size
        val input = TagData(byteArrayOf(42).toUByteArray())
        val actual = input.toFixedLengthBuffer(MifareClassic.BLOCK_SIZE)

        assertEquals(MifareClassic.BLOCK_SIZE, actual.size)

        // should have copied existing value
        assertEquals(42.toByte(), actual[0])
        // the rest should be filled with 0x0
        actual.toList().drop(1).forEach { value -> assertEquals(0.toByte(), value) }
    }
}

