package fr.hozakan.flysightble.model.config

enum class AltitudeUnit(
    val value: Int,
    val text: String
) {
    Metric(0, "m"),
    Imperial(1, "ft");

    companion object {
        fun fromText(text: String): AltitudeUnit? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): AltitudeUnit? {
            return entries.firstOrNull { it.value == value }
        }
    }
}