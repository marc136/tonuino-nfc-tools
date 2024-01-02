package de.mw136.tonuino.ui.enter

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.nfc.tonuinoCookie

private const val COOKIE = 0
private const val VERSION = 4
private const val FOLDER = 5
private const val MODE = 6
private const val SPECIAL = 7
private const val SPECIAL2 = 8

@ExperimentalUnsignedTypes
class TagData() : ViewModel(), Parcelable {
    private var _bytes: UByteArray = default()
    val bytes: UByteArray get() = _bytes
    fun setBytes(arr: UByteArray) {
        var same = false
        if (arr.size == _bytes.size && arr.isNotEmpty()) {
            for (index in arr.indices) {
                same = arr[index] == _bytes[index]
                if (!same) break
            }
        }
        if (same) return

        if (arr.size <= SPECIAL2) {
            // ensure the UByteArray is big enough (`.plus(0u)` crashed the app)
            val buffer = UByteArray(SPECIAL2 + 1) { 0u }
            arr.forEachIndexed { index, value -> buffer[index] = value }
            _bytes = buffer
        } else {
            _bytes = arr
        }

        _version.value = _bytes[VERSION]
        _folder.value = _bytes[FOLDER]
        _mode.value = _bytes[MODE]
        _special.value = _bytes[SPECIAL]
        _special2.value = _bytes[SPECIAL2]
    }

    val cookie: UByteArray
        get() {
            return _bytes.sliceArray(COOKIE until VERSION)
        }

    fun setCookie(value: UByteArray) {
        for (index in 0 until VERSION) {
            _bytes[index] = if (value.size > index) value[index] else 0u
        }
    }

    private var _version: MutableLiveData<UByte> = MutableLiveData(_bytes[VERSION])
    val version: LiveData<UByte> get() = _version
    fun setVersion(value: UByte) {
        _bytes[VERSION] = value
        _version.value = value
    }

    private var _folder: MutableLiveData<UByte> = MutableLiveData(_bytes[FOLDER])
    val folder: LiveData<UByte> get() = _folder
    fun setFolder(value: UByte) {
        if (_bytes[FOLDER] == value) return
        _bytes[FOLDER] = value
        _folder.value = value
    }

    private var _mode: MutableLiveData<UByte> = MutableLiveData(_bytes[MODE])
    val mode: LiveData<UByte> get() = _mode
    fun setMode(value: UByte) {
        _bytes[MODE] = value
        _mode.value = value
    }

    private var _special: MutableLiveData<UByte> = MutableLiveData(_bytes[SPECIAL])
    val special: LiveData<UByte> get() = _special
    fun setSpecial(value: UByte) {
        _bytes[SPECIAL] = value
        _special.value = value
    }

    private var _special2: MutableLiveData<UByte> = MutableLiveData(_bytes[SPECIAL2])
    val special2: LiveData<UByte> get() = _special2

    fun setSpecial2(value: UByte) {
        _bytes[SPECIAL2] = value
        _special2.value = value
    }

    override fun toString(): String {
        return "TagData<${byteArrayToHex(_bytes).joinToString(" ")}>"
    }

    constructor(arr: UByteArray) : this() {
        setBytes(arr)
    }

    constructor(parcel: Parcel) : this() {
        setBytes(parcel.createByteArray()?.toUByteArray() ?: default())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(_bytes.toByteArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TagData> {
        private fun default(): UByteArray {
            val buffer = UByteArray(SPECIAL2 + 1) { 0u }
            // TODO load tonuinoCookie from settings
            // see https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
            // and https://proandroiddev.com/customizing-the-new-viewmodel-cf28b8a7c5fc
            tonuinoCookie.forEachIndexed { index, value -> buffer[COOKIE + index] = value }
            // TODO load default version from settings
            buffer[VERSION] = 2u
            buffer[FOLDER] = 1u
            buffer[MODE] = 1u

            return buffer
        }

        override fun createFromParcel(parcel: Parcel): TagData {
            return TagData(parcel)
        }

        override fun newArray(size: Int): Array<TagData?> {
            return arrayOfNulls(size)
        }
    }
}
