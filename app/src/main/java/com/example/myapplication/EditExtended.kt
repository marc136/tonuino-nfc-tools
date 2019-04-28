package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

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
    private lateinit var mode: EditText
    private lateinit var special: EditText
    private lateinit var specialRow: View
    private lateinit var special2: EditText
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
        mode = view.findViewById(R.id.edit_ext_mode)
        special = view.findViewById(R.id.edit_ext_special2)
        specialRow = view.findViewById(R.id.edit_ext_special_row)
        special2 = view.findViewById(R.id.edit_ext_special2)
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
            refreshText(listener!!)
        }
    }

    override fun refreshText(data: EditNfcData) {
        val bytes = data.bytes
        Log.i("$TAG:refreshText", listOf(bytes).toString())

        folder.setText(bytes[BytePositions.FOLDER.ordinal].toString())

        mode.setText(bytes[BytePositions.MODE.ordinal].toString())

        val specialRowVisibility = when (bytes[BytePositions.MODE.ordinal].toInt()) {
            1, 2, 3, 5 -> {
                View.GONE
            }
            else -> {
                View.VISIBLE
            }
        }
        specialRow.visibility = specialRowVisibility
        special.setText(bytes[BytePositions.SPECIAL.ordinal].toString())

        // always hide special2 for now as it is not used in Tonuino 2.0.1
        special2Row.visibility = View.GONE
        special2.setText("unused: " + bytes[BytePositions.SPECIAL2.ordinal].toString())
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach()")
        listener = null
    }
}
