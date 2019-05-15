package de.mw136.tonuino.ui

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
    }

    @Suppress("UNUSED_PARAMETER")
    fun writeTag(view: View) {
        if (tag == null) return

        Log.w("$TAG.writeTag", "will write to tag ${tagIdAsString(tag!!)}")
        val result = writeTonuino(tag!!, tagData)

        Log.w(TAG, "result ${result}")
    }

    override fun onNfcTag(tag: Tag) {
        this.tag = tag
        val tagId = tagIdAsString(tag)
        Log.i("$TAG.onNfcTag", "Tag $tagId")
//        supportActionBar?.title = getString(R.string.read_title, tagId)

        findViewById<Button>(R.id.write_button).text = getString(R.string.edit_write_button, tagId)
    }
}
