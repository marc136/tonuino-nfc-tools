package de.mw136.tonuino.ui.enter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.mw136.tonuino.R
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.hexToBytes
import java.util.*

const val NO_BYTE_FORMATTER = true

/**
 * A simple [Fragment] subclass.
 * Use the [EnterHex.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalUnsignedTypes
class EnterHex : Fragment() {
    val TAG = "EnterHex"
    private val tagData: TagData by activityViewModels()

    private lateinit var bytesEdit: EditText

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
        val view = inflater.inflate(R.layout.enter_fragment_hex, container, false)

        bytesEdit = view.findViewById(R.id.bytes)
        bytesEdit.addTextChangedListener(ByteFormatter(bytesEdit, tagData))

        view.findViewById<Button>(R.id.apply_change).setOnClickListener {
            val str: String = bytesEdit.text.toString().replace("\\s".toRegex(), "")
            if (str.isNotEmpty()) {
                tagData.setBytes(hexToBytes(str))
            }
        }

        addLiveDataEventListeners()

        return view
    }

    private fun addLiveDataEventListeners() {
        tagData.version.observe(viewLifecycleOwner, Observer { _ -> updateEditTextIfNeeded() })
        tagData.folder.observe(viewLifecycleOwner, Observer { _ -> updateEditTextIfNeeded() })
        tagData.mode.observe(viewLifecycleOwner, Observer { _ -> updateEditTextIfNeeded() })
        tagData.special.observe(viewLifecycleOwner, Observer { _ -> updateEditTextIfNeeded() })
        tagData.special2.observe(viewLifecycleOwner, Observer { _ -> updateEditTextIfNeeded() })
    }


    private fun updateEditTextIfNeeded() {
        val new = byteArrayToHex(tagData.bytes).joinToString(if (NO_BYTE_FORMATTER) "" else " ")
        Log.v(TAG, "old/new: '${bytesEdit.text}' == '$new'")
        if (bytesEdit.text.toString() != new) {
            Log.e(TAG, "set text to '$new'")
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

@ExperimentalUnsignedTypes
private class ByteFormatter(val editText: EditText, val tagData: TagData) : TextWatcher {
    private val TAG = this.javaClass.simpleName

    private var start: Int = 0
    private var before: Int = 0
    private var after: Int = 0

    private var ignoreChangeEvents: Boolean = if (NO_BYTE_FORMATTER) true else false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (ignoreChangeEvents) return
        Log.w(TAG, "beforeChanged: start: $start, count: $count, after: $after, '$s'")
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (ignoreChangeEvents) return
        Log.w(TAG, "onChanged: start: $start, before: $before, count: $count,  '$s'")
        this.start = start
        this.before = before
        this.after = count
    }

    override fun afterTextChanged(s: Editable?) {
        if (ignoreChangeEvents) return
        ignoreChangeEvents = true

        Log.w(TAG, "afterChanged: '$s'")
        Log.w(TAG, "afterChanged: start: $start, before: $before, after: $after,  '$s'")

        val unformatted = s.toString()
        val formatted = formatBytes(s.toString())
        Log.w(TAG, "$unformatted ->")
        Log.w(TAG, formatted)

        /* cases to cover
            https://developer.android.com/reference/android/text/TextWatcher
        one char added
            00 01 -> 00a 01 -> 00 A0 1
            00 01 -> 0a0 01 -> 0A 00 1
        one char deleted
            00 01 -> 0001 -> 00 01 => this should actually lead to '00 1'
            00 01 -> 0 01 -> 00 1
            00 01 -> 00 0 -> 00 0
        multiple chars added
            must then not only put the cursor, but select more chars
        multiple chars deleted
         */

        if (formatted != unformatted) {
            editText.setText(formatted)

            val diff = formatted.length - unformatted.length
            val cursor = if (before > after) {
                Log.w(TAG, "deleted $diff chars")
                start + after
            } else if (after > before) {
                Log.w(TAG, "added $diff chars")
                start + after + diff
            } else {
                Log.w(TAG, "no chars added")
                start + after + diff
            }

            if (cursor < formatted.length) {
                editText.setSelection(cursor)
            }
        }

        ignoreChangeEvents = false
    }

}
