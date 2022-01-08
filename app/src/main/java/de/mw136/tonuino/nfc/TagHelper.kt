package de.mw136.tonuino.nfc

import android.content.res.Resources
import android.nfc.FormatException
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import android.nfc.tech.TagTechnology
import android.util.Log
import de.mw136.tonuino.R
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.hexToBytes
import java.io.IOException
import kotlin.math.ceil


private const val TAG = "TagHelper"
private const val tonuinoSector = 1
private const val firstBlockNum: Byte = 8
private const val lastBlockNum: Byte = 11

@ExperimentalUnsignedTypes
val tonuinoCookie = hexToBytes("1337b347").toList() // TODO add to expert settings

@ExperimentalUnsignedTypes
private val factoryKey =
    hexToBytes("FFFFFFFFFFFF") // factory preset, same as MifareClassic.KEY_DEFAULT

fun tagIdAsString(tag: TagTechnology) = tagIdAsString(tag.tag)

fun tagIdAsString(tag: Tag): String = tag.id.toHex(":")

fun ByteArray.toHex(separator: String = " "): String =
    joinToString(separator) { eachByte -> "%02x".format(eachByte).uppercase() }

fun describeTagType(tag: TagTechnology): String {
    return try {
        if (!tag.isConnected) tag.connect()
        when (tag) {
            is MifareClassic ->
                when (tag.type) {
                    MifareClassic.TYPE_CLASSIC -> "Mifare Classic"
                    MifareClassic.TYPE_PLUS -> "Mifare Plus"
                    MifareClassic.TYPE_PRO -> "Mifare Pro"
                    else -> "Mifare Classic (${
                        Resources.getSystem().getString(R.string.identify_unknown_type)
                    })"
                }
            is MifareUltralight ->
                when (tag.type) {
                    MifareUltralight.TYPE_ULTRALIGHT -> "Mifare Ultralight"
                    MifareUltralight.TYPE_ULTRALIGHT_C -> "Mifare Ultralight C"
                    else -> "Mifare Ultralight (${
                        Resources.getSystem().getString(R.string.identify_unknown_type)
                    })"
                }
            is NfcA ->
                "NfcA (SAK: ${tag.sak.toString().padStart(2, '0')}, ATQA: ${tag.atqa.toHex()})"
            else ->
                Resources.getSystem().getString(R.string.identify_unsupported_type)
        }
    } catch (ex: Exception) {
        Resources.getSystem().getString(R.string.identify_exception)
    }
}

fun connectTo(tag: Tag): TagTechnology? {
    return when {
        tag.techList.contains(MifareClassic::class.java.name) -> {
            MifareClassic.get(tag)?.apply { connect() }
        }
        tag.techList.contains(MifareUltralight::class.java.name) -> {
            MifareUltralight.get(tag)?.apply { connect() }
        }
        tag.techList.contains(NfcA::class.java.name) -> {
            NfcA.get(tag)?.apply { connect() }
        }
        else -> {
            throw FormatException("Can only handle MifareClassic, MifareUltralight and NfcA")
        }
    }
}

@ExperimentalUnsignedTypes
fun readFromTag(tag: Tag): UByteArray {
    val id = tagIdAsString(tag)
    var result = ubyteArrayOf()

    try {
        Log.i(TAG, "Tag $id techList: ${techListOf(tag).joinToString(", ")}")
        when {
            tag.techList.contains(MifareClassic::class.java.name) -> {
                MifareClassic.get(tag)?.use { mifare -> result = readFromTag(mifare) }
            }
            tag.techList.contains(MifareUltralight::class.java.name) -> {
                MifareUltralight.get(tag)?.use { mifare -> result = readFromTag(mifare) }
            }
            tag.techList.contains(NfcA::class.java.name) -> {
                NfcA.get(tag)?.use { nfca -> result = readFromTag(nfca) }
            }
            else -> {
                Log.e(
                    "$TAG.readFromTag",
                    "Tag $id did not enumerate MifareClassic, MifareUltralight or NfcA and is not supported"
                )
            }
        }
    } catch (ex: Exception) {
        // e.g. android.nfc.TagLostException, IOException
        Log.e("$TAG.readFromTag", ex.toString())
    }

    return dropTrailingZeros(result)
}

@ExperimentalUnsignedTypes
fun dropTrailingZeros(bytes: UByteArray): UByteArray {
    if (bytes.isEmpty()) return bytes

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
fun readFromTag(tag: MifareClassic): UByteArray {
    if (!tag.isConnected) tag.connect()
    var result = ubyteArrayOf()

    val key = factoryKey.asByteArray()
    if (tag.authenticateSectorWithKeyA(tonuinoSector, key)) {
        val blockIndex = tag.sectorToBlock(tonuinoSector)
        val block = tag.readBlock(blockIndex).toUByteArray()
        tag.close()

        Log.w(TAG, "Bytes in sector: ${byteArrayToHex(block).joinToString(" ")}")

        // first 4 byte should match the tonuinoCookie
        if (block.take(tonuinoCookie.size) == tonuinoCookie) {
            Log.i(TAG, "This is a Tonuino MifareClassic tag")
        }

        result = block
    } else {
        tag.close()
        Log.e(TAG, "Authentication of sector $tonuinoSector failed!")
    }

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
fun readFromTag(tag: MifareUltralight): UByteArray {
    if (!tag.isConnected) tag.connect()

    val tagType = when (tag.type) {
        MifareUltralight.TYPE_ULTRALIGHT -> "ULTRALIGHT"
        MifareUltralight.TYPE_ULTRALIGHT_C -> "ULTRALIGHT_C"
        else -> "ULTRALIGHT (UNKNOWN)"
    }

    // tonuinoCookie should be in page 8
    val block = tag.readPages(8).toUByteArray()
    tag.close()

    // first 4 byte should match the tonuinoCookie
    if (block.take(tonuinoCookie.size) == tonuinoCookie) {
        Log.i(TAG, "This is a Tonuino MIFARE $tagType tag")
    }

    Log.i(TAG, "Bytes in sector: ${byteArrayToHex(block).joinToString(" ")}")

    return block
}

/**
 * This actually reads a Mifare Ultralight TAG using NfcA
 */
@ExperimentalUnsignedTypes
fun readFromTag(tag: NfcA): UByteArray {
    if (!tag.isConnected) tag.connect()

    val block = tag.transceive(
        byteArrayOf(
            0x3A.toByte(),  // FAST_READ
            firstBlockNum,
            lastBlockNum,
        ),
    ).toUByteArray()
    tag.close()

    // first 4 byte should match the tonuinoCookie
    if (block.take(tonuinoCookie.size) == tonuinoCookie) {
        Log.i(TAG, "This is a Tonuino NFCA tag")
    }

    return block
}

data class WriteResultData(val description: String, val result: WriteResult)

// ADT as shown on https://medium.com/sharenowtech/kotlin-adt-74472319962a
sealed class WriteResult {
    object Success : WriteResult()
    object UnsupportedFormat : WriteResult()
    object AuthenticationFailure : WriteResult()
    object TagUnavailable : WriteResult()
    data class NfcATransceiveNotOk(val response: ByteArray) : WriteResult()
    object UnknownError : WriteResult()
}

@ExperimentalUnsignedTypes
fun writeTonuino(tag: TagTechnology, data: UByteArray): WriteResultData {
    var description: String = ""
    val result: WriteResult = try {
        description = describeTagType(tag)
        when (tag) {
            is MifareClassic -> writeTag(tag, data)
            is MifareUltralight -> writeTag(tag, data)
            is NfcA -> writeTag(tag, data)
            else -> WriteResult.UnsupportedFormat
        }
    } catch (ex: TagLostException) {
        WriteResult.TagUnavailable
    } catch (ex: FormatException) {
        WriteResult.UnsupportedFormat
    } catch (ex: Exception) {
        WriteResult.UnknownError
    }

    return WriteResultData(description, result)
}

@ExperimentalUnsignedTypes
fun writeTag(tag: MifareClassic, data: UByteArray): WriteResult {
    val result: WriteResult
    try {
        if (!tag.isConnected) tag.connect()
    } catch (ex: IOException) {
        // is e.g. thrown if the NFC tag was removed
        return WriteResult.TagUnavailable
    }

    val key = factoryKey.asByteArray() // TODO allow configuration
    result = if (tag.authenticateSectorWithKeyB(tonuinoSector, key)) {
        val blockIndex = tag.sectorToBlock(tonuinoSector)
        // NOTE: This could truncates data, if we have more than 16 Byte (= MifareClassic.BLOCK_SIZE)
        val block = toFixedLengthBuffer(data, MifareClassic.BLOCK_SIZE)
        tag.writeBlock(blockIndex, block)
        Log.i(
            TAG, "Wrote ${byteArrayToHex(data)} to tag ${
                tagIdAsString(
                    tag.tag
                )
            }"
        )
        WriteResult.Success
    } else {
        WriteResult.AuthenticationFailure
    }

    tag.close()

    return result
}

@ExperimentalUnsignedTypes
fun writeTag(tag: MifareUltralight, data: UByteArray): WriteResult {
    try {
        if (!tag.isConnected) tag.connect()
    } catch (ex: IOException) {
        // is e.g. thrown if the NFC tag was removed
        return WriteResult.TagUnavailable
    }

    val len = data.size
    Log.i(TAG, "data byte size $len")

    val pagesNeeded = ceil(data.size.toDouble() / MifareUltralight.PAGE_SIZE).toInt()

    val block = toFixedLengthBuffer(data, MifareUltralight.PAGE_SIZE * pagesNeeded)
    var current = 0
    for (index in 0 until pagesNeeded) {
        val next = current + MifareUltralight.PAGE_SIZE
        val part = block.slice(current until next).toByteArray()
        tag.writePage(8 + index, part)
        Log.i(
            TAG,
            "Wrote ${byteArrayToHex(part.toUByteArray())} to tag ${tagIdAsString(tag.tag)}"
        )
        current = next
    }

    return WriteResult.Success
}


@ExperimentalUnsignedTypes
fun writeTag(tag: NfcA, data: UByteArray): WriteResult {
    try {
        if (!tag.isConnected) tag.connect()
    } catch (ex: IOException) {
        // is e.g. thrown if the NFC tag was removed
        return WriteResult.TagUnavailable
    }

    // The MFRC522 lib that TonUINO uses detects the tag type using the SAK ID with `PICC_GetType` (Proximity inductive coupling card)
    // See https://github.com/miguelbalboa/rfid/blob/eda2e385668163062250526c0e19033247d196a8/src/MFRC522.cpp#L1321
    // More information on the standards, different vendors and how to guess the tag type using SAK and ATQA values is on
    // https://nfc-tools.github.io/resources/standards/iso14443A/
    return when (tag.sak.toInt()) {
        0 ->
            writeMifareUltralight(tag, data)

        8, 9, 10, 11, 18 ->
            // should be writable as Mifare Classic according to
            // https://nfc-tools.github.io/resources/standards/iso14443A/ and https://github.com/miguelbalboa/rfid/blob/eda2e385668163062250526c0e19033247d196a8/src/MFRC522.cpp#L1321
            // writeMifareClassic(tag, data) // WIP: DOES NOT WORK YET!
            WriteResult.UnsupportedFormat

        else ->
            WriteResult.UnsupportedFormat
    }
}

fun writeMifareUltralight(tag: NfcA, data: UByteArray): WriteResult {
    val len = data.size
    var pageNum = firstBlockNum
    val pagesize = MifareUltralight.PAGE_SIZE
    val pagesNeeded = ceil(data.size.toDouble() / pagesize).toInt()

    Log.i(TAG, "data byte size $len")

    var current = 0
    val block = toFixedLengthBuffer(data, pagesize * pagesNeeded)
    for (index in 0 until pagesNeeded) {
        val next = current + pagesize
        val data = byteArrayOf(0xA2.toByte() /* WRITE */, pageNum) + block.slice(current until next)
            .toByteArray()
        Log.i(TAG, "Will transceive(${data.toHex()})")
        val result = tag.transceive(data)
        current = next
        Log.i(TAG, "transceive(${data.toHex()}) returned ${result.toHex()}")
        if (result.size != 1 || result[0] != 0x0A.toByte()) {
            Log.e(TAG, "transceive did not return `ACK (0A)`. Got `${result.toHex()}` instead.")
            tag.close()
            return WriteResult.NfcATransceiveNotOk(result)
        }
        pageNum++
    }

    tag.close()
    return WriteResult.Success
}

@ExperimentalUnsignedTypes
fun toFixedLengthBuffer(bytes: UByteArray, size: Int): ByteArray {
    val block = UByteArray(size) { 0u }
    bytes.forEachIndexed { index, value -> block[index] = value }
    return block.toByteArray()
}

fun techListOf(tag: TagTechnology?) = techListOf(tag?.tag)

fun techListOf(tag: Tag?): List<String> {
    // shorten fully qualified class names, e.g. android.nfc.tech.MifareClassic -> MifareClassic
    return tag?.techList?.map { str -> str.drop(str.lastIndexOf('.') + 1) } ?: listOf()
}
