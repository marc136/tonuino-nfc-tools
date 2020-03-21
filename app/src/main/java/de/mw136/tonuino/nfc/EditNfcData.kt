package de.mw136.tonuino.nfc

import de.mw136.tonuino.ui.edit.EditFragment

@ExperimentalUnsignedTypes
interface EditNfcData {
    var tagData: TagData
    val fragments: Array<EditFragment>

    fun setByte(which: WhichByte, value: UByte, fullRefresh: Boolean = false) {
        val diff = which.index - tagData.bytes.lastIndex
        if (diff > 0) {
            // increase buffer size
            tagData.bytes += UByteArray(diff) { 0u }
        }
        if (tagData.bytes[which.index] != value) {
//            Log.w("Tag.setByte", "${which.name} ${value.toString()}")
            tagData.bytes[which.index] = value
            if (fullRefresh) return

            fragments.forEach { fragment ->
                if (fragment.isVisible) {
                    if (fullRefresh) {
                        fragment.refreshUi(tagData)
                    } else {
                        fragment.refreshDescriptions(tagData)
                    }
                }
            }
        }
    }
}

enum class WhichByte(val index: Int) { VERSION(4), FOLDER(5), MODE(6), SPECIAL(7), SPECIAL2(8) }
