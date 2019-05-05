package com.example.myapplication

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView

@ExperimentalUnsignedTypes
class MainActivity : NfcIntentActivity() {
    override val TAG = "MainActivity"
    private var nfcAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val errorContainer = findViewById<View>(R.id.error_container)
        val errorView = findViewById<TextView>(R.id.error_text)

        if (nfcAdapter == null) {
            errorContainer.visibility = View.VISIBLE
            errorView.text = getString(R.string.main_nfcadapter_is_null)
        } else if (!nfcAdapter!!.isEnabled) {
            errorContainer.visibility = View.VISIBLE
            errorView.text = getString(R.string.main_nfcadapter_disabled)
        } else {
            nfcAvailable = true
            errorContainer.visibility = View.GONE
        }
    }

    fun showWriteActivity(view: View) {
        startActivity(Intent(view.context, EditActivity::class.java))
    }

    override fun onNfcTag(tag: Tag) {
        val bytes = readFromTag(tag)
        Log.d(TAG, "bytes: ${byteArrayToHex(bytes).joinToString(" ")}")
        if (bytes.isNotEmpty()) {
            val intent = Intent(this, ReadActivity::class.java).apply {
                putExtra(PARCEL_TAG, tag)
                putExtra(PARCEL_TAGDATA, TagData(bytes))
            }
            startActivity(intent)
        }
    }
}
