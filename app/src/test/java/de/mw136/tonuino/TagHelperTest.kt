package de.mw136.tonuino

import android.os.Parcel
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.dropTrailingZeros
import org.junit.Assert.*
import org.junit.Test


@ExperimentalUnsignedTypes
class TagHelperTest {
    @Test
    fun dropTrailingZeros1() {
        val expected = byteArrayOf(0, 0, 0, 1, 0, 0, 17).toUByteArray()
        val input = expected.copyOf() + UByteArray(4)

        val actual = dropTrailingZeros(input)
        assert(input.size > actual.size) { "Should have dropped trailing zeros" }
        assertArrayEquals(expected.toByteArray(), actual.toByteArray())
        assertEquals(
            "Last deleted element should be 0, but was ${input[expected.size]}",
            0.toUByte(),
            input[expected.size]
        )
    }

    @Test
    fun checkAccessors() {
        val data = TagData(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8).toUByteArray())

        assertArrayEquals(byteArrayOf(0, 1, 2, 3), data.cookie.toByteArray())
        assertEquals(4.toUByte(), data.version)
        assertEquals(5.toUByte(), data.folder)
        assertEquals(6.toUByte(), data.mode)
        assertEquals(7.toUByte(), data.special)
        assertEquals(8.toUByte(), data.special2)
    }
}
