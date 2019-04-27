package com.example.myapplication

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.View


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), EditSimple.OnFragmentInteractionListener,
    EditExtended.OnFragmentInteractionListener {
    private var currentScreen: Fragment? = null
    public override var bytes: UByteArray = ubyteArrayOf(1u, 2u, 3u, 4u, 5u)
    public lateinit var cardData: CardData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardData = CardData()

//        // Add a backstack listener so we get the handle for the new screen when user presses back
//        supportFragmentManager.addOnBackStackChangedListener {
//            currentScreen = supportFragmentManager
//                .findFragmentById(R.id.mainactivity_content) as BaseFragment?
//        }
        
        val viewPager = findViewById<ViewPager>(R.id.mainactivity_pager)
        viewPager.adapter = EditPagerAdapter(supportFragmentManager)
        val tabLayout = findViewById<TabLayout>(R.id.mainactivity_tabs)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    public fun loadSimpleEditFragment(v: View) {
        val fragment = EditSimple()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.mainactivity_pager, fragment)
//            .commit();
//        onTransition(null, fragment);
    }

    /*
     * Interactions
     */
//    fun transitionTo(newFragment: Fragment): Int {
//        // Setup animation
//        val ft = supportFragmentManager.beginTransaction()
//        ft.setCustomAnimations(
//            R.anim.slide_in_right, R.anim.slide_out_left, //entering fragment animation, exiting fragment animation
//            R.anim.slide_in_left, R.anim.slide_out_right
//        ) //reverse for on back pressed
//            .replace(R.id.mainactivity_content, newFragment, newFragment.getClass().getSimpleName())
//            .addToBackStack(newFragment.getClass().getSimpleName())
//
//        // Perform animation
//        val id = ft.commit()
//
//        onTransition(currentScreen, newFragment)
//
//        return id
//    }

    protected fun onTransition(oldFragment: Fragment?, newFragment: Fragment) {
        currentScreen = newFragment
        //TODO: e.g. analytics tracking
    }


    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
