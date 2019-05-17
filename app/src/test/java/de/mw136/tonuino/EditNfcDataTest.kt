package de.mw136.tonuino

import de.mw136.tonuino.nfc.EditNfcData
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.WhichByte
import de.mw136.tonuino.test.*
import de.mw136.tonuino.ui.edit.EditFragment
import org.junit.Assert.*
import org.junit.Test

@ExperimentalUnsignedTypes
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
    override val fragments: Array<EditFragment> = arrayOf(SimpleEditFragment())

    constructor() : this(ubyteArrayOf())

    constructor(bytes: UByteArray) {
        tagData = TagData(bytes)
    }
}

class SimpleEditFragment : EditFragment() {
    override fun refreshDescriptions(data: TagData) {}
    override fun refreshInputs(data: TagData) {}
}
