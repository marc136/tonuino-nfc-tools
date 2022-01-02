package de.mw136.tonuino.test

import org.junit.Assert


fun assertEquals(expected: Int, actual: UByte) {
    return Assert.assertEquals(expected.toUByte(), actual)
}
