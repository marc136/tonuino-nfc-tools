package de.mw136.tonuino.ui.edit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.mw136.tonuino.*
import de.mw136.tonuino.nfc.EditNfcData
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.WhichByte

private const val TAG = "EditSimple"


@ExperimentalUnsignedTypes
class EditSimple : EditFragment() {
    private var listener: EditNfcData? = null
    override val TAG = "EditSimple"

    private lateinit var folder: Spinner
    private lateinit var mode: Spinner
    private lateinit var modeDescription: TextView
    private lateinit var special: EditText
    private lateinit var specialLabel: TextView
    private lateinit var specialDescription: TextView
    private lateinit var specialRow: View
    private lateinit var special2: EditText
    private lateinit var special2Label: TextView
    private lateinit var special2Description: TextView
    private lateinit var special2Row: View

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
        val view = inflater.inflate(R.layout.fragment_edit_simple, container, false)

        folder = view.findViewById(R.id.folder)
        // initialize spinner for 'folder'
        val folders = (1..99).map { it.toString().padStart(2, '0') }
        ArrayAdapter<String>(activity!!.baseContext, android.R.layout.simple_spinner_item, folders).also {
            folder.adapter = it
        }
        folder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}

            @ExperimentalUnsignedTypes
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                this@EditSimple.listener?.setByte(WhichByte.FOLDER, (position + 1).toUByte())
                refreshUi(listener!!.tagData)
            }
        }

        mode = view.findViewById(R.id.mode)
        // initialize spinner for 'mode'
        ArrayAdapter.createFromResource(
            activity!!.baseContext,
            R.array.edit_mode, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            mode.adapter = adapter
        }
        mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}

            @ExperimentalUnsignedTypes
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                this@EditSimple.listener?.setByte(WhichByte.MODE, (position + 1).toUByte())
                refreshUi(listener!!.tagData)
            }
        }
        modeDescription = view.findViewById(R.id.mode_description)

        special = view.findViewById(R.id.special)
        special.validateInputAndSetByte(WhichByte.SPECIAL, 0, 255)
        specialLabel = view.findViewById(R.id.special_label)
        specialDescription = view.findViewById(R.id.special_description)
        specialRow = view.findViewById(R.id.special_row)

        special2 = view.findViewById(R.id.special2)
        special2.validateInputAndSetByte(WhichByte.SPECIAL2, 0, 255)
        special2Description = view.findViewById(R.id.special2_description)
        special2Row = view.findViewById(R.id.special2_row)

        return view
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

        val folderIndex = data.folder.toInt() - 1
        Log.w("$TAG:refreshFolderSpinner", "index=$folderIndex, count=${folder.adapter.count}")
        if (folderIndex < folder.adapter.count) {
            folder.setSelection(folderIndex, false)
        }

        val modeIndex = data.mode.toInt() - 1
        val arr = resources.getStringArray(R.array.edit_mode_description)
        if (modeIndex < arr.size) {
            mode.setSelection(modeIndex, false)
        }

        special.setText(data.special.toString())

        special2.setText(data.special2.toString())
    }

    override fun refreshDescriptions(data: TagData) {
        Log.i("$TAG:refreshDescriptions", data.toString())

        val folderIndex = data.folder.toInt() - 1
        Log.i("$TAG:refreshFolderSpinner", "index=$folderIndex, count=${folder.adapter.count}")

        val modeIndex = data.mode.toInt() - 1
        val arr = resources.getStringArray(R.array.edit_mode_description)
        modeDescription.text = if (modeIndex in 0..arr.size) {
            arr[modeIndex]
        } else {
            getString(R.string.edit_mode_unknown, modeIndex + 1)
        }

        refreshSpecialRow(modeIndex + 1, data.special.toInt())

        // always hide special2 for now as it is not used in Tonuino 2.0.1
        special2Row.visibility = View.GONE
    }

    private fun refreshSpecialRow(mode: Int, value: Int) {
        when (mode) {
            1, 2, 3, 5 -> {
                specialRow.visibility = View.GONE
                specialLabel.text = getString(R.string.edit_hidden_label)
                specialDescription.visibility = View.GONE
                specialDescription.text = getString(R.string.edit_hidden_label)
            }
            4 -> {
                specialRow.visibility = View.VISIBLE
                specialLabel.text = getString(R.string.edit_special_label_for_album_mode)
                specialDescription.visibility = View.VISIBLE
                specialDescription.text = getString(R.string.play_mp3_file, value)
            }
            else -> {
                // unknown modes
                specialRow.visibility = View.VISIBLE
                specialLabel.text = getString(R.string.edit_special_label)
                specialDescription.visibility = View.GONE
                specialDescription.text = getString(R.string.edit_hidden_label)
            }
        }
    }
}

class SpinnerListener(val which: WhichByte) : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>) {}

    @ExperimentalUnsignedTypes
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        Log.d(TAG, "spinner $position")
        parent.getItemAtPosition(position)
        val max = parent.resources.getStringArray(R.array.edit_mode).size
        if (position < max) {
            (parent.context as EditNfcData).setByte(which, (position + 1).toUByte())

        } else {
            Log.w(TAG, "Spinner selected element $position, but only values smaller than $max are allowed.")
        }
    }
}
