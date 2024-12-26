package fr.hozakan.flysightcompanion.model.config

enum class ToneLimitBehaviour(
    val value: Int
) {
    NoTone(0),
    MinMaxTone(1),
    ChirpUpDown(2),
    ChirpDownUp(2);

    companion object {
        fun fromValue(value: Int): ToneLimitBehaviour? {
            return entries.firstOrNull { it.value == value }
        }
    }

}