package de.mw136.tonuino.nfc

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.mw136.tonuino.R

@ExperimentalUnsignedTypes
abstract class NfcIntentActivity : AppCompatActivity() {
    private lateinit var pendingIntent: PendingIntent
    protected var nfcAdapter: NfcAdapter? = null
    protected abstract val TAG: String

    abstract fun onNfcTag(tag: Tag)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNfcTagIntents()
    }

    private fun initNfcTagIntents() {
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onPause() {
        Log.i(TAG, "onPause()")
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onResume() {
        Log.i(TAG, "onResume()")
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            NfcIntentHelper.intentFilters,
            NfcIntentHelper.techLists
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) ?: return
        Log.i(TAG, "onNewIntent tag: ${tag.id}")

        val id = tagIdAsString(tag)
        Toast.makeText(this, getString(R.string.nfc_tag_found, id), Toast.LENGTH_LONG).show()

        return onNfcTag(tag)
    }

    fun showReadErrorModalDialog(tag: Tag) {
        with(AlertDialog.Builder(this)) {
            setTitle(R.string.nfc_read_tag_failure)
            setMessage(getString(R.string.nfc_tag_technologies, techListOf(tag).joinToString(", ")))

            setPositiveButton(getString(R.string.button_ok)) { _, _ -> }
            create().show()
        }
    }

    companion object {
        const val PARCEL_TAG = "tag"
        const val PARCEL_TAGDATA = "bytes"
    }
}

object NfcIntentHelper {
    val intentFilters: Array<IntentFilter>
    val techLists: Array<Array<String>>

    init {
        // Order of intents: ACTION_NDEF_DISCOVERED > ACTION_TECH_DISCOVERED > ACTION_TAG_DISCOVERED
        // https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#filter-intents
        val intentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                // Currently catches all MIME types, should be refined if actually used
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Compile time error: MalformedMimeTypeException", e)
            }
        }
        intentFilters = arrayOf(intentFilter, IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))

        // NfcA is a superset of Mifare Classic (= Mifare Standard), but not all devices support Mifare Classic
        // https://developer.android.com/reference/android/nfc/tech/TagTechnology.html
        techLists = arrayOf(arrayOf(NfcA::class.java.name), arrayOf(NfcB::class.java.name))
    }
}
