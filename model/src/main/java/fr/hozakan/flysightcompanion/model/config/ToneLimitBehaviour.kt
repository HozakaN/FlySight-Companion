package fr.hozakan.flysightcompanion.model.config

enum class ToneLimitBehaviour(
    val value: Int,
    val text: String
) {
    NoTone(0, "No tone"),
    MinMaxTone(1, "Min/max tone"),
    ChirpUpDown(2, "Chirp up/down"),
    ChirpDownUp(2, "Chirp down/up");

    companion object {
        fun fromText(text: String): ToneLimitBehaviour? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): ToneLimitBehaviour? {
            return entries.firstOrNull { it.value == value }
        }
    }

}