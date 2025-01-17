package fr.hozakan.flysightcompanion.model.records

import java.time.LocalDateTime

data class AnalyzeOptions(
    val mass: Double = 70.0,
    val planformArea: Double = 2.0,
    val minDrag: Double = 0.05,
    val minLift: Double = 0.0,
    val maxLift: Double = 0.5,
    val maxLD: Double = 3.0,
    val simulationTime: Int = 120,
    val windE: Double = 0.0,
    val windN: Double = 0.0,
    val windAdjustment: Boolean = false,
    val groundReference: GroundReference = GroundReference.Automatic,
    val courseReference: CourseReference = CourseReference.Automatic,
    val exitReference: ExitReference = ExitReference.Automatic
)

sealed interface GroundReference {
    data object Automatic : GroundReference
    data class Fixed(val value: Double) : GroundReference
}

sealed interface CourseReference {
    data object Automatic : CourseReference
    data class Fixed(val value: Double) : CourseReference

    val content: Double?
        get() = when (this) {
            is Automatic -> null
            is Fixed -> value
        }
}

sealed class ExitReference {
    data object Automatic : ExitReference()
    data class Fixed(val dateTime: LocalDateTime) : ExitReference()
}