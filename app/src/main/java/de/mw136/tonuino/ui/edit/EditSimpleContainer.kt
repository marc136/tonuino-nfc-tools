package de.mw136.tonuino.ui.edit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.mw136.tonuino.R
import de.mw136.tonuino.nfc.EditNfcData
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.WhichByte
import de.mw136.tonuino.ui.edit.simple.EditSimple
import de.mw136.tonuino.ui.edit.simple.ModifierTag


enum class TagType { Normal, Modifier }

@ExperimentalUnsignedTypes
class EditSimpleContainer : EditFragment() {
    private var listener: EditNfcData? = null
    override val TAG = "EditSimpleContainer"
    private var child: EditFragment? = null

    private var tagType = TagType.Normal

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i(TAG, "onAttach()")
        listener = context as EditNfcData
        if (listener == null) {
            throw RuntimeException("$context must implement EditNfcData")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        propagateChanges = false
        Log.i(TAG, "onCreateView()")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_simple_container, container, false)

        if (listener?.tagData?.isModifierTag() == true) {
            Log.e(TAG, "useModifierTagEditUi")
            tagType = TagType.Modifier
            child = ModifierTag()
        } else {
            Log.e(TAG, "useNormalTagEditUi")
            tagType = TagType.Normal
            child = EditSimple()
        }
        requireFragmentManager().beginTransaction().replace(R.id.children, child!!).commit()

        return view
    }

    override fun onResume() {
        super.onResume()
        // necessary because calling refreshUI from setUserVisibleHint does not paint the changes if it is called from
        // the wrong thread. Happens e.g. when switching between EditHex and EditSimple using the EditPagerAdapter
        refreshUi(listener!!.tagData)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.i(TAG, "setUserVisibleHint($isVisibleToUser)")

        if (listener == null) {
            Log.d("$TAG.setUserVisibleHint", "listener is null")
        } else if (isVisibleToUser) {
            refreshUi(listener!!.tagData)
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.i(TAG, "onDetach()")
        listener = null
    }

    override fun refreshInputs(data: TagData) {
        Log.i("$TAG:refreshInputs", data.toString())
        try {
            child?.refreshInputs(data)
        } catch (ex: Exception) {
            // ignore
        }
    }

    override fun refreshDescriptions(data: TagData) {
        Log.i("$TAG:refreshDescriptions", data.toString())
        try {
            child?.refreshDescriptions(data)
        } catch (ex: Exception) {
            // ignore
        }
    }

    fun useNormalTagEditUi() {
        if (tagType != TagType.Normal) {
            listener?.setByte(WhichByte.FOLDER, 1u)
            tagType = TagType.Normal
        }
        if (!(child is EditSimple)) {
            child = EditSimple()
            requireFragmentManager().beginTransaction().replace(R.id.children, child!!).commit()
        }
    }

    fun useModifierTagEditUi() {
        if (tagType != TagType.Modifier) {
            listener?.setByte(WhichByte.FOLDER, 0u)
            tagType = TagType.Modifier
        }
        if (!(child is ModifierTag)) {
            child = ModifierTag()
            requireFragmentManager().beginTransaction().replace(R.id.children, child!!).commit()
        }
    }
}
