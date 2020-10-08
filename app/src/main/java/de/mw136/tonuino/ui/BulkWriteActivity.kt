package de.mw136.tonuino.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import de.mw136.tonuino.utils.RuntimePermission
import kotlinx.android.synthetic.main.bulkwrite_fragment_enter_list.*
import java.io.IOException

@ExperimentalUnsignedTypes
class BulkWriteActivity : NfcIntentActivity() {
    override val TAG = "BulkActivity"
    private val REQUEST_CODE_CAMERA = 4710
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
        model.currentLine.observe(this, Observer<String> {
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

    override fun onStart() {
        super.onStart()
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            button_qrcode.visibility = View.VISIBLE
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

    fun openQRCodeScanner(view: View) {
        // request runtime permission for camera for API23+
        if (RuntimePermission.askFor(this, Manifest.permission.CAMERA, REQUEST_CODE_CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(Intent(view.context, QRCodeScannerActivity::class.java), REQUEST_CODE_QR_SCAN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_QR_SCAN && data != null) {
            val result = data.getStringExtra("de.mw136.tonuino.ui.qrcode_result")
            editText.setText(result)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_CAMERA ->
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    startActivityForResult(Intent(this, QRCodeScannerActivity::class.java), REQUEST_CODE_QR_SCAN)
                }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
