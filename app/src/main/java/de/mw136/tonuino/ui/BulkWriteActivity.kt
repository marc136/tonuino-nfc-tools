package de.mw136.tonuino.ui

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import de.mw136.tonuino.BulkEditViewModel
import de.mw136.tonuino.R
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.nfc.NfcIntentActivity
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.readFromTag

import kotlinx.android.synthetic.main.bulkwrite_activity.*

@ExperimentalUnsignedTypes
class BulkWriteActivity : NfcIntentActivity() {
    override val TAG = "BulkActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bulkwrite_activity)

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        // Use the 'by viewModels()' Kotlin property delegate
        // from the activity-ktx artifact
        val model: BulkEditViewModel by viewModels()
        model.currentLine.observe(this, Observer<String>{ _ ->
            // update UI
        })


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }


    override fun onNfcTag(tag: Tag) {
        Log.d(TAG, "onNfcTag ${byteArrayToHex(tag.id.toUByteArray())}")
        val bytes = readFromTag(tag)
        Log.d(TAG, "bytes: ${byteArrayToHex(bytes).joinToString(" ")}")

        if (bytes.isNotEmpty()) {

        }
    }
}
