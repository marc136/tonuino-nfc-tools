package com.example.myapplication

@ExperimentalUnsignedTypes
private val TonuinoCookie = ubyteArrayOf(1u, 2u, 3u, 4u, 5u) // TODO set correct cookie

@ExperimentalUnsignedTypes
class CardData(
    var cookie: UByteArray = TonuinoCookie,
    var version: UByte = 0x01.toUByte(),
    val folderSettings : NfcFolderSettings = NfcFolderSettings()
)
