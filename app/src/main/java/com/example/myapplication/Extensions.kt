package com.example.myapplication


import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText

//import java.lang.Exception

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun EditText.addValidator(whichByte: WhichByte, error: String, validator: (String) -> Boolean) {
    this.afterTextChanged {
        this.error = if (validator(it)) {
            (this.context as EditNfcData)?.setByte(whichByte, this.text.toString().toUByte())
            null
        } else error
    }
}

fun EditText.validateInputAndSetByte(which: WhichByte, min: Int = 0, max: Int = 255) {
    if (max > 255) throw IllegalArgumentException("max must not be greater than 255")
    if (max < 0) throw IllegalArgumentException("min must not be smaller than 0")
    if (min > max) throw java.lang.IllegalArgumentException("min must be smaller than max")
    return (this.addValidator(which, resources.getString(R.string.edit_limit_numeric_value, min, max)) { str ->
        try {
            val int = str.toInt()
            return@addValidator int >= min && int <= max
        } catch (ex: Exception) {
            return@addValidator false
        }
    })
}
