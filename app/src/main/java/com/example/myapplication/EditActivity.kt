package com.example.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager


@ExperimentalUnsignedTypes
class EditActivity : AppCompatActivity(), EditNfcData {
    public override var bytes: UByteArray = ubyteArrayOf(1u, 2u, 3u, 4u, 5u)
    public lateinit var cardData: CardData
    public override var currentEditFragment: EditFragment? = null
    override var triggerRefreshTextOnCurrentFragment: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        cardData = CardData()

        val viewPager = findViewById<ViewPager>(R.id.edit_main_pager)
        viewPager.adapter = EditPagerAdapter(supportFragmentManager)
        val tabLayout = findViewById<TabLayout>(R.id.edit_main_tabs)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // We could access the fragment that will be shown with
                // `(viewPager.adapter as FragmentPagerAdapter)?.getItem(tab.position)`
                // but when calling refreshText on it, it will throw an error
                viewPager.currentItem = tab.position
//                this@EditActivity.currentEditFragment =
//                    (viewPager.adapter as FragmentPagerAdapter).getItem(tab.position) as EditFragment
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        triggerRefreshTextOnCurrentFragment = true
    }
}
