package de.mw136.tonuino.ui.enter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.mw136.tonuino.R
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.hexToBytes
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [EnterHex.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalUnsignedTypes
class EnterHex : Fragment() {
    val TAG = "EnterHex"
    private val tagData: EnterViewModel by activityViewModels()

    private lateinit var bytesEdit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.enter_fragment_hex, container, false)

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

                    if (!tagData.bytes.contentEquals(value)) {
                        Log.e(TAG, "Will change the bytes to ${tagData.bytes}")
                        tagData.setBytes(value)
                    }
                    bytesEdit.setText(formatted)
                    // TODO fix cursor position jumping (e.g. when adding a new char)
                    bytesEdit.setSelection(cursorPosition)
                }
            }
        })

        addLiveDataEventListeners()

        return view
    }

    private fun addLiveDataEventListeners() {
        tagData.version.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "version.observe $value")
            updateEditTextIfNeeded()
        })

        tagData.folder.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "folder.observe $value")
            updateEditTextIfNeeded()
        })

        tagData.mode.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "mode.observe $value")
            updateEditTextIfNeeded()
        })

        tagData.special.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "special.observe $value")
            updateEditTextIfNeeded()
        })

        tagData.special2.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "special2.observe $value")
            updateEditTextIfNeeded()
        })
    }

    private fun updateEditTextIfNeeded() {
        val new = byteArrayToHex(tagData.bytes).joinToString(" ")
        Log.e(TAG, "old/new: '${bytesEdit.text.toString()}' == '$new'")
        if (bytesEdit.text.toString() != new) {
            bytesEdit.setText(new)
        }
    }
}

fun formatBytes(bytes: String): String {
    // "012 3456789" -> "01 23 45 67 89"
    return bytes.replace("\\s".toRegex(), "")
        .toUpperCase(Locale.ENGLISH).chunked(2)
        .joinToString(" ")
}
