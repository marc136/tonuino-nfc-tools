package com.example.myapplication

// This name is used in Tonuino 2.1 (dev)
@ExperimentalUnsignedTypes
class NfcFolderSettings(
    var folder: UByte = 1u,
    var mode: UByte = 1u,
    var special: UByte = 0u,
    var special2: UByte = 0u
)
