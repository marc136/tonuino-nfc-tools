package com.example.myapplication

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import java.lang.RuntimeException

@ExperimentalUnsignedTypes
class EditPagerAdapter(fm: FragmentManager?, val fragments: Array<EditFragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return this.fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
