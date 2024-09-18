package fr.hozakan.flysightble.model.config

enum class InitMode(
    val value: Int,
    val text: String
) {
    DoNothing(0, "Do nothing"),
    TestSpeechMode(1, "Test speech mode"),
    PlayFile(2, "Play file");

    companion object {
        fun fromText(text: String): InitMode? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): InitMode? {
            return entries.firstOrNull { it.value == value }
        }
    }
}