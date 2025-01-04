package fr.hozakan.flysightcompanion.model.config

enum class SpeechMode(
    val value: Int
) {
    HorizontalSpeed(0),
    VerticalSpeed(1),
    GlideRatio(2),
    InverseGlideRatio(3),
    TotalSpeed(4),
    AltitudeAboveDropzone(5),
    DiveAngle(11);

    companion object {
        fun fromValue(value: Int): SpeechMode? {
            return entries.firstOrNull { it.value == value }
        }
    }
}