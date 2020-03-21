package de.mw136.tonuino

import android.nfc.tech.TagTechnology
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

const val TAG = "BulkEditViewModel"

// Patterns and Antipatterns about ViewModels https://medium.com/androiddevelopers/viewmodels-and-livedata-patterns-antipatterns-21efaef74a54

// Information how to save/load data, see first https://developer.android.com/jetpack/docs/guide#overview
// and then https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate

@ExperimentalUnsignedTypes
class BulkEditViewModel : ViewModel() {
    // https://medium.com/@taman.neupane/basic-example-of-livedata-and-viewmodel-14d5af922d0
    var lines: List<String> = if (BuildConfig.DEBUG) {
        listOf(
            "1337B3470101040500;Erste Karte",
            "1337B3470204083D5A;Guten Morgen"
        )
    } else {
        listOf()
    }
        private set

    var currentLineIndex: Int = 0
        private set
    private var _currentLine: MutableLiveData<String> = MutableLiveData(lines[currentLineIndex])
    val currentLine: LiveData<String> = _currentLine

    val lineCount: Int
        get() = lines.size

    fun setLines(input: CharSequence) {
        lines = input.lines()
        Log.w("Bulk", "lines is now ${lines.joinToString("/n")}")
        if (lines.isNotEmpty() && lines[0].isNotEmpty()) {
            _currentLine.value = lines[0]
        }
    }

    val hasNext: Boolean
        get() = lines.size > currentLineIndex + 1

    fun nextLine() {
        if (currentLineIndex + 1 < lines.size) {
            currentLineIndex++
            _currentLine.value = lines[currentLineIndex]
        }
    }

    val hasPrevious: Boolean
        get() = currentLineIndex > 0

    fun previousLine() {
        if (currentLineIndex > 0) {
            currentLineIndex--
            _currentLine.value = lines[currentLineIndex]
        }
    }

    // NFC tag entity for writing

    private var _tag: MutableLiveData<TagTechnology?> = MutableLiveData(null)
    val tag: LiveData<TagTechnology?> = _tag

    fun removeTag() {
        _tag.value = null
    }

    fun setTag(tech: TagTechnology) {
        _tag.value = tech
    }
}


@ExperimentalUnsignedTypes
class TagWithComment(val bytes: UByteArray, val title: String) {

    companion object {
        fun of(str: String): TagWithComment? {
            // TODO add tests
            val parts = str.split(";", " ")
            if (parts.isNotEmpty()) {
                val first: UByteArray = hexToBytes(parts[0])
                val rest = parts.drop(1).joinToString(" ")
                Log.w("Bulk", "first: $first")
                Log.w("Bulk", "rest: $rest")
                if (first.isNotEmpty()) {
                    return TagWithComment(first, rest)
                }
            }
            return null
        }
    }
}