package de.mw136.tonuino


import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView


/**
 * In order for this to work, the textChanged events must abort when the element does not have focus.
 *
 * ```kotlin
 * editText.addTextChangedListener(object : TextWatcher {
 *     override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
 *         if (!myTextBox.hasFocus()) return
 *         // propagate changes
 *     }
 * })
 * ```
 *
 * See also https://stackoverflow.com/a/33151589
 */
fun EditText.setTextWithoutFocus(text: String) {
    val hadFocus = hasFocus()
    if (hadFocus) {
        clearFocus()
    }
    setText(text)
    if (hadFocus) {
        requestFocus()
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

@ExperimentalUnsignedTypes
fun EditText.setByteIfChanged(value: UByte?) {
    if (value == null || this.text.toString() == value.toString()) return
    Log.i(
        "Ton.setByteIfChanged",
        "hasFocus ${this.hasFocus()}, '${this.text}' != '$value'"
    )
    this.setText(value.toString())
}

fun TextView.setResArrayString(value: Int, resId1: Int, resId2: Int, fallback: String = "") {
    val titles = resources.getStringArray(resId1)
    val descriptions = resources.getStringArray(resId2)
    val max = if (titles.size < descriptions.size) titles.size else descriptions.size

    when {
        value in 0 until max -> {
            this.visibility = View.VISIBLE
            this.text = titles[value] + ": " + descriptions[value]
        }
        fallback.isNotBlank() -> {
            this.visibility = View.VISIBLE
            this.text = fallback
        }
        else -> {
            this.visibility = View.GONE
            this.text = ""
        }
    }
}

fun TextView.setTextOrHideIfBlank(string: String?) {
    if (string.isNullOrBlank()) {
        this.visibility = View.VISIBLE
        this.text = string
    } else {
        this.visibility = View.INVISIBLE
    }
}