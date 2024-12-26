package fr.hozakan.flysightcompanion.model.config

enum class ToneMode(
    val value: Int
) {
    HorizontalSpeed(0),
    VerticalSpeed(1),
    GlideRatio(2),
    InverseGlideRatio(3),
    TotalSpeed(4),
    DiveAngle(11);

    companion object {
        fun fromValue(value: Int): ToneMode? {
            return entries.firstOrNull { it.value == value }
        }
    }

}