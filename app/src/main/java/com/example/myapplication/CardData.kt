package com.example.myapplication

import android.util.Log

@ExperimentalUnsignedTypes
private val TonuinoCookie = ubyteArrayOf(1u, 2u, 3u, 4u, 5u) // TODO set correct cookie

@ExperimentalUnsignedTypes
class CardData(
    var cookie: UByteArray = TonuinoCookie,
    var version: UByte = 0x01.toUByte(),
    val folderSettings: NfcFolderSettings = NfcFolderSettings()
)

@ExperimentalUnsignedTypes
interface EditNfcData {
    var bytes: UByteArray
    var currentEditFragment: EditFragment? // TODO remove if not needed
    var triggerRefreshTextOnCurrentFragment: Boolean // TODO remove if not needed
    val fragments: Array<EditFragment>

    fun setByte(which: WhichByte, value: UByte) {
        if (bytes[which.ordinal] != value) {
            bytes[which.ordinal] = value
            fragments.forEach {fragment ->
                if (fragment.isVisible) {
                    fragment.refreshDescriptions(this)
                }
            }
        }
    }
}

enum class WhichByte { FOLDER, MODE, SPECIAL, SPECIAL2 }

enum class BytePositions { FOLDER, MODE, SPECIAL, SPECIAL2 }
