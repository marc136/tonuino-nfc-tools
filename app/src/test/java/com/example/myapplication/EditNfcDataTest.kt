package com.example.myapplication

import com.example.myapplication.test.*
import org.junit.Assert.*
import org.junit.Test

class EditNfcDataTest {
    @Test
    fun increaseByteArray() {
        val data = Simple()

        val before = data.tagData.bytes.size
        assert(before < WhichByte.FOLDER.index) { "Size should be smaller than ${WhichByte.FOLDER.index}, but was $before" }

        data.setByte(WhichByte.FOLDER, 23u)

        // value should be set
        assertEquals(23, data.tagData.bytes[WhichByte.FOLDER.index])
        assertEquals(23, data.tagData.folder)

        // should only increase the size as needed
        assertEquals(data.tagData.bytes.lastIndex, WhichByte.FOLDER.index)

        // other values should be filled with 0x0
        data.tagData.bytes.toList().dropLast(1).forEach { value -> assertEquals(0.toUByte(), value) }
    }

    @Test
    fun increaseByteArrayTwice() {
        val data = Simple().apply {
            setByte(WhichByte.FOLDER, 23u)
            setByte(WhichByte.SPECIAL2, 42u)
        }

        // value should be set
        assertEquals(23, data.tagData.bytes[WhichByte.FOLDER.index])
        assertEquals(23, data.tagData.folder)
        assertEquals(42, data.tagData.bytes[WhichByte.SPECIAL2.index])
        assertEquals(42, data.tagData.special2)
        // value between should be 0x0
        assertEquals(0.toUByte(), data.tagData.bytes[WhichByte.SPECIAL.index])

        // should only increase the size as needed
        assertEquals(data.tagData.bytes.lastIndex, WhichByte.SPECIAL2.index)
    }
}

@ExperimentalUnsignedTypes
class Simple : EditNfcData {
    override var tagData: TagData
    override var currentEditFragment: EditFragment? = null
    override var triggerRefreshTextOnCurrentFragment: Boolean = false
    override val fragments: Array<EditFragment> = arrayOf()

    constructor() : this(ubyteArrayOf())

    constructor(bytes: UByteArray) {
        tagData = TagData(bytes)
    }
}
