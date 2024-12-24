package fr.hozakan.flysightcompanion.model.config

enum class ToneMode(
    val value: Int,
    val text: String
) {
    HorizontalSpeed(0, "Horizontal speed"),
    VerticalSpeed(1, "Vertical speed"),
    GlideRatio(2, "Glide ratio"),
    InverseGlideRatio(3, "Inverse glide ratio"),
    TotalSpeed(4, "Total speed"),
    DiveAngle(11, "Dive angle");

    companion object {
        fun fromText(text: String): ToneMode? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): ToneMode? {
            return entries.firstOrNull { it.value == value }
        }
    }

}