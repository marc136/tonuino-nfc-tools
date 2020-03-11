package de.mw136.tonuino.ui

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import de.mw136.tonuino.*
import de.mw136.tonuino.nfc.NfcIntentActivity
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.readFromTag
import de.mw136.tonuino.BuildConfig

@ExperimentalUnsignedTypes
class MainActivity : NfcIntentActivity() {
    override val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val version = BuildConfig.VERSION_NAME + "#" + BuildConfig.VERSION_CODE
        Log.i(TAG,"Version ${BuildConfig.VERSION_NAME} build #${BuildConfig.VERSION_CODE}")
        supportActionBar?.title = "${getString(R.string.app_name)} ${getString(R.string.app_version, version)}"

        val errorContainer = findViewById<View>(R.id.error_container)
        val errorView = findViewById<TextView>(R.id.error_text)
        val openNfcSettingsButton = findViewById<View>(R.id.nfc_settings)
        val enabledContainer = findViewById<View>(R.id.enabled_container)

        if (nfcAdapter == null) {
            errorContainer.visibility = View.VISIBLE
            errorView.text = getString(R.string.main_nfcadapter_is_null)
            openNfcSettingsButton.visibility = View.GONE
            enabledContainer.visibility = View.GONE

        } else if (!nfcAdapter!!.isEnabled) {
            errorContainer.visibility = View.VISIBLE
            errorView.text = getString(R.string.main_nfcadapter_disabled)
            openNfcSettingsButton.visibility = View.VISIBLE
            enabledContainer.visibility = View.GONE

        } else {
            enabledContainer.visibility = View.VISIBLE
            errorContainer.visibility = View.GONE
        }

        if (BuildConfig.DEBUG) {
            enabledContainer.visibility = View.VISIBLE
        }
    }

    fun showWriteActivity(view: View) {
        startActivity(Intent(view.context, EditActivity::class.java))
    }

    @Suppress("UNUSED_PARAMETER")
    fun openNfcSettings(view: View) {
        startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
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
        } else {
            showReadErrorModalDialog(tag)
        }
    }
}
