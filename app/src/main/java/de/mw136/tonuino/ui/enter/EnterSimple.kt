package de.mw136.tonuino.ui.enter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.mw136.tonuino.R
import de.mw136.tonuino.afterTextChanged
import de.mw136.tonuino.setByteIfChanged
import de.mw136.tonuino.ui.Format1Mode
import de.mw136.tonuino.ui.Format2Mode
import de.mw136.tonuino.ui.Tonuino

private const val VERSION_MAX = 2
private const val FOLDER_MAX = 99
private const val MODE_MAX_FORMAT1 = 6
private const val MODE_MAX_FORMAT2 = 9

@ExperimentalUnsignedTypes
class EnterSimple : Fragment() {
    private val TAG: String = "enter.Simple"

    private val tagData: EnterViewModel by activityViewModels()
    private val mode_max: Int
        get() {
            return if (tagData.version.value?.toUInt() == 1u) MODE_MAX_FORMAT1 else MODE_MAX_FORMAT2
        }

    private lateinit var version: Spinner
    private lateinit var versionDescription: TextView
    private lateinit var folder: Spinner
    private lateinit var folderDescription: TextView
    private var folderValue: UByte = 0u // TODO remove
    private lateinit var mode: Spinner
    private lateinit var modeDescription: TextView
    private var modeValue: UByte = 0u // TODO remove
    private lateinit var special: EditText
    private var specialValue: UByte = 0u // TODO remove
    private lateinit var specialLabel: TextView
    private lateinit var specialDescription: TextView
    private lateinit var specialRow: View
    private lateinit var special2: EditText
    private var special2Value: UByte = 0u // TODO remove
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
        val view = inflater.inflate(R.layout.enter_fragment_simple, container, false)

        version = view.findViewById(R.id.version)
        versionDescription = view.findViewById(R.id.version_description)

        folder = view.findViewById(R.id.folder)
        folderDescription = view.findViewById(R.id.folder_description)

        mode = view.findViewById(R.id.mode)
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
        initSpinnerValues()
        addUserEventListeners()
        addLiveDataEventListeners()

        return view
    }

    private fun showDescriptions() {
        Log.v(TAG, "showDescriptions for version ${tagData.version.value}")
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
                versionDescription.text =
                    getString(R.string.edit_unknown_value, tagData.version.value)
                hideAllDescriptions()
            }
        }
    }

    private fun showFormat1Descriptions(mode: Int) {
        Log.d(TAG, "showFormat1Descriptions")

        val folder = tagData.folder.value?.toInt() ?: return

        if (folder in 1..FOLDER_MAX) {
            folderDescription.text =
                getString(R.string.edit_ext_folder_description, folder)
            folderDescription.visibility = View.VISIBLE
        } else {
            // value not used in TonUINO
            folderDescription.visibility = View.VISIBLE
            folderDescription.text = getString(R.string.edit_ext_folder_not_allowed_description)
        }


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
                specialRow.visibility = View.INVISIBLE
                specialLabel.text = getString(R.string.edit_hidden_label)
                specialDescription.visibility = View.INVISIBLE
                specialDescription.text = getString(R.string.edit_hidden_label)
            }
            Format1Mode.Single.value -> {
                specialRow.visibility = View.VISIBLE
                specialLabel.text = getString(R.string.edit_special_label_for_album_mode)
                specialDescription.visibility = View.VISIBLE
                specialDescription.text =
                    getString(R.string.play_mp3_file, tagData.special.value?.toInt())
            }
            else -> {
                // unknown modes
                specialRow.visibility = View.VISIBLE
                specialLabel.text = getString(R.string.edit_special_label)
                specialDescription.visibility = View.INVISIBLE
                specialDescription.text = getString(R.string.edit_hidden_label)
            }
        }

        // always hide special2 for now as it is not used in Tonuino 2.0.1
        special2Row.visibility = View.INVISIBLE
        special2Description.visibility = View.GONE
    }

    private fun showFormat2Descriptions(mode: Int) {
        Log.d(TAG, "showFormat2Descriptions")

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
                specialRow.visibility = View.GONE
                specialLabel.text = getString(R.string.edit_hidden_label)
                specialDescription.visibility = View.GONE
                specialDescription.text = getString(R.string.edit_hidden_label)
                special2Row.visibility = View.GONE
            }
            Format1Mode.Single.value -> {
                specialRow.visibility = View.VISIBLE
                specialLabel.text = getString(R.string.edit_special_label_for_album_mode)
                specialDescription.visibility = View.VISIBLE
                specialDescription.text =
                    getString(R.string.play_mp3_file, tagData.special.value?.toInt())
                special2Row.visibility = View.GONE
                special2Description.visibility = View.GONE
            }
            Format2Mode.AudioBookRandom2.value,
            Format2Mode.Album2.value,
            Format2Mode.Party2.value -> {
                specialRow.visibility = View.VISIBLE
                specialLabel.text = getString(R.string.edit_special_label_from)
                specialDescription.visibility = View.VISIBLE
                specialDescription.text =
                    getString(R.string.edit_special_from, tagData.special.value?.toInt())
                special2Row.visibility = View.VISIBLE
                special2Label.text = getString(R.string.edit_special2_label_to)
                special2Description.visibility = View.VISIBLE
                special2Description.text =
                    getString(R.string.edit_special2_to, tagData.special2.value?.toInt())
            }
            else -> {
                // unknown modes
                specialRow.visibility = View.VISIBLE
                specialLabel.text = getString(R.string.edit_special_label)
                specialDescription.visibility = View.GONE
                specialDescription.text = getString(R.string.edit_hidden_label)
                special2Row.visibility = View.VISIBLE
                special2Label.text = getString(R.string.edit_special2_label)
                special2Description.visibility = View.GONE
                special2Description.text = getString(R.string.edit_hidden_label)
            }
        }
    }

    private fun hideAllDescriptions() {
        Log.d(TAG, "hideAllDescriptions")
        folderDescription.visibility = View.GONE
        modeDescription.visibility = View.GONE
        specialRow.visibility = View.GONE
        specialLabel.text = getString(R.string.edit_special_label)
        specialDescription.visibility = View.GONE
        special2Row.visibility = View.GONE
        special2Label.text = getString(R.string.edit_special2_label)
        special2Description.visibility = View.GONE
    }

    private fun initSpinnerValues() {
        val versions = resources.getStringArray(R.array.edit_version).asList().map { it.trim() }
        ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_item,
            versions
        ).also { adapter ->
            adapter.setNotifyOnChange(true)
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            version.adapter = adapter
        }

        // initialize spinner for 'folder'
        val folders = (1..FOLDER_MAX).map { it.toString().padStart(2, '0') }
        ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_item,
            folders
        ).also { adapter ->
            adapter.setNotifyOnChange(true)
            folder.adapter = adapter
        }

        setModeSpinnerValues()
    }

    /** Must be called multiple times because changing the version also changes the options here */
    private fun setModeSpinnerValues() {
        val allModes: List<String> = resources.getStringArray(R.array.edit_mode).asList()
        val modes = allModes.take(mode_max)
        ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_item,
            modes
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter.setNotifyOnChange(true)
            // Apply the adapter to the spinner
            mode.adapter = adapter
        }

        tagData.mode.value?.let { selectCurrentItemInModeSpinner(it.toInt()) }
    }

    private fun addUserEventListeners() {
        version.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}

            @ExperimentalUnsignedTypes
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position in 0 until VERSION_MAX) {
                    tagData.setVersion((position + 1).toUByte())
                }
                showDescriptions()
            }
        }

        folder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}

            @ExperimentalUnsignedTypes
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position in 0 until FOLDER_MAX) {
                    folderValue = (position + 1).toUByte()
                    tagData.setFolder(folderValue)
                }
                showDescriptions()
            }
        }

        mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}

            @ExperimentalUnsignedTypes
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position in 0 until mode_max) {
                    modeValue = (position + 1).toUByte()
                    tagData.setMode(modeValue)
                }
                showDescriptions()
            }
        }

        special.afterTextChanged { value ->
            Log.v(TAG, "special.afterTextChanged $value")
            val it = value.toUByteOrNull()
            if (it == null) {
                special.error = resources.getString(R.string.edit_limit_numeric_value, 0, 255)
            } else {
                special.error = null
                specialValue = it
                tagData.setSpecial(it)
                showDescriptions()
            }
        }

        special2.afterTextChanged { value ->
            Log.v(TAG, "special2.afterTextChanged $value")
            val it = value.toUByteOrNull()
            if (it == null) {
                special2.error = resources.getString(R.string.edit_limit_numeric_value, 0, 255)
            } else {
                special2.error = null
                special2Value = it
                tagData.setSpecial2(it)
                showDescriptions()
            }
        }
    }

    private fun addLiveDataEventListeners() {
        tagData.version.observe(viewLifecycleOwner, Observer { value: UByte ->
//            if (!version.hasFocus()) {
            Log.v(TAG, "version.observe $value")
            val adapter: ArrayAdapter<String> = version.adapter as ArrayAdapter<String>
            if (adapter.count > VERSION_MAX) {
                adapter.getItem(adapter.count - 1)?.let { item ->
                    Log.d(TAG, "Removing entry '$item' from version spinner")
                    adapter.remove(item)
                }
            }

            if (value.toInt() in 1..VERSION_MAX) {
                if (version.selectedItemPosition !== value.toInt() - 1) {
                    version.setSelection(value.toInt() - 1, false)
                }
            } else {
                val str = getString(R.string.edit_unsupported_value, value.toString())
                Log.d(TAG, "Will add '$str' to version spinner and select it")
                adapter.add(str)
                version.setSelection(adapter.count - 1, false)
            }
//            }
            setModeSpinnerValues()
        })

        tagData.folder.observe(viewLifecycleOwner, Observer { value: UByte ->
            if (!folder.hasFocus() && folderValue != value) {
                Log.v(TAG, "folder.observe $value")
                folderValue = value

                val adapter: ArrayAdapter<String> = folder.adapter as ArrayAdapter<String>
                if (adapter.count > FOLDER_MAX) {
                    adapter.getItem(FOLDER_MAX)?.let { item ->
                        Log.d(TAG, "Removing entry '$item' from folder spinner")
                        adapter.remove(item)
                    }
                }

                val folderIndex = value.toInt()
                if (folderIndex in 1..FOLDER_MAX) {
                    folder.setSelection(folderIndex - 1, false)
                } else {
                    val str = getString(R.string.edit_unsupported_value, value.toString())
                    Log.d(TAG, "Will add '$str' to folder spinner and select it")
                    adapter.add(str)
                    folder.setSelection(adapter.count - 1, false)
                }
            }
        })

        tagData.mode.observe(viewLifecycleOwner, Observer { value: UByte ->
            val int = value.toInt()
            if (!mode.hasFocus() && modeValue != value) {
                Log.v(TAG, "mode.observe $value")
                modeValue = value

                val adapter = mode.adapter as ArrayAdapter<String>
                if (adapter.count > mode_max) {
                    adapter.getItem(mode_max)?.let { item ->
                        Log.d(TAG, "Removing entry '$item' from mode spinner")
                        adapter.remove(item)
                    }
                }

                selectCurrentItemInModeSpinner(value.toInt())
            }
        })

        tagData.special.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "special.observe $value")
            specialValue = value
            special.setByteIfChanged(value)
        })

        tagData.special2.observe(viewLifecycleOwner, Observer { value: UByte ->
            Log.v(TAG, "special2.observe $value")
            special2Value = value
            special2.setByteIfChanged(value)
        })
    }

    private fun selectCurrentItemInModeSpinner(value: Int) {
        if (value in 1..mode_max) {
            mode.setSelection(value - 1, false)
        } else {
            val str = getString(R.string.edit_unsupported_value, value.toString())
            Log.d(TAG, "Will add '$str' to mode spinner and select it")
            val adapter = mode.adapter as ArrayAdapter<String>
            adapter.add(str)
            mode.setSelection(adapter.count - 1, false)
        }
    }
}

//TODO generalization to observe changes on spinner widgets
//@ExperimentalUnsignedTypes
//class MyObserver(
//    private val min: Int,
//    private val max: Int,
//    private val folder: Spinner,
//    private val caption: String
//) : Observer<UByte> {
//    override fun onChanged(value: UByte) {
//
//        if (!folder.hasFocus() && value.toString() != folder.selectedItem) {
//            Log.v(TAG, "folder.observe $value")
//
//            val adapter = folder.adapter as ArrayAdapter<String>
//            if (adapter.count >= FOLDER_MAX) {
//                adapter.getItem(FOLDER_MAX - 1)?.let { item ->
//                    Log.d(TAG, "Removing entry '$item' from folder spinner")
//                    adapter.remove(item)
//                }
//            }
//
//            val folderIndex = value.toInt()
//            if (folderIndex in 1..FOLDER_MAX) {
//                folder.setSelection(folderIndex - 1, false)
//            } else {
//                val str = getString(R.string.edit_unsupported_value, value.toString())
//                Log.d(TAG, "Will add '$str' to folder spinner and select it")
//                adapter.add(str)
//                folder.setSelection(FOLDER_MAX, false)
//            }
//        }
//    }
//}