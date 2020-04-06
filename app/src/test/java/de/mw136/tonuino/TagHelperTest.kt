package de.mw136.tonuino

import de.mw136.tonuino.nfc.dropTrailingZeros
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
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
}
