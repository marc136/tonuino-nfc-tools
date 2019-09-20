package de.mw136.tonuino.ui.edit

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import de.mw136.tonuino.R
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.hexToBytes
import de.mw136.tonuino.nfc.EditNfcData
import de.mw136.tonuino.nfc.TagData
import java.util.*

@ExperimentalUnsignedTypes
class EditHex : EditFragment() {
    override val TAG = "EditHex"

    private var listener: EditNfcData? = null
    private lateinit var bytesEdit: EditText


    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i(TAG, "onAttach()")
        listener = context as EditNfcData
        if (listener == null) {
            throw RuntimeException(context.toString() + " must implement EditNfcData")
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
        Log.i(TAG, "onCreateView()")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_hex, container, false)

        bytesEdit = view.findViewById(R.id.bytes)
        bytesEdit.addTextChangedListener(object : TextWatcher {
            private var position = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                position = start + count
            }

            override fun afterTextChanged(s: Editable?) {
                val formatted = formatBytes(s.toString())
                if (s.toString() != formatted) {
                    val cursorPosition = position
                    val value = hexToBytes(formatted.replace(" ", ""))

                    (this@EditHex.context as EditNfcData).tagData.bytes = value
                    bytesEdit.setText(formatted)
                    bytesEdit.setSelection(cursorPosition)
                }
            }
        });

        refreshUi(listener!!.tagData)
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

        val formatted = byteArrayToHex(data.bytes).joinToString(" ")
        bytesEdit.setText(formatted)
    }

    override fun refreshDescriptions(data: TagData) {
//        Log.i("$TAG:refreshDescriptions", data.toString())
    }

    private fun formatBytes(bytes: String): String {
        // "012 3456789" -> "01 23 45 67 89"
        return bytes.replace("\\s".toRegex(), "").toUpperCase(Locale.ENGLISH).chunked(2).joinToString(" ")
    }
}
