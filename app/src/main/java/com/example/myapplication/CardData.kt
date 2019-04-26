package com.example.myapplication

class CardData(
    var cookie: UByteArray,
    var version: UByte = 0x01.toUByte(),
    val folderSettings: NfcFolderSettings
)
