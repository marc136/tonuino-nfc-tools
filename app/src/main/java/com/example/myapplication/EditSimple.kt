package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "EditSimple"

/**
 * A simple [Fragment] subclass.
 * https://developer.android.com/guide/components/fragments#kotlin
 * Activities that contain this fragment must implement the
 * [EditSimple.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [EditSimple.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
@ExperimentalUnsignedTypes
class EditSimple : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var firstByteText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_simple, container, false)

        firstByteText = view.findViewById(R.id.byte0_label)

        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as OnFragmentInteractionListener
        if (listener == null) {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onResume() {
        super.onResume()

        if (listener == null) return
        val bytes = listener!!.bytes

        var byte: UByte = bytes[0]
        firstByteText.text = "First byte: ${byte}"

        val spinner: Spinner = view!!.findViewById(R.id.byte0_spinner)
        ArrayAdapter.createFromResource(
            activity!!.baseContext,
            R.array.tonuino_modes, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        val listener = SpinnerListener(this, WhichByte.FOLDER)
        spinner.onItemSelectedListener = listener
    }

    fun drawBytes() {
        val bytes = listener!!.bytes

        Log.i(TAG, "drawBytes")
        Log.i("drawBytes", listOf(bytes).toString())

        var byte: UByte = bytes[0]
        firstByteText.text = "First byte: ${byte}"
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        var bytes: UByteArray
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    public fun setByte(value: UByte, which: WhichByte) {
        listener!!.bytes[which.ordinal] = value
        drawBytes()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditSimple.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditSimple().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

enum class WhichByte { FOLDER, MODE, SPECIAL }

class SpinnerListener(val fragment: EditSimple, val which: WhichByte) : AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ExperimentalUnsignedTypes
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        Log.d(TAG, "spinner $position")
        parent.getItemAtPosition(position)

        fragment.setByte((position + 1).toUByte(), which)
    }
}