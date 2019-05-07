package de.mw136.tonuino.ui.edit

import android.support.v4.app.Fragment
import android.util.Log
import de.mw136.tonuino.nfc.TagData

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
