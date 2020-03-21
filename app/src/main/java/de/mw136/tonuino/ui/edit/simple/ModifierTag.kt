package de.mw136.tonuino.ui.edit.simple

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import de.mw136.tonuino.R
import de.mw136.tonuino.nfc.EditNfcData
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.WhichByte
import de.mw136.tonuino.ui.edit.EditFragment
import de.mw136.tonuino.validateInputAndSetByte

@ExperimentalUnsignedTypes
class ModifierTag : EditFragment() {
    private var listener: EditNfcData? = null
    override val TAG = "ModifierTag"

    private lateinit var mode: Spinner
    private lateinit var modeDescription: TextView
    private lateinit var special: EditText
    private lateinit var specialLabel: TextView
    private lateinit var specialDescription: TextView
    private lateinit var specialRow: View

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
        val view = inflater.inflate(R.layout.fragment_modifier_tag, container, false)

        mode = view.findViewById(R.id.mode)
        // initialize spinner for 'mode'
        ArrayAdapter.createFromResource(
            requireActivity().baseContext,
            R.array.edit_modifier_tags, android.R.layout.simple_spinner_item
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
                if (propagateChanges) {
                    this@ModifierTag.listener?.setByte(WhichByte.MODE, (position + 1).toUByte())
                    refreshUi(listener!!.tagData)
                }
            }
        }
        modeDescription = view.findViewById(R.id.mode_description)

        specialLabel = view.findViewById(R.id.special_label)
        special = view.findViewById(R.id.special)
        special.validateInputAndSetByte(WhichByte.SPECIAL, 0, 255)
        specialDescription = view.findViewById(R.id.special_description)
        specialRow = view.findViewById(R.id.special_row)

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

        val modeIndex = data.mode.toInt() - 1
        val arr = resources.getStringArray(R.array.edit_mode_description)
        if (modeIndex < arr.size) {
            mode.setSelection(modeIndex, false)
        }

        special.setText(data.special.toString())
    }

    override fun refreshDescriptions(data: TagData) {
        Log.i("$TAG:refreshDescriptions", data.toString())

        val modeIndex = data.mode.toInt() - 1
        val arr = resources.getStringArray(R.array.edit_modifier_tags_description)
        modeDescription.text = if (modeIndex in 0..arr.size) {
            arr[modeIndex]
        } else {
            getString(R.string.edit_mode_unknown, modeIndex + 1)
        }

        refreshSpecialRow(modeIndex + 1, data.special.toInt())
    }

    private fun refreshSpecialRow(mode: Int, value: Int) {
        when (mode) {
            1 -> {
                specialRow.visibility = View.VISIBLE
                specialLabel.text = getString(R.string.edit_special_label_for_modifier_sleep_timer)
                specialDescription.visibility = View.VISIBLE
                specialDescription.text = getString(R.string.play_timer, value)
            }

            2, 3, 4, 5, 6, 7 -> {
                specialRow.visibility = View.GONE
                specialLabel.text = getString(R.string.edit_hidden_label)
                specialDescription.visibility = View.GONE
                specialDescription.text = getString(R.string.edit_hidden_label)
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
