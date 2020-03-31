package de.mw136.tonuino.ui

@ExperimentalUnsignedTypes
class Tonuino {
    companion object {
        // TODO decide if I want this format or enum classes (which cannot pattern-match nicely)
        val format1: UByte = 1u
        val format2: UByte = 2u

        val mode = object {
            val AudioBookRandom: UByte = 1u
        }
    }
}

@ExperimentalUnsignedTypes
enum class TonuinoFormat(val value: UByte) {
    Format1(1u), Format2(2u)
}

@ExperimentalUnsignedTypes
enum class Format1Mode(val value: Int) {
    AudioBookRandom(1),
    Album(2),
    Party(3),
    Single(4),
    AudioBookMultiple(5),
    Admin(6),
}

@ExperimentalUnsignedTypes
enum class Format2Mode(val value: Int) {
    AudioBookRandom2(7),
    Album2(8),
    Party2(9)
}

@ExperimentalUnsignedTypes
fun usageExample1() {
    val version: UByte = 1u
    var a1 = when (version) {
        Tonuino.format1 ->
            TODO()
        Tonuino.format2 ->
            TODO()
        else ->
            1
    }

    var a2 = when (version.toInt()) {
        Format1Mode.AudioBookRandom.value,
        Format1Mode.AudioBookMultiple.value ->
            TODO()
        else ->
            1
    }

//    The following is not possible
//    var a3 = when (version) {
//        Tonuino.mode.AudioBookRandom ->
//            TODO()
//        else ->
//            1
//    }
}


