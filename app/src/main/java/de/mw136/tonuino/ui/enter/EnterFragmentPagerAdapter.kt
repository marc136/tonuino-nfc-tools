package de.mw136.tonuino.ui.enter

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import de.mw136.tonuino.R

private val TAB_TITLES = arrayOf(
    R.string.edit_tab_simple,
    R.string.edit_tab_extended,
    R.string.edit_tab_bytes
)

const val TAG = "enter.PagerAdapter"

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class EnterFragmentPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        Log.d(TAG, "getItem(position = $position)")
        // getItem is called to instantiate the fragment for the given page.
        // after it was created, the same instance is reused
        return when (position) {
            0 ->
                EnterSimple()
            1 ->
                Extended()
            2 ->
                PlaceholderFragment.newInstance(position + 1)
            else ->
                PlaceholderFragment.newInstance(position + 1)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int = 3
}
