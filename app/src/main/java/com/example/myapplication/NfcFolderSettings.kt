package com.example.myapplication

// This name is used in Tonuino 2.1 (dev)
@ExperimentalUnsignedTypes
class NfcFolderSettings(
    var folder: UByte,
    var mode: UByte,
    var special: UByte = UByte.MIN_VALUE,
    var special2: UByte = UByte.MIN_VALUE
)
