package de.mw136.tonuino.ui

import android.app.AlertDialog
import android.nfc.FormatException
import android.nfc.Tag
import android.nfc.tech.TagTechnology
import android.os.Bundle
import android.os.Handler
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import android.util.Log
import android.view.View
import android.widget.*
import de.mw136.tonuino.*
import de.mw136.tonuino.nfc.*
import de.mw136.tonuino.ui.edit.*
import java.io.IOException

@ExperimentalUnsignedTypes
class EditActivity : NfcIntentActivity(), EditNfcData {
    override val TAG = "EditActivity"

    var tag: TagTechnology? = null
    private val handler = Handler()
    private lateinit var isTagConnected: Runnable

    override lateinit var tagData: TagData
    private val editSimpleContainer = EditSimpleContainer()

    override val fragments: Array<EditFragment> = arrayOf(
        editSimpleContainer,
        EditExtended(),
        EditHex()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val tagType = findViewById<Spinner>(R.id.tag_type_selector)
        tagType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                Log.i(TAG, "TODO tag type selection: selected ${position}")
                val version: UByte = when (position) {
                    0 -> 1u
                    1, 2 -> 2u
                    else -> {
                        Log.e(TAG, "tag type at position ${position} was not implememented.")
                        return
                    }
                }

                when (position) {
                    2 ->
                        editSimpleContainer.useModifierTagEditUi()
                    else ->
                        editSimpleContainer.useNormalTagEditUi()
                }

                if (tagData.version != version) {
                    setByte(WhichByte.VERSION, version, fullRefresh = true)
                }
            }
        }

        // Switch between input implementations
        val viewPager = findViewById<androidx.viewpager.widget.ViewPager>(R.id.edit_main_pager)
        viewPager.adapter = EditPagerAdapter(supportFragmentManager, fragments)
        val tabLayout = findViewById<TabLayout>(R.id.edit_main_tabs)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // We could access the fragment that will be shown with
                // `(viewPager.adapter as FragmentPagerAdapter)?.getItem(tab.position)`
                // but when calling refreshUi on it, it will throw an error
                viewPager.currentItem = tab.position
//                this@EditActivity.currentEditFragment =
//                    (viewPager.adapter as FragmentPagerAdapter).getItem(tab.position) as EditFragment
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tagData = intent.getParcelableExtra(PARCEL_TAGDATA) ?: TagData.createDefault()

        isTagConnected = Runnable {
            if (tag?.isConnected == true) {
                // should be able to write to tag
                pollTag()

            } else {
                tag = null
                enableWriteButtonIfTagPresent()
            }
        }

        intent.getParcelableExtra<Tag>(PARCEL_TAG)?.let { tag -> onNfcTag(tag) }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        pollTag()
    }

    private fun pollTag() {
        handler.postDelayed(isTagConnected, 321)
    }

    private fun enableWriteButtonIfTagPresent() {
        findViewById<Button>(R.id.write_button)?.apply {
            if (this@EditActivity.tag == null) {
                setText(getString(R.string.edit_write_button_no_tag))
                isEnabled = false
                Toast.makeText(this@EditActivity, "Verbindung verloren", Toast.LENGTH_LONG).show()
            } else {
                setText(getString(R.string.edit_write_button, tagIdAsString(this@EditActivity.tag!!)))
                isEnabled = true
                pollTag()
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickWriteTagButton(view: View) = writeTag()

    private fun writeTag() {
        var result = WriteResult.TAG_UNAVAILABLE
        if (tag != null) {
            Log.w("$TAG.writeTag", "will write to tag ${tagIdAsString(tag!!)}")
            result = writeTonuino(tag!!, tagData)
            Log.w("$TAG.writeTag", "result ${result}")
        }
        showModalDialog(result)
    }

    private fun showModalDialog(result: WriteResult) {
        with(AlertDialog.Builder(this)) {
            var showRetryButton = false

            when (result) {
                WriteResult.SUCCESS -> {
                    setMessage(R.string.written_success)
                }
                WriteResult.UNSUPPORTED_FORMAT -> {
                    setTitle(R.string.written_unsupported_tag_type)
                    setMessage(getString(R.string.nfc_tag_technologies, techListOf(tag).joinToString(", ")))
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
                    setMessage(getString(R.string.nfc_tag_technologies, techListOf(tag).joinToString(", ")))
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

    override fun onNfcTag(tag: Tag) {
        val tagId = tagIdAsString(tag)
        Log.i("$TAG.onNfcTag", "Tag $tagId")
//        supportActionBar?.title = getString(R.string.read_title, tagId)
        try {
            this.tag = connectTo(tag)
            enableWriteButtonIfTagPresent()
        } catch (ex: IOException) {
            // could not connect to tag
            Log.w("$TAG.onNfcTag", "Could not connect to the NFC tag")
        } catch (ex: FormatException) {
            // unsupported tag format
            Log.w("$TAG.onNfcNtag", "Unsupported format")
        } catch (ex: Exception) {
            // TODO display unexpected error
            Log.e("$TAG.onNfcTag", ex.toString())
        }
    }
}
