package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

private const val TAG = "EditSimple"

@ExperimentalUnsignedTypes
class EditSimple : EditFragment() {
    private var listener: EditNfcData? = null


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

        folder = view.findViewById(R.id.edit_simple_folder)
        mode = view.findViewById(R.id.edit_simple_mode)
        modeDescription = view.findViewById(R.id.edit_simple_mode_description)

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume()")
        if (listener == null) {
            Log.i(TAG, "onCreateView(), listener is null")
            return
        }
        val bytes = listener!!.bytes

        val spinner: Spinner = view!!.findViewById(R.id.edit_simple_mode)
        ArrayAdapter.createFromResource(
            activity!!.baseContext,
            R.array.edit_mode, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}

            @ExperimentalUnsignedTypes
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                this@EditSimple.listener?.setByte(WhichByte.MODE, (position + 1).toUByte())
                refreshText(listener!!)
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.i(TAG, "setUserVisibleHint($isVisibleToUser)")

        if (listener == null) {
            Log.d("$TAG.setUserVisibleHint", "listener is null")
        } else if (isVisibleToUser) {
            refreshText(listener!!)
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.i(TAG, "onDetach()")
        listener = null
    }

    override fun refreshText(data: EditNfcData) {
        val bytes = data.bytes
        Log.i("$TAG:refreshText", listOf(bytes).toString())

        val modeIndex = bytes[BytePositions.MODE.ordinal].toInt() - 1
        val arr = resources.getStringArray(R.array.edit_mode_description)
        modeDescription.text = if (modeIndex < arr.size  ) {
            mode.setSelection(modeIndex, false)
            arr[modeIndex]
        } else {
            getString(R.string.edit_mode_unknown, modeIndex + 1)
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
