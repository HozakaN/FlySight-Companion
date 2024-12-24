package fr.hozakan.flysightcompanion.model.config

enum class RateMode(
    val value: Int,
    val text: String
) {
    HorizontalSpeed(0, "Horizontal speed"),
    VerticalSpeed(1, "Vertical speed"),
    GlideRatio(2, "Glide ratio"),
    InverseGlideRatio(3, "Inverse glide ratio"),
    TotalSpeed(4, "Total speed"),
    MagnitudeOf1(8, "Magnitude of tone value"),
    ChangeInValue1(9, "Change in tone value"),
    DiveAngle(11, "Dive angle");

    companion object {
        fun fromText(text: String): RateMode? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): RateMode? {
            return entries.firstOrNull { it.value == value }
        }
    }

}