package de.mw136.tonuino

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Patterns and Antipatterns about ViewModels https://medium.com/androiddevelopers/viewmodels-and-livedata-patterns-antipatterns-21efaef74a54

// Information how to save/load data, see first https://developer.android.com/jetpack/docs/guide#overview
// and then https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate

class BulkEditViewModel : ViewModel() {
    // https://medium.com/@taman.neupane/basic-example-of-livedata-and-viewmodel-14d5af922d0
    private val _lines: MutableLiveData<List<String>> = abc()
    val lines: LiveData<List<String>>
        get() = _lines
    private val _currentLine: MutableLiveData<Int> = MutableLiveData(0)
    val currentLine: LiveData<Int>
        get() = _currentLine

    fun abc(): MutableLiveData<List<String>> {
        Log.w("Bulk", "BulkEditViewModel constructor was called")
        return MutableLiveData(listOf("abc", "def"))
    }

    fun setLines(input: CharSequence) {
        Log.w("Bulk", "setLines with ${input.toString()}")
        _lines.value = input.lines()
        Log.w("Bulk", "lines is now ${_lines.value?.joinToString("/n")}")
    }

}
