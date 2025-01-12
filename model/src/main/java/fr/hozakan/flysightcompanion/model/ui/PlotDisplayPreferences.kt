package fr.hozakan.flysightcompanion.model.ui

typealias ColorHex = String

sealed class PlotDisplayPreference(
    val name: String,
    val colorHex: ColorHex
) {
    class Elevation(colorHex: ColorHex) : PlotDisplayPreference("Elevation", colorHex)
    class HorizontalSpeed(colorHex: ColorHex) : PlotDisplayPreference("HorizontalSpeed", colorHex)
    class VerticalSpeed(colorHex: ColorHex) : PlotDisplayPreference("VerticalSpeed", colorHex)
    class TotalSpeed(colorHex: ColorHex) : PlotDisplayPreference("TotalSpeed", colorHex)
    class Course(colorHex: ColorHex) : PlotDisplayPreference("Course", colorHex)
    class CourseRate(colorHex: ColorHex) : PlotDisplayPreference("CourseRate", colorHex)
    class CourseAccuracy(colorHex: ColorHex) : PlotDisplayPreference("CourseAccuracy", colorHex)
    class GlideRatio(colorHex: ColorHex) : PlotDisplayPreference("GlideRatio", colorHex)
    class DiveAngle(colorHex: ColorHex) : PlotDisplayPreference("DiveAngle", colorHex)
    class DiveRate(colorHex: ColorHex) : PlotDisplayPreference("DiveRate", colorHex)
    class HorizontalAccuracy(colorHex: ColorHex) :
        PlotDisplayPreference("HorizontalAccuracy", colorHex)

    class VerticalAccuracy(colorHex: ColorHex) : PlotDisplayPreference("VerticalAccuracy", colorHex)
    class SpeedAccuracy(colorHex: ColorHex) : PlotDisplayPreference("SpeedAccuracy", colorHex)
    class NumberOfSatellites(colorHex: ColorHex) :
        PlotDisplayPreference("NumberOfSatellites", colorHex)

    class Acceleration(colorHex: ColorHex) : PlotDisplayPreference("Acceleration", colorHex)
    class AccelerationForward(colorHex: ColorHex) :
        PlotDisplayPreference("AccelerationForward", colorHex)

    class AccelerationRight(colorHex: ColorHex) :
        PlotDisplayPreference("AccelerationRight", colorHex)

    class AccelerationDown(colorHex: ColorHex) : PlotDisplayPreference("AccelerationDown", colorHex)
    class AccelerationMagnitude(colorHex: ColorHex) :
        PlotDisplayPreference("AccelerationMagnitude", colorHex)

    class TotalEnergy(colorHex: ColorHex) : PlotDisplayPreference("TotalEnergy", colorHex)
    class EnergyRate(colorHex: ColorHex) : PlotDisplayPreference("EnergyRate", colorHex)
    class LiftCoefficient(colorHex: ColorHex) : PlotDisplayPreference("LiftCoefficient", colorHex)
    class DragCoefficient(colorHex: ColorHex) : PlotDisplayPreference("DragCoefficient", colorHex)
    class SphericalErrorProbability(colorHex: ColorHex) :
        PlotDisplayPreference("SphericalErrorProbability", colorHex)

    class SpeedScoreAccuracy(colorHex: ColorHex) :
        PlotDisplayPreference("SpeedScoreAccuracy", colorHex)

    companion object {
        fun defaultValues(): List<PlotDisplayPreference> = listOf(
            Elevation("#FF0000"),
            HorizontalSpeed("#00FF00"),
            VerticalSpeed("#0000FF"),
            TotalSpeed("#FF00FF"),
            Course("#FFFF00"),
            CourseRate("#00FFFF"),
            CourseAccuracy("#FF8000"),
            GlideRatio("#FF0080"),
            DiveAngle("#00FF80"),
            DiveRate("#8000FF"),
            HorizontalAccuracy("#FF8080"),
            VerticalAccuracy("#80FF80"), //here done
            SpeedAccuracy("#8080FF"),
            NumberOfSatellites("#FF80FF"),
            Acceleration("#80FF00"),
            AccelerationForward("#FF80FF"),
            AccelerationRight("#80FFFF"),
            AccelerationDown("#FF0080"),
            AccelerationMagnitude("#FF8000"),
            TotalEnergy("#00FF80"),
            EnergyRate("#8000FF"),
            LiftCoefficient("#FF8080"),
            DragCoefficient("#80FF80"),
            SphericalErrorProbability("#8080FF"),
            SpeedScoreAccuracy("#FF80FF")
        )

        fun toStringPreference(): String {
            return defaultValues().joinToString(";") { "${it.name}:${it.colorHex}" }
        }

        fun fromStringPreference(stringPref: String): List<PlotDisplayPreference> {
            return stringPref.split(";").mapNotNull { pref ->
                val (name, colorHex) = pref.split(":")
                defaultValues().firstOrNull { it.name == name }?.let {
                    it::class.java.constructors.first()
                        .newInstance(colorHex) as PlotDisplayPreference
                }
            }
        }
    }
}

val List<PlotDisplayPreference>.accelerationColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.Acceleration }.colorHex

val List<PlotDisplayPreference>.courseColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.Course }.colorHex

val List<PlotDisplayPreference>.courseAccuracyColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.CourseAccuracy }.colorHex

val List<PlotDisplayPreference>.courseRateColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.CourseRate }.colorHex

val List<PlotDisplayPreference>.diveAngleColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.DiveAngle }.colorHex

val List<PlotDisplayPreference>.diveRateColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.DiveRate }.colorHex

val List<PlotDisplayPreference>.dragCoefficientColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.DragCoefficient }.colorHex

val List<PlotDisplayPreference>.elevationColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.Elevation }.colorHex

val List<PlotDisplayPreference>.energyRateColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.EnergyRate }.colorHex

val List<PlotDisplayPreference>.glideRatioColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.GlideRatio }.colorHex

val List<PlotDisplayPreference>.horizontalAccuracyColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.HorizontalAccuracy }.colorHex

val List<PlotDisplayPreference>.horizontalSpeedColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.HorizontalSpeed }.colorHex

val List<PlotDisplayPreference>.liftCoefficientColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.LiftCoefficient }.colorHex

val List<PlotDisplayPreference>.numberOfSatellitesColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.NumberOfSatellites }.colorHex

val List<PlotDisplayPreference>.speedAccuracyColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.SpeedAccuracy }.colorHex

val List<PlotDisplayPreference>.speedScoreAccuracyColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.SpeedScoreAccuracy }.colorHex

val List<PlotDisplayPreference>.sphericalErrorProbabilityColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.SphericalErrorProbability }.colorHex

val List<PlotDisplayPreference>.totalEnergyColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.TotalEnergy }.colorHex

val List<PlotDisplayPreference>.totalSpeedColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.TotalSpeed }.colorHex

val List<PlotDisplayPreference>.verticalAccuracyColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.VerticalAccuracy }.colorHex

val List<PlotDisplayPreference>.verticalSpeedColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.VerticalSpeed }.colorHex

val List<PlotDisplayPreference>.accelerationForwardColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.AccelerationForward }.colorHex

val List<PlotDisplayPreference>.accelerationRightColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.AccelerationRight }.colorHex

val List<PlotDisplayPreference>.accelerationDownColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.AccelerationDown }.colorHex

val List<PlotDisplayPreference>.accelerationMagnitudeColor: ColorHex
    get() = this.first { it is PlotDisplayPreference.AccelerationMagnitude }.colorHex