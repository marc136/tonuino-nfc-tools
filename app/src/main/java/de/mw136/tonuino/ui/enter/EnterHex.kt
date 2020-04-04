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
        bytesEdit.addTextChangedListener(ByteFormatter(bytesEdit, tagData))

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

private class ByteFormatter(val editText: EditText, val tagData: EnterViewModel) : TextWatcher {
    private val TAG = this.javaClass.simpleName

    private var start: Int = 0
    private var before: Int = 0
    private var after: Int = 0

    private var ignoreChangeEvents: Boolean = true // TODO test this for the next patch release

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

        // TODO add functionality to delete spaces

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
