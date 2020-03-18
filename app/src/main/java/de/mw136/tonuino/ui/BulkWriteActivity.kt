package de.mw136.tonuino.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import de.mw136.tonuino.BulkEditViewModel
import de.mw136.tonuino.R

import kotlinx.android.synthetic.main.bulkwrite_activity.*

@ExperimentalUnsignedTypes
class BulkWriteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bulkwrite_activity)

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        // Use the 'by viewModels()' Kotlin property delegate
        // from the activity-ktx artifact
        val model: BulkEditViewModel by viewModels()
        model.currentLine.observe(this, Observer<String>{ _ ->
            // update UI
        })


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

}
