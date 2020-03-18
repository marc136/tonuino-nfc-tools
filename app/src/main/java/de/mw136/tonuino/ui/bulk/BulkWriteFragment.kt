package de.mw136.tonuino.ui.bulk

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.mw136.tonuino.*


@ExperimentalUnsignedTypes
class BulkWriteFragment : Fragment() {
    val TAG = "BulkActivity"
    private val viewModel: BulkEditViewModel by activityViewModels()

    private lateinit var buttonNext: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bulkwrite_fragment_write_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tagTitle = view.findViewById<TextView>(R.id.text_tag_title)
        val tagBytes = view.findViewById<TextView>(R.id.text_bytes)
        val lines = view.findViewById<TextView>(R.id.description)
        buttonNext = view.findViewById<Button>(R.id.button_next)

        viewModel.currentLine.observe(viewLifecycleOwner, Observer { current ->
            Log.i(TAG, "currentLine changed to $current")
            lines.text = getString(
                R.string.bulkwrite_line_number,
                viewModel.currentLineIndex + 1,
                viewModel.lineCount
            )

            Log.e(TAG, "viewModel.hasNext ${viewModel.hasNext}")
            buttonNext.isEnabled = viewModel.hasNext

            val tag = TagWithComment.of(current)
            if (tag != null) {
                tagTitle.setText(tag.title)
                val str = byteArrayToHex(tag.bytes).joinToString(" ")
                tagBytes.setText(str)
            } else {
                tagTitle.setText("Konnte die Zeile nicht lesen")
                tagBytes.setText('"' + current + '"')
            }
        })

        buttonNext.setOnClickListener {
            viewModel.nextLine()
        }

        view.findViewById<Button>(R.id.button_back).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        view.clearFocus()
    }
}
