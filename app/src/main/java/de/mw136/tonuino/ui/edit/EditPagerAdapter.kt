package de.mw136.tonuino.ui.edit

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

@ExperimentalUnsignedTypes
class EditPagerAdapter(fm: FragmentManager?, val fragments: Array<EditFragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return this.fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
