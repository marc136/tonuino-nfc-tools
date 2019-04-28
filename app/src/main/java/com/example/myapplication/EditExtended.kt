package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

private val TAG = "EditExtended"

/**
 * From https://developer.android.com/guide/components/fragments#Creating
 * Execution order: onAttach, onCreate, onCreateView, onActivityCreated, onStart, onResume
 * Note: onAttach, onCreate, onDestroy and onDetach are only executed once
 *
 * When the TabLayout is used to show this fragment, setUserVisibleHint(true) is called and the view is updated
 */
@ExperimentalUnsignedTypes
class EditExtended : EditFragment() {
    private var listener: EditNfcData? = null

    private lateinit var folder: EditText
    private lateinit var folderDescription: TextView
    private lateinit var mode: EditText
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
        Log.d(TAG, "onAttach()")
        if (context is EditNfcData) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement EditNfcData")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView()")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_extended, container, false)

        folder = view.findViewById(R.id.edit_ext_folder)
        folder.validateInputAndSetByte(WhichByte.FOLDER, 1, 99) // 0x01-0x63 (restricted by the DFPlayer)
        folderDescription = view.findViewById(R.id.edit_ext_folder_description)

        mode = view.findViewById(R.id.edit_ext_mode)
        mode.validateInputAndSetByte(WhichByte.MODE, 0, 255)
        modeDescription = view.findViewById(R.id.edit_ext_mode_description)

        special = view.findViewById(R.id.edit_ext_special)
        special.validateInputAndSetByte(WhichByte.SPECIAL, 0, 255)
        specialLabel = view.findViewById(R.id.edit_ext_special_label)
        specialDescription = view.findViewById(R.id.edit_ext_special_description)
        specialRow = view.findViewById(R.id.edit_ext_special_row)

        special2 = view.findViewById(R.id.edit_ext_special2)
//        special2.validateInputAndSetByte(0, 255) // TODO
        special2Description = view.findViewById(R.id.edit_ext_special_description)
        special2Row = view.findViewById(R.id.edit_ext_special2_row)

        val btn = view.findViewById<Button>(R.id.toggle_visible_button)
        btn.setOnClickListener {
            Log.i("onclick", "visibility before click: ${specialRow.visibility}")
            if (specialRow.visibility == View.VISIBLE) {
                specialRow.visibility = View.INVISIBLE
            } else {
                specialRow.visibility = View.VISIBLE
            }
            Log.i("onclick", "visibility after click: ${specialRow.visibility}")
        }

        refreshText(listener!!)

        return view
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.i(TAG, "setUserVisibleHint($isVisibleToUser)")

        if (listener == null) {
            Log.d("$TAG.setUserVisibleHint", "listener is null")
        } else if (isVisibleToUser) {
            listener?.triggerRefreshTextOnCurrentFragment = true
            refreshText(listener!!)
            listener?.triggerRefreshTextOnCurrentFragment = false
        }
    }

    override fun refreshText(data: EditNfcData) {
        val bytes = data.bytes
        Log.i("$TAG:refreshText", listOf(bytes).toString())

        val folder_ = bytes[BytePositions.FOLDER.ordinal]
        folder.setText(folder_.toString())
        folderDescription.text = getString(R.string.edit_ext_folder_description, folder_.toInt())

        val mode_ = bytes[BytePositions.MODE.ordinal]
        mode.setText(mode_.toString())
        if (mode_ > 0u && mode_ <= 6u) {
            modeDescription.visibility = View.VISIBLE
            modeDescription.text = resources.getStringArray(R.array.edit_mode_description)[mode_.toInt() - 1]
        } else {
            Log.w("$TAG:refreshText", "Cannot display a description for unknown mode '$mode_'.")
            modeDescription.visibility = View.GONE
            modeDescription.text = ""
        }

        refreshSpecialRow(mode_.toInt(), bytes[BytePositions.SPECIAL.ordinal].toInt())

        // always hide special2 for now as it is not used in Tonuino 2.0.1
        special2Row.visibility = View.GONE
        special2.setText(bytes[BytePositions.SPECIAL2.ordinal].toString())
    }

    private fun refreshSpecialRow(mode: Int, value: Int) {
        Log.w("$TAG:ebbes", "refreshSpecialRow($value)")
        special.setText(value.toString())

        when (mode) {
            1, 2, 3, 5 -> {
                specialRow.visibility = View.GONE
                specialLabel.text = "hidden"
                specialDescription.visibility = View.GONE
                specialDescription.text = "hidden"
            }
            4 -> {
                specialRow.visibility = View.VISIBLE
                specialLabel.text = "Titel"
                specialDescription.visibility = View.VISIBLE
                specialDescription.text = getString(R.string.play_mp3_file, value)
            }
            else -> {
                // unknown modes
                specialRow.visibility = View.VISIBLE
                specialLabel.text = "Extra"
                specialDescription.visibility = View.GONE
                specialDescription.text = "hidden"
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach()")
        listener = null
    }
}
