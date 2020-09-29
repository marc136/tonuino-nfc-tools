package de.mw136.tonuino.ui

import android.app.Activity
import android.content.Intent
import android.nfc.FormatException
import android.nfc.Tag
import android.nfc.tech.TagTechnology
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import de.mw136.tonuino.BulkEditViewModel
import de.mw136.tonuino.R
import de.mw136.tonuino.nfc.NfcIntentActivity
import de.mw136.tonuino.nfc.connectTo
import de.mw136.tonuino.nfc.tagIdAsString
import kotlinx.android.synthetic.main.bulkwrite_fragment_enter_list.*
import java.io.IOException


@ExperimentalUnsignedTypes
class BulkWriteActivity : NfcIntentActivity() {
    override val TAG = "BulkActivity"
    private val REQUEST_CODE_QR_SCAN = 4711

    var tag: TagTechnology? = null
    private val handler = Handler()
    private lateinit var isTagConnected: Runnable
    private val model: BulkEditViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bulkwrite_activity)

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        // Use the 'by viewModels()' Kotlin property delegate
        // from the activity-ktx artifact
        model.currentLine.observe(this, Observer<String> { _ ->
            // update UI
        })

        isTagConnected = Runnable {
            if (tag?.isConnected == true) {
                // should be able to write to tag
                pollTag()

            } else {
                model.removeTag()
            }
        }
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

    override fun onNfcTag(tag: Tag) {
        val tagId = tagIdAsString(tag)
        Log.i("$TAG.onNfcTag", "Tag $tagId")
//        supportActionBar?.title = getString(R.string.read_title, tagId)
        try {
            connectTo(tag)?.let {
                this.tag = it
                model.setTag(it)
            }
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

    fun openQRCodeReader(view: View) {
        startActivityForResult(Intent(view.context, QRCodeScanner::class.java), REQUEST_CODE_QR_SCAN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_QR_SCAN && data != null) {
            val result = data.getStringExtra("de.mw136.tonuino.ui.qrcode_result")
            editText.setText(result)
        }
    }
}
