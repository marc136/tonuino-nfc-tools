package com.example.myapplication

import android.support.v4.app.Fragment

@ExperimentalUnsignedTypes
abstract class EditFragment : Fragment() {
    public abstract fun refreshText(data: EditNfcData)
}
