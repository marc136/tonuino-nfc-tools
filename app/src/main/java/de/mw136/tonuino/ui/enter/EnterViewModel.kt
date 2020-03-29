package de.mw136.tonuino.ui.enter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.mw136.tonuino.nfc.tonuinoCookie

private const val COOKIE = 0
private const val VERSION = 4
private const val FOLDER = 5
private const val MODE = 6
private const val SPECIAL = 7
private const val SPECIAL2 = 8

@ExperimentalUnsignedTypes
class EnterViewModel : ViewModel() {
    var bytes: UByteArray = EnterViewModel.default()
        private set

    val cookie: UByteArray
        get() {
            return bytes.sliceArray(COOKIE until VERSION)
        }

    fun setCookie(value: UByteArray) {
        TODO("Implement setCookie")
    }

    private var _version: MutableLiveData<UByte> = MutableLiveData(bytes[VERSION])
    val version: LiveData<UByte> get() = _version
    fun setVersion(value: UByte) {
        bytes[VERSION] = value
        _version.value = value
    }

    private var _folder: MutableLiveData<UByte> = MutableLiveData(bytes[FOLDER])
    val folder: LiveData<UByte> get() = _folder
    fun setFolder(value: UByte) {
        bytes[FOLDER] = value
        _folder.value = value
    }

    private var _mode: MutableLiveData<UByte> = MutableLiveData(bytes[MODE])
    val mode: LiveData<UByte> get() = _mode
    fun setMode(value: UByte) {
        bytes[MODE] = value
        _mode.value = value
    }

    private var _special: MutableLiveData<UByte> = MutableLiveData(bytes[SPECIAL])
    val special: LiveData<UByte> get() = _special
    fun setSpecial(value: UByte) {
        bytes[SPECIAL] = value
        _special.value = value
    }

    private var _special2: MutableLiveData<UByte> = MutableLiveData(bytes[SPECIAL2])
    val special2: LiveData<UByte> get() = _special2
    fun setSpecial2(value: UByte) {
        bytes[SPECIAL2] = value
        _special2.value = value
    }

    companion object {
        private fun default(): UByteArray {
            val buffer = UByteArray(SPECIAL2 + 1) { 0u }
            tonuinoCookie.forEachIndexed { index, value -> buffer[COOKIE + index] = value }
            buffer[VERSION] = 1u
            buffer[FOLDER] = 1u
            buffer[MODE] = 1u

            return buffer
        }
    }
}
