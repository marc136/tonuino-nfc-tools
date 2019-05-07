package de.mw136.tonuino.ui.edit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import de.mw136.tonuino.*
import de.mw136.tonuino.nfc.EditNfcData
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.WhichByte

/**
 * From https://developer.android.com/guide/components/fragments#Creating
 * Execution order: onAttach, onCreate, onCreateView, onActivityCreated, onStart, onResume
 * Note: onAttach, onCreate, onDestroy and onDetach are only executed once
 *
 * When the TabLayout is used to show this fragment, setUserVisibleHint(true) is called and the view is updated
 */
@ExperimentalUnsignedTypes
class EditExtended : EditFragment() {
    override val TAG = "EditExtended"

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

        folder = view.findViewById(R.id.folder)
        folder.validateInputAndSetByte(WhichByte.FOLDER, 1, 99) // 0x01-0x63 (restricted by the DFPlayer)
        folderDescription = view.findViewById(R.id.folder_description)

        mode = view.findViewById(R.id.mode)
        mode.validateInputAndSetByte(WhichByte.MODE, 0, 255)
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

        refreshUi(listener!!.tagData)
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
            refreshUi(listener!!.tagData)
        }
    }

    override fun refreshInputs(data: TagData) {
        Log.i("$TAG:refreshInputs", data.toString())

        folder.setText(data.folder.toString())
        mode.setText(data.mode.toString())
        special.setText(data.special.toString())
        special2.setText(data.special2.toString())
    }

    override fun refreshDescriptions(data: TagData) {
        Log.i("$TAG:refreshDescriptions", data.toString())

        folderDescription.text = getString(R.string.edit_ext_folder_description, data.folder.toInt())

        val mode_ = data.mode.toInt()
        if (mode_ in 1..6) {
            modeDescription.visibility = View.VISIBLE
            val text = resources.getStringArray(R.array.edit_mode)[mode_ - 1] + ": " +
                    resources.getStringArray(R.array.edit_mode_description)[mode_ - 1]
            modeDescription.text = text
        } else {
            Log.w("$TAG:refreshDescriptions", "Cannot display a description for unknown mode '$mode_'.")
            modeDescription.visibility = View.GONE
            modeDescription.text = ""
        }

        refreshSpecialDescription(mode_.toInt(), data.special.toInt())

        // always hide special2 for now as it is not used in Tonuino 2.0.1
        special2Row.visibility = View.GONE
    }

    private fun refreshSpecialDescription(mode: Int, value: Int) {
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

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach()")
        listener = null
    }

    private fun isFormValid(): Boolean {
        return listOf(folder.error).all { it == null }
    }
}
