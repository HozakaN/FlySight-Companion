package fr.hozakan.flysightble.model.config

enum class SpeechUnit(
    val value: Int,
    val text: String
) {
    Metric(0, "km/h"),
    Imperial(1, "mph");

    companion object {
        fun fromText(text: String): SpeechUnit? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): SpeechUnit? {
            return entries.firstOrNull { it.value == value }
        }
    }
}