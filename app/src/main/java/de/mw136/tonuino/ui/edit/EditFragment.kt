package de.mw136.tonuino.ui.edit

import android.support.v4.app.Fragment
import android.util.Log
import de.mw136.tonuino.nfc.TagData

@ExperimentalUnsignedTypes
abstract class EditFragment : Fragment() {
    protected open val TAG = "EditFragment"
    protected var propagateChanges = true

    fun refreshUi(data: TagData) {
        Log.i("$TAG.refreshUi", data.toString())
        propagateChanges = false
        refreshInputs(data)
        refreshDescriptions(data)
        propagateChanges = true
    }

    abstract fun refreshInputs(data: TagData)
    abstract fun refreshDescriptions(data: TagData)
}
