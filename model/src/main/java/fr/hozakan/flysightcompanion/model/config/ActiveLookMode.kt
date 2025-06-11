package fr.hozakan.flysightcompanion.model.config

enum class ActiveLookMode(
    val value: Int
) {
    NotActive(0),
    DefaultMode(1);

    companion object {
        fun fromValue(value: Int): ActiveLookMode? {
            return entries.firstOrNull { it.value == value }
        }
    }
}