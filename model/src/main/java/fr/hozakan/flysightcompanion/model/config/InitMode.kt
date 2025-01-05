package fr.hozakan.flysightcompanion.model.config

enum class InitMode(
    val value: Int
) {
    DoNothing(0),
    TestSpeechMode(1),
    PlayFile(2);

    companion object {

        fun fromValue(value: Int): InitMode? {
            return entries.firstOrNull { it.value == value }
        }
    }
}