package de.mw136.tonuino.ui.bulk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.mw136.tonuino.BulkEditViewModel
import de.mw136.tonuino.R


@ExperimentalUnsignedTypes
class EnterListFragment : Fragment() {
    private val viewModel: BulkEditViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bulkwrite_fragment_enter_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonStart = view.findViewById<Button>(R.id.button_start)
        buttonStart.isEnabled = viewModel.lineCount > 0
        buttonStart.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val myTextBox = view.findViewById(R.id.editText) as EditText

        myTextBox.setText(viewModel.lines.joinToString(separator = "\n"))
        myTextBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.setLines(s)
                buttonStart.isEnabled = s.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        myTextBox.setHorizontallyScrolling(true)
        myTextBox.setMovementMethod(ScrollingMovementMethod())
    }
}
