package com.example.myapplication

import android.support.v4.app.Fragment
import android.util.Log

@ExperimentalUnsignedTypes
abstract class EditFragment : Fragment() {
    protected open val TAG = "EditFragment"

    fun refreshUi(data: TagData) {
        Log.i("$TAG.refreshUi", data.toString())
        refreshInputs(data)
        refreshDescriptions(data)
    }

    abstract fun refreshInputs(data: TagData)
    abstract fun refreshDescriptions(data: TagData)
}
