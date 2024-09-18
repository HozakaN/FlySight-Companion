package fr.hozakan.flysightble.model.config

enum class UnitSystem(
    val value: Int,
    val text: String
) {
    Metric(0, "Metric"),
    Imperial(1, "Imperial");

    companion object {
        fun fromText(text: String): UnitSystem? {
            return entries.firstOrNull { it.text == text }
        }
    }
}