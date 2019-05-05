package com.example.myapplication

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@ExperimentalUnsignedTypes
class HexStringTest {
    @Test
    fun hexToBytes() {
        transformHex("")
        transformHex("FF")
        transformHex("FF4", "FF04")
        transformHex("123aB2e0", "123AB2E0")
    }

    private fun transformHex(input: String, expected: String? = null) {
        val expected_ = expected ?: input
        val asBytes = hexToBytes(input)
        val asString = byteArrayToHex(asBytes)
        assertEquals(expected_, asString.joinToString(""))
    }
}
