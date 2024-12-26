package fr.hozakan.flysightcompanion.model.config

enum class RateMode(
    val value: Int
) {
    HorizontalSpeed(0),
    VerticalSpeed(1),
    GlideRatio(2),
    InverseGlideRatio(3),
    TotalSpeed(4),
    MagnitudeOf1(8),
    ChangeInValue1(9),
    DiveAngle(11);

    companion object {
        fun fromValue(value: Int): RateMode? {
            return entries.firstOrNull { it.value == value }
        }
    }

}