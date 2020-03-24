package de.mw136.tonuino.ui

import android.nfc.Tag
import android.os.Bundle
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import de.mw136.tonuino.R
import de.mw136.tonuino.nfc.NfcIntentActivity
import de.mw136.tonuino.ui.enter.EnterViewModel
import de.mw136.tonuino.ui.enter.EnterFragmentPagerAdapter

@ExperimentalUnsignedTypes
class EnterTagActivity : NfcIntentActivity() {
    override val TAG = "EnterTagActivity"

    private val tagData: EnterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_tag)
        val sectionsPagerAdapter = EnterFragmentPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    override fun onNfcTag(tag: Tag) {
        TODO("Not yet implemented")
    }

}

