package de.mw136.tonuino.ui.bulk

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.mw136.tonuino.BulkEditViewModel
import de.mw136.tonuino.R
import de.mw136.tonuino.TagWithComment
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.nfc.*


@ExperimentalUnsignedTypes
class BulkWriteFragment : Fragment() {
    val TAG = "BulkWFrag"
    private val viewModel: BulkEditViewModel by activityViewModels()
    private var tagData: TagWithComment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bulkwrite_fragment_write_tags, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tagTitle = view.findViewById<TextView>(R.id.text_tag_title)
        val tagBytes = view.findViewById<TextView>(R.id.text_bytes)
        val lines = view.findViewById<TextView>(R.id.description)
        val buttonNext = view.findViewById<Button>(R.id.button_next)
        val buttonPrev = view.findViewById<Button>(R.id.button_prev)

        viewModel.currentLine.observe(viewLifecycleOwner, Observer { current ->
            Log.i(TAG, "currentLine changed to $current")
            lines.text = getString(
                R.string.bulk_write_line_number,
                viewModel.currentLineIndex + 1,
                viewModel.lineCount
            )

            buttonPrev.isEnabled = viewModel.hasPrevious
            buttonNext.isEnabled = viewModel.hasNext

            val tag = TagWithComment.of(current)
            if (tag != null) {
                tagData = tag
                tagTitle.setText(tag.title)
                val str = byteArrayToHex(tag.bytes).joinToString(" ")
                tagBytes.setText(str)
            } else {
                tagTitle.setText(getString(R.string.bulk_write_invalid_line_format))
                tagBytes.setText('"' + current + '"')
                tagData = null
            }
        })

        buttonPrev.setOnClickListener {
            viewModel.previousLine()
        }

        buttonNext.setOnClickListener {
            viewModel.nextLine()
        }

        view.findViewById<Button>(R.id.button_back).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        val writeButton = view.findViewById<Button>(R.id.button_write)
        writeButton.setOnClickListener {
            writeTag()
        }

        viewModel.tag.observe(viewLifecycleOwner, Observer { tag ->
            if (tag == null) {
                writeButton.setText(getString(R.string.edit_write_button_no_tag))
                writeButton.isEnabled = false
                Toast.makeText(activity, getString(R.string.edit_nfc_connection_lost), Toast.LENGTH_LONG).show()
            } else {
                writeButton.setText(getString(R.string.edit_write_button, tagIdAsString(tag)))
                writeButton.isEnabled = true

                // TODO add option to instantly write data when a tag is found
            }
        })

        view.clearFocus()
    }

    private fun writeTag() {
        var result = WriteResult.TAG_UNAVAILABLE
        Log.w(TAG, "called writeTag")
        viewModel.tag.value?.let {
            Log.w(TAG, "has tag.value $it")
            Log.w("$TAG.writeTag", "will write to tag ${tagIdAsString(it)}")

            tagData?.bytes?.let { bytes ->
                result = writeTonuino(it, TagData(bytes))
            }
            Log.w("$TAG.writeTag", "result $result")
        }
        showModalDialog(result)
    }

    private fun showModalDialog(result: WriteResult) {
        with(AlertDialog.Builder(activity)) {
            var showRetryButton = false

            when (result) {
                WriteResult.SUCCESS -> {
                    // TODO add "next line" button
                    setMessage(R.string.written_success)
                }
                WriteResult.UNSUPPORTED_FORMAT -> {
                    setTitle(R.string.written_unsupported_tag_type)
                    setMessage(
                        getString(
                            R.string.nfc_tag_technologies,
                            techListOf(viewModel.tag.value).joinToString(", ")
                        )
                    )
                }
                WriteResult.AUTHENTICATION_FAILURE -> {
                    setTitle(R.string.written_title_failure)
                    setMessage(R.string.written_authentication_failure)
                    showRetryButton = true
                }
                WriteResult.TAG_UNAVAILABLE -> {
                    setTitle(R.string.written_title_failure)
                    setMessage(R.string.written_tag_unavailable)
                    showRetryButton = true
                }
                WriteResult.UNKNOWN_ERROR -> {
                    setTitle(R.string.written_unknown_error)
                    setMessage(
                        getString(
                            R.string.nfc_tag_technologies,
                            techListOf(viewModel.tag.value).joinToString(", ")
                        )
                    )
                    showRetryButton = true
                }
            }

            setPositiveButton(getString(R.string.button_ok)) { _, _ -> }
            if (showRetryButton) {
                setNegativeButton(getString(R.string.written_button_retry)) { _, _ -> writeTag() }
            }
            create().show()
        }
    }
}
