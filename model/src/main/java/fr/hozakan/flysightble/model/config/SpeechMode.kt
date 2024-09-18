package fr.hozakan.flysightble.model.config

enum class SpeechMode(
    val value: Int,
    val text: String
) {
    HorizontalSpeed(0, "Horizontal speed"),
    VerticalSpeed(1, "Vertical speed"),
    GlideRatio(2, "Glide ratio"),
    InverseGlideRatio(3, "Inverse glide ratio"),
    TotalSpeed(4, "Total speed"),
    AltitudeAboveDropzone(5, "Altitude above dropzone"),
    DiveAngle(11, "Dive angle");

    companion object {
        fun fromText(text: String): SpeechMode? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): SpeechMode? {
            return entries.firstOrNull { it.value == value }
        }
    }
}