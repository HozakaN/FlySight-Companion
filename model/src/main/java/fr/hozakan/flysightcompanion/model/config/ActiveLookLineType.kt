package fr.hozakan.flysightcompanion.model.config

enum class ActiveLookLineType(
    val value: Int
) {
    HorizontalSpeed(0),
    VerticalSpeed(1),
    GlideRatio(2),
    InverseGlideRatio(3),
    TotalSpeed(4),
    DirectionToDestination(5),
    DistanceToDestination(6),
    DirectionToBearing(7),
    DiveAngle(11),
    AltitudeAboveDzElev(12),
    Course(13);

    companion object Companion {
        fun fromValue(value: Int): ActiveLookLineType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}