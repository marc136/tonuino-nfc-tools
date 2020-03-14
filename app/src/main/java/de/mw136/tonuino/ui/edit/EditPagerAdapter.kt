package de.mw136.tonuino.ui.edit

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

@ExperimentalUnsignedTypes
class EditPagerAdapter(fm: FragmentManager?, val fragments: Array<EditFragment>) :
    FragmentPagerAdapter(fm!!, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return this.fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
