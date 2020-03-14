package de.mw136.tonuino.ui.bulk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.mw136.tonuino.BulkEditViewModel
import de.mw136.tonuino.R


class BulkWriteFragment : Fragment() {
    private val viewModel: BulkEditViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bulkwrite_fragment_write_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentLine.observe(viewLifecycleOwner, Observer { currentLine ->
            val lines = viewModel.lines.value ?: listOf()
            val line = lines[currentLine]
            view.findViewById<TextView>(R.id.textView3).setText(line)

        })

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        view.clearFocus()
    }
}
