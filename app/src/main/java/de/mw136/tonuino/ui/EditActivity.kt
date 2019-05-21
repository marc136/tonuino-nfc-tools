package de.mw136.tonuino.ui

import android.app.AlertDialog
import android.nfc.Tag
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.widget.Button
import de.mw136.tonuino.*
import de.mw136.tonuino.nfc.*
import de.mw136.tonuino.ui.edit.*


@ExperimentalUnsignedTypes
class EditActivity() : NfcIntentActivity(), EditNfcData {
    override val TAG = "EditActivity"

    var tag: Tag? = null
    public override lateinit var tagData: TagData
    override val fragments: Array<EditFragment> = arrayOf(
        EditSimple(),
        EditExtended(),
        EditHex()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        tag = intent.getParcelableExtra<Tag>(PARCEL_TAG) ?: null
        tagData = intent.getParcelableExtra<TagData>(PARCEL_TAGDATA) ?: TagData.createDefault()

        val viewPager = findViewById<ViewPager>(R.id.edit_main_pager)
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

        enableWriteButtonIfTagPresent()
    }

    fun enableWriteButtonIfTagPresent() {
        findViewById<Button>(R.id.write_button)?.apply {
            if (this@EditActivity.tag == null) {
                setText(getString(R.string.edit_write_button_no_tag))
                isEnabled = false
            } else {
                setText(getString(R.string.edit_write_button, tagIdAsString(this@EditActivity.tag!!)))
                isEnabled = true
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickWriteTagButton(view: View) {
        if (tag == null) {
            showModalDialog(WriteResult.TAG_UNAVAILABLE)
        } else {
            writeTag()
        }
    }

    private fun writeTag() {
        Log.w("$TAG.writeTag", "will write to tag ${tagIdAsString(tag!!)}")
        val result = writeTonuino(tag!!, tagData)
        Log.w(TAG, "result ${result}")

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
        this.tag = tag
        val tagId = tagIdAsString(tag)
        Log.i("$TAG.onNfcTag", "Tag $tagId")
//        supportActionBar?.title = getString(R.string.read_title, tagId)

        enableWriteButtonIfTagPresent()
    }
}
