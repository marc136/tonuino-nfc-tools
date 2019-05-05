package com.example.myapplication.test

import org.junit.Assert


@ExperimentalUnsignedTypes
fun assertEquals(expected: Int, actual: UByte) {
    return Assert.assertEquals(expected.toUByte(), actual)
}
