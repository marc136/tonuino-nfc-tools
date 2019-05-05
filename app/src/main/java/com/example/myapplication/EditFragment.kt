package com.example.myapplication

import android.support.v4.app.Fragment

@ExperimentalUnsignedTypes
abstract class EditFragment : Fragment() {
    fun refreshUi(data: EditNfcData) {
        refreshInputs(data)
        refreshDescriptions(data)
    }
    abstract fun refreshInputs(data: EditNfcData)
    abstract fun refreshDescriptions(data: EditNfcData)
}
