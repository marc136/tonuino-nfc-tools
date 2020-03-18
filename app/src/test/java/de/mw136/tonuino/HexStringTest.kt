package de.mw136.tonuino

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
        assertArrayEquals(
            ubyteArrayOf(0x12U, 0xAFU).toByteArray(),
            hexToBytes("12AF").toByteArray()
        )

        assertArrayEquals(
            "Should return an empty array if invalid number format is used",
            ubyteArrayOf().toByteArray(),
            hexToBytes("q1").toByteArray()
        )
    }

    @Test
    fun hexToBytesRoundTrip() {
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
