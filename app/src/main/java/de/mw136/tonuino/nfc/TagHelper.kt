package de.mw136.tonuino.nfc

import android.nfc.FormatException
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.TagTechnology
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.hexToBytes
import java.io.IOException

private const val TAG = "TagHelper"
private const val tonuinoSector = 1

@ExperimentalUnsignedTypes
private val tonuinoCookie = hexToBytes("1337b347").toList() // TODO add to expert settings

@ExperimentalUnsignedTypes
private val factoryKey =
    hexToBytes("FFFFFFFFFFFF") // factory preset, same as MifareClassic.KEY_DEFAULT


@ExperimentalUnsignedTypes
fun tagIdAsString(tag: TagTechnology) = tagIdAsString(tag.tag)

@ExperimentalUnsignedTypes
fun tagIdAsString(tag: Tag): String {
    return byteArrayToHex(tag.id.toUByteArray()).joinToString(":")
}

@ExperimentalUnsignedTypes
class TagData(var bytes: UByteArray = ubyteArrayOf()) : Parcelable {
    private val versionIndex = 4 // TODO remove magic byte index number

    val cookie: UByteArray
        get() {
            if (bytes.size < versionIndex) {
                return ubyteArrayOf(0u, 0u, 0u, 0u)
            } else {
                return bytes.sliceArray(0 until versionIndex)
            }
        }
    val version: UByte
        get() = getAtWithDefault(versionIndex)
    val folder: UByte
        get() = getAtWithDefault(versionIndex + 1)
    val mode: UByte
        get() = getAtWithDefault(versionIndex + 2)
    val special: UByte
        get() = getAtWithDefault(versionIndex + 3)
    val special2: UByte
        get() = getAtWithDefault(versionIndex + 4)

    fun getAtWithDefault(index: Int, default: UByte = 0u): UByte {
        return bytes.elementAtOrElse(index) { default }
    }

    fun isModifierTag(): Boolean {
        return version == 2u.toUByte() && folder == 0u.toUByte()
    }

    constructor(parcel: Parcel) : this() {
        bytes = parcel.createByteArray()?.toUByteArray() ?: ubyteArrayOf()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(bytes.toByteArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TagData> {
        override fun createFromParcel(parcel: Parcel): TagData {
            return TagData(parcel)
        }

        override fun newArray(size: Int): Array<TagData?> {
            return arrayOfNulls(size)
        }

        fun createDefault(): TagData {
            val versionIndex = tonuinoCookie.size
            val buffer = UByteArray(versionIndex + 3) { 0u }
            tonuinoCookie.forEachIndexed { index, value -> buffer[index] = value }
            buffer[versionIndex] = 1u // version
            buffer[versionIndex + 1] = 1u // folder
            buffer[versionIndex + 2] = 1u // mode
            return TagData(buffer)
        }
    }

    override fun toString(): String {
        return "TagData<${byteArrayToHex(bytes).joinToString(" ")}>"
    }

    /**
     * Be aware that this might truncate data!
     */
    fun toFixedLengthBuffer(size: Int): ByteArray {
        val block = UByteArray(size) { 0u }
        bytes.forEachIndexed { index, value -> block[index] = value }
        return block.toByteArray()
    }
}

fun connectTo(tag: Tag): TagTechnology? {
    if (tag.techList.contains(MifareClassic::class.java.name)) {
        return MifareClassic.get(tag)?.apply { connect() }
    } else if (tag.techList.contains(MifareUltralight::class.java.name)) {
        return MifareUltralight.get(tag)?.apply { connect() }
    } else {
        throw FormatException("Can only handle MifareClassic and MifareUltralight")
    }
}

@ExperimentalUnsignedTypes
fun readFromTag(tag: Tag): UByteArray {
    val id = tagIdAsString(tag)
    var result = ubyteArrayOf()

    try {
        Log.i(TAG, "Tag $id techList: ${techListOf(tag).joinToString(", ")}")
        if (tag.techList.contains(MifareClassic::class.java.name)) {
            MifareClassic.get(tag)?.use { mifare -> result = readFromTag(mifare) }
        } else if (tag.techList.contains(MifareUltralight::class.java.name)) {
            MifareUltralight.get(tag)?.use { mifare -> result = readFromTag(mifare) }
        } else {
            Log.e(
                "$TAG.readFromTag",
                "Tag ${id} did not enumerate MifareClassic or MifareUltralight and is thus not supported"
            )
        }
    } catch (ex: Exception) {
        // e.g. android.nfc.TagLostException, IOException
        Log.e("$TAG.readFromTag", ex.toString())
    }

    return dropTrailingZeros(result)
}

@ExperimentalUnsignedTypes
fun dropTrailingZeros(bytes: UByteArray): UByteArray {
    if (bytes.size == 0) return bytes

    val lastNonZeroIndex = bytes.indexOfLast { value -> value > 0u }
    if (lastNonZeroIndex == 0) return ubyteArrayOf()

    return bytes.sliceArray(0..lastNonZeroIndex)
}

/**
 * Different MIFARE Classic formats:
 * MIFARE Classic Mini are 320 bytes (SIZE_MINI), with 5 sectors each of 4 blocks.
 * MIFARE Classic 1k are 1024 bytes (SIZE_1K), with 16 sectors each of 4 blocks.
 * MIFARE Classic 2k are 2048 bytes (SIZE_2K), with 32 sectors each of 4 blocks.
 * MIFARE Classic 4k are 4096 bytes (SIZE_4K). The first 32 sectors contain 4 blocks and the last 8 sectors contain 16 blocks.
 *
 * Source: https://developer.android.com/reference/android/nfc/tech/MifareClassic.html
 */
@ExperimentalUnsignedTypes
fun readFromTag(mifare: MifareClassic): UByteArray {
    if (!mifare.isConnected) mifare.connect()
    var result = ubyteArrayOf()

    val key = factoryKey.asByteArray()
    if (mifare.authenticateSectorWithKeyA(tonuinoSector, key)) {
        val blockIndex = mifare.sectorToBlock(tonuinoSector)
        val block = mifare.readBlock(blockIndex).toUByteArray()

        Log.w(TAG, "Bytes in sector: ${byteArrayToHex(block).joinToString(" ")}")

        // first 4 byte should match the tonuinoCookie
        if (block.take(tonuinoCookie.size) == tonuinoCookie) {
            Log.i(TAG, "This is a Tonuino MifareClassic tag")
        }

        result = block
    } else {
        Log.e(TAG, "Authentication of sector $tonuinoSector failed!")
    }

    mifare.close()
    return result
}

/**
 * Different MIFARE Ultralight formats, page size is 4 byte
 * MIFARE Ultralight are 64 bytes, final 12 pages may be written to
 * MIFARE Ultralight C are 192 bytes, first 8 and last 4 pages are not available
 *
 * Source: https://developer.android.com/reference/android/nfc/tech/MifareUltralight
 */
@ExperimentalUnsignedTypes
fun readFromTag(mifare: MifareUltralight): UByteArray {
    if (!mifare.isConnected) mifare.connect()
    var result: UByteArray

    val type_ = when (mifare.type) {
        MifareUltralight.TYPE_ULTRALIGHT -> "ULTRALIGHT"
        MifareUltralight.TYPE_ULTRALIGHT_C -> "ULTRALIGHT_C"
        else -> "ULTRALIGHT (UNKNOWN)"
    }

    // tonuinoCookie should be in page 8
    val block = mifare.readPages(8).toUByteArray()
    // first 4 byte should match the tonuinoCookie
    if (block.take(tonuinoCookie.size) == tonuinoCookie) {
        Log.i(TAG, "This is a Tonuino MIFARE $type_ tag")
    }

    Log.i(TAG, "Bytes in sector: ${byteArrayToHex(block).joinToString(" ")}")

    result = block

    mifare.close()
    return result
}


enum class WriteResult { SUCCESS, UNSUPPORTED_FORMAT, AUTHENTICATION_FAILURE, TAG_UNAVAILABLE, UNKNOWN_ERROR }

@ExperimentalUnsignedTypes
fun writeTonuino(tag: TagTechnology, data: TagData): WriteResult {
    var result: WriteResult

    try {
        result = when (tag) {
            is MifareClassic -> writeTag(tag, data)
            is MifareUltralight -> writeTag(tag, data)
            else -> WriteResult.UNSUPPORTED_FORMAT
        }
    } catch (ex: TagLostException) {
        result = WriteResult.TAG_UNAVAILABLE
    } catch (ex: FormatException) {
        result = WriteResult.UNSUPPORTED_FORMAT
    } catch (ex: Exception) {
        result = WriteResult.UNKNOWN_ERROR
    }

    return result
}

@ExperimentalUnsignedTypes
fun writeTag(mifare: MifareClassic, data: TagData): WriteResult {
    val result: WriteResult
    try {
        if (!mifare.isConnected) mifare.connect()
    } catch (ex: IOException) {
        // is e.g. thrown if the NFC tag was removed
        return WriteResult.TAG_UNAVAILABLE
    }

    val key = factoryKey.asByteArray() // TODO allow configuration
    if (mifare.authenticateSectorWithKeyB(tonuinoSector, key)) {
        val blockIndex = mifare.sectorToBlock(tonuinoSector)
        // NOTE: This could truncates data, if we have more than 16 Byte (= MifareClassic.BLOCK_SIZE)
        val block = data.toFixedLengthBuffer(MifareClassic.BLOCK_SIZE)
        mifare.writeBlock(blockIndex, block)
        Log.i(
            TAG, "Wrote ${byteArrayToHex(data.bytes)} to tag ${tagIdAsString(
                mifare.tag
            )}"
        )
        result = WriteResult.SUCCESS
    } else {
        result = WriteResult.AUTHENTICATION_FAILURE
    }

    mifare.close()

    return result
}

@ExperimentalUnsignedTypes
fun writeTag(mifare: MifareUltralight, data: TagData): WriteResult {
    try {
        if (!mifare.isConnected) mifare.connect()
    } catch (ex: IOException) {
        // is e.g. thrown if the NFC tag was removed
        return WriteResult.TAG_UNAVAILABLE
    }

    val len = data.bytes.size

    Log.i(TAG, "data byte size $len")

    val pagesNeeded = Math.ceil(data.bytes.size.toDouble() / MifareUltralight.PAGE_SIZE).toInt()

    val block = data.toFixedLengthBuffer(MifareUltralight.PAGE_SIZE * pagesNeeded)
    var current = 0
    for (index in 0 until pagesNeeded) {
        val next = current + MifareUltralight.PAGE_SIZE
        val part = block.slice(current until next).toByteArray()
        mifare.writePage(8 + index, part)
        Log.i(
            TAG,
            "Wrote ${byteArrayToHex(part.toUByteArray())} to tag ${tagIdAsString(mifare.tag)}"
        )
        current = next
    }

    return WriteResult.SUCCESS
}


fun techListOf(tag: TagTechnology?) = techListOf(tag?.tag)

fun techListOf(tag: Tag?): List<String> {
    // shorten fully qualified class names, e.g. android.nfc.tech.MifareClassic -> MifareClassic
    return tag?.techList?.map { str -> str.drop(str.lastIndexOf('.') + 1) } ?: listOf()
}
