package de.mw136.tonuino

private val characters = "0123456789ABCDEF".toCharArray()

fun byteToHex(byte: UByte): String {
    val i = byte.toInt() and 0xFF
    val i0 = i ushr 4 // i >> 4
    val i1 = i and 0x0F

    return String(charArrayOf(characters[i0], characters[i1]))
}

@ExperimentalUnsignedTypes
fun byteArrayToHex(bytes: UByteArray): List<String> {
    return bytes.map { byteToHex(it) }
}

@ExperimentalUnsignedTypes
fun hexToBytes(hex: String): UByteArray {
    // Could not find a nice way to create a UByteArray from a List<UByte>
    val conv: List<UByte> = hex.chunked(size = 2) { it.toString().toUByte(radix = 16) }
    val result = UByteArray(conv.size) { index -> conv[index] }

    conv.forEachIndexed { index, byte -> result[index] = byte }
    return result
}
