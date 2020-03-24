package de.mw136.tonuino.ui.enter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.mw136.tonuino.R
import de.mw136.tonuino.afterTextChanged
import de.mw136.tonuino.setByteIfChanged
import de.mw136.tonuino.setResArrayString
import de.mw136.tonuino.ui.Format1Mode
import de.mw136.tonuino.ui.Tonuino


@ExperimentalUnsignedTypes
class Extended : Fragment() {
    private val TAG: String = "enter.Extended"

    private val tagData: EnterViewModel by activityViewModels()

    private lateinit var version: EditText
    private lateinit var versionDescription: TextView
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
        val view = inflater.inflate(R.layout.enter_fragment_extended, container, false)

        version = view.findViewById(R.id.version)
        version.setByteIfChanged(tagData.version.value)
        versionDescription = view.findViewById(R.id.version_description)

        folder = view.findViewById(R.id.folder)
        folder.setByteIfChanged(tagData.folder.value)
        folderDescription = view.findViewById(R.id.folder_description)

        mode = view.findViewById<EditText>(R.id.mode)
        mode.setByteIfChanged(tagData.mode.value)
        modeDescription = view.findViewById(R.id.mode_description)

        special = view.findViewById(R.id.special)
        specialLabel = view.findViewById(R.id.special_label)
        specialDescription = view.findViewById(R.id.special_description)
        specialRow = view.findViewById(R.id.special_row)

        special2 = view.findViewById(R.id.special2)
        special2Label = view.findViewById(R.id.special2_label)
        special2Description = view.findViewById(R.id.special2_description)
        special2Row = view.findViewById(R.id.special2_row)

        showDescriptions()
        addUserEventListeners()
        addLiveDataEventListeners()

        return view
    }

    private fun showDescriptions() {
        Log.w(TAG, "showDescriptions for version ${tagData.version.value}")
        when (tagData.version.value) {
            Tonuino.format1 -> {
                showFormat1Descriptions(tagData.mode.value?.toInt() ?: -1)
                versionDescription.text = getString(R.string.edit_version_1)
            }
            Tonuino.format2 -> {
                showFormat2Descriptions(tagData.mode.value?.toInt() ?: -1)
                versionDescription.text = getString(R.string.edit_version_2)
            }
            else -> {
                hideAllDescriptions()
                versionDescription.text =
                    getString(R.string.edit_unknown_value, tagData.version.value)
            }
        }
    }

    private fun showFormat1Descriptions(mode: Int) {
        Log.d(TAG, "showFormat1Descriptions")

        val arr = resources.getStringArray(R.array.edit_mode_description)
        modeDescription.text = if (mode in 1..arr.size) {
            arr[mode - 1]
        } else {
            getString(R.string.edit_mode_unknown, mode)
        }
        modeDescription.visibility = View.VISIBLE

        when (mode) {
            Format1Mode.AudioBookRandom.value,
            Format1Mode.AudioBookMultiple.value,
            Format1Mode.Album.value,
            Format1Mode.Party.value
            -> {
                specialLabel.text = getString(R.string.edit_special_label)
                specialDescription.visibility = View.INVISIBLE
                specialDescription.text = getString(R.string.edit_hidden_label)
            }
            Format1Mode.Single.value -> {
                specialLabel.text = getString(R.string.edit_special_label_for_album_mode)
                specialDescription.visibility = View.VISIBLE
                specialDescription.text =
                    getString(R.string.play_mp3_file, tagData.special.value?.toInt())
            }
            else -> {
                // unknown modes
                specialLabel.text = getString(R.string.edit_special_label)
                specialDescription.visibility = View.INVISIBLE
                specialDescription.text = getString(R.string.edit_hidden_label)
            }
        }

        special2Row.visibility = View.VISIBLE
    }

    private fun showFormat2Descriptions(mode: Int) {
        Log.d(TAG, "showFormat2Descriptions")

        // TODO might need to change because of other changes
        val folder = tagData.folder.value ?: return
        val mode = tagData.mode.value ?: return

        specialRow.visibility = View.VISIBLE
        special2Row.visibility = View.VISIBLE

        if (folder == 0u.toUByte()) {
            showFormat2ModifierDescriptions(mode.toInt())

        } else if (folder < 100u) {
            folderDescription.text =
                getString(R.string.edit_ext_folder_description, folder.toInt())
            folderDescription.visibility = View.VISIBLE

            modeDescription.setResArrayString(
                mode.toInt(),
                R.array.edit_mode,
                R.array.edit_mode_description
            )
//            modeDescription.visibility = View.VISIBLE
//            val index: Int = mode.toInt() - 1
//            val text = resources.getStringArray(R.array.edit_mode)[index] + ": " +
//                    resources.getStringArray(R.array.edit_mode_description)[index]
//            modeDescription.text = text
        } else {
            // value not used in TonUINO
            folderDescription.visibility = View.VISIBLE
            folderDescription.text = getString(R.string.edit_ext_folder_not_allowed_description)
        }
    }

    private fun hideAllDescriptions() {
        Log.d(TAG, "hideAllDescriptions")
        folderDescription.visibility = View.GONE
        modeDescription.visibility = View.GONE
        specialRow.visibility = View.VISIBLE
        specialLabel.text = getString(R.string.edit_special_label)
        specialDescription.visibility = View.GONE
        special2Row.visibility = View.VISIBLE
        special2Label.text = getString(R.string.edit_special2_label)
        special2Description.visibility = View.GONE
    }


    private fun compoundDescription(
        textView: TextView,
        value: Int,
        resId1: Int,
        resId2: Int,
        fallback: String = ""
    ) {
        textView.visibility = View.VISIBLE

        val titles = resources.getStringArray(resId1)
        val descriptions = resources.getStringArray(resId2)
        val max = if (titles.size > descriptions.size) titles.size else descriptions.size

        if (value in 1 until max) {
            textView.visibility = View.VISIBLE
            textView.text = titles[value - 1] + ": " + descriptions[value - 1]
        } else {
            if (fallback.isNullOrBlank()) {
                textView.visibility = View.GONE
                textView.text = ""
            } else {
                textView.visibility = View.VISIBLE
                textView.text = fallback
            }
        }
    }

    private fun showFormat2ModifierDescriptions(mode: Int) {
        folderDescription.visibility = View.INVISIBLE

        val titles = resources.getStringArray(R.array.edit_modifier_tags)
        val descriptions = resources.getStringArray(R.array.edit_modifier_tags_description)
        val max = if (titles.size > descriptions.size) titles.size else descriptions.size
        if (mode in 1 until max) {
            val index = mode - 1
            modeDescription.visibility = View.VISIBLE
            val text = titles[index] + ": " + descriptions[index]
            modeDescription.text = text
        } else {
            Log.d(TAG, "Cannot display a description for unknown modifier mode '$mode'.")
            modeDescription.visibility = View.GONE
            modeDescription.text = ""
        }
    }

    private fun addUserEventListeners() {
        version.afterTextChanged { value ->
            Log.d(TAG, "version.afterTextChanged $value")
            val it = value.toUByteOrNull()
            if (it == null) {
                version.error = resources.getString(R.string.edit_limit_numeric_value, 0, 255)
            } else {
                version.error = null
                tagData.setVersion(it)
                showDescriptions()
            }
        }

        folder.afterTextChanged { value ->
            Log.d(TAG, "folder.afterTextChanged $value")
            val it = value.toUByteOrNull()
            if (it == null) {
                folder.error = resources.getString(R.string.edit_limit_numeric_value, 0, 255)
            } else {
                folder.error = null
                tagData.setFolder(it)
                showDescriptions()
            }
        }

        mode.afterTextChanged { value ->
            Log.d(TAG, "mode.afterTextChanged $value")
            val it = value.toUByteOrNull()
            if (it == null) {
                mode.error = resources.getString(R.string.edit_limit_numeric_value, 0, 255)
            } else {
                mode.error = null
                tagData.setMode(it)
                showDescriptions()
            }
        }

        special.afterTextChanged { value ->
            Log.e(TAG, "special.afterTextChanged $value")
            val it = value.toUByteOrNull()
            if (it == null) {
                special.error = resources.getString(R.string.edit_limit_numeric_value, 0, 255)
            } else {
                special.error = null
                tagData.setSpecial(it)
                showDescriptions()
            }
        }

        special2.afterTextChanged { value ->
            Log.d(TAG, "special2.afterTextChanged $value")
            val it = value.toUByteOrNull()
            if (it == null) {
                special2.error = resources.getString(R.string.edit_limit_numeric_value, 0, 255)
            } else {
                special2.error = null
                tagData.setSpecial2(it)
                showDescriptions()
            }
        }
    }

    private fun addLiveDataEventListeners() {
        tagData.version.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "version.observe $value")
            version.setByteIfChanged(value)
        })

        tagData.folder.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "folder.observe $value")
            folder.setByteIfChanged(value)
        })

        tagData.mode.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "mode.observe $value")
            mode.setByteIfChanged(value)
        })

        tagData.special.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "special.observe $value")
            special.setByteIfChanged(value)
        })

        tagData.special2.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "special2.observe $value")
            special2.setByteIfChanged(value)
        })
    }
}
