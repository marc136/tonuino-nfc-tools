package de.mw136.tonuino.nfc

import android.nfc.FormatException
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import android.nfc.tech.TagTechnology
import android.util.Log
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


@ExperimentalUnsignedTypes
fun tagIdAsString(tag: TagTechnology) = tagIdAsString(tag.tag)

@ExperimentalUnsignedTypes
fun tagIdAsString(tag: Tag): String {
    return byteArrayToHex(tag.id.toUByteArray()).joinToString(":")
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
            throw FormatException("Can only write MifareClassic, MifareUltralight and NfcA")
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

enum class WriteResult { SUCCESS, UNSUPPORTED_FORMAT, AUTHENTICATION_FAILURE, TAG_UNAVAILABLE, UNKNOWN_ERROR }

@ExperimentalUnsignedTypes
fun writeTonuino(tag: TagTechnology, data: UByteArray): WriteResult {
    val result: WriteResult = try {
        when (tag) {
            is MifareClassic -> writeTag(tag, data)
            is MifareUltralight -> writeTag(tag, data)
            is NfcA -> writeTag(tag, data)
            else -> WriteResult.UNSUPPORTED_FORMAT
        }
    } catch (ex: TagLostException) {
        WriteResult.TAG_UNAVAILABLE
    } catch (ex: FormatException) {
        WriteResult.UNSUPPORTED_FORMAT
    } catch (ex: Exception) {
        WriteResult.UNKNOWN_ERROR
    }

    return result
}

@ExperimentalUnsignedTypes
fun writeTag(tag: MifareClassic, data: UByteArray): WriteResult {
    val result: WriteResult
    try {
        if (!tag.isConnected) tag.connect()
    } catch (ex: IOException) {
        // is e.g. thrown if the NFC tag was removed
        return WriteResult.TAG_UNAVAILABLE
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
        WriteResult.SUCCESS
    } else {
        WriteResult.AUTHENTICATION_FAILURE
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
        return WriteResult.TAG_UNAVAILABLE
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

    return WriteResult.SUCCESS
}

/**
 * This actually writes a Mifare Ultralight TAG using NfcA
 */
@ExperimentalUnsignedTypes
fun writeTag(tag: NfcA, data: UByteArray): WriteResult {
    try {
        if (!tag.isConnected) tag.connect()
    } catch (ex: IOException) {
        // is e.g. thrown if the NFC tag was removed
        return WriteResult.TAG_UNAVAILABLE
    }

    val len = data.size
    var pageNum = firstBlockNum
    val pagesize = MifareUltralight.PAGE_SIZE
    val pagesNeeded = ceil(data.size.toDouble() / pagesize).toInt()

    Log.i(TAG, "data byte size $len")
    var current = 0
    val block = toFixedLengthBuffer(data, pagesize * pagesNeeded)
    for (index in 0 until pagesNeeded) {
        val next = current + pagesize
        val part = block.slice(current until next).toByteArray()
        tag.transceive(
            byteArrayOf(
                0xA2.toByte(),  // WRITE
                pageNum,
                part[0], part[1], part[2], part[3]
            )
        )
        current = next
        pageNum++
    }

    return WriteResult.SUCCESS
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
