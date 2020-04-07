package de.mw136.tonuino.ui

import android.app.AlertDialog
import android.nfc.FormatException
import android.nfc.Tag
import android.nfc.tech.TagTechnology
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import de.mw136.tonuino.R
import de.mw136.tonuino.nfc.*
import de.mw136.tonuino.ui.enter.EnterFragmentPagerAdapter
import de.mw136.tonuino.ui.enter.TagData
import java.io.IOException

@ExperimentalUnsignedTypes
class EnterTagActivity : NfcIntentActivity() {
    override val TAG = "EnterTagActivity"

    private val tagData: TagData by viewModels()


    var tag: TagTechnology? = null
    private lateinit var isTagConnected: Runnable
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getParcelableExtra<TagData>(PARCEL_TAGDATA)?.let {
            Log.i(TAG, "Found parceled tagData $it and will overwrite the current values")
            tagData.setBytes(it.bytes)
        }
        Log.i(TAG, tagData.toString())

        setContentView(R.layout.enter_activity)
        val sectionsPagerAdapter = EnterFragmentPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

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
        findViewById<Button>(R.id.button_write)?.apply {
            if (this@EnterTagActivity.tag == null) {
                text = getString(R.string.edit_write_button_no_tag)
                isEnabled = false
                Toast.makeText(
                    this@EnterTagActivity,
                    getString(R.string.edit_nfc_connection_lost),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                text = getString(
                    R.string.edit_write_button,
                    tagIdAsString(this@EnterTagActivity.tag!!)
                )
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
            result = writeTonuino(tag!!, tagData.bytes)
            Log.w("$TAG.writeTag", "result $result")
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
                    setMessage(
                        getString(
                            R.string.nfc_tag_technologies,
                            techListOf(tag).joinToString(", ")
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
                            techListOf(tag).joinToString(", ")
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
            Log.w("$TAG.onNfcTag", "Unsupported format")
        } catch (ex: Exception) {
            // TODO display unexpected error
            Log.e("$TAG.onNfcTag", ex.toString())
        }
    }
}

