package fr.hozakan.flysightcompanion.model.ui

sealed class PlotLeftItem(val name: String) {
    data object Elevation : PlotLeftItem("Elevation")
    data object HorizontalSpeed : PlotLeftItem("HorizontalSpeed")
    data object VerticalSpeed : PlotLeftItem("VerticalSpeed")
    data object TotalSpeed : PlotLeftItem("TotalSpeed")
    data object Course : PlotLeftItem("Course")
    data object CourseRate : PlotLeftItem("CourseRate")
    data object CourseAccuracy : PlotLeftItem("CourseAccuracy")
    data object GlideRatio : PlotLeftItem("GlideRatio")
    data object DiveAngle : PlotLeftItem("DiveAngle")
    data object DiveRate : PlotLeftItem("DiveRate")
    data object HorizontalAccuracy : PlotLeftItem("HorizontalAccuracy")
    data object VerticalAccuracy : PlotLeftItem("VerticalAccuracy")
    data object SpeedAccuracy : PlotLeftItem("SpeedAccuracy")
    data object NumberOfSatellites : PlotLeftItem("NumberOfSatellites")
    data object Acceleration : PlotLeftItem("Acceleration")
    sealed class LocalAcceleration(name: String) : PlotLeftItem(name) {
        data object AccelerationForward : LocalAcceleration("AccelerationForward")
        data object AccelerationRight : LocalAcceleration("AccelerationRight")
        data object AccelerationDown : LocalAcceleration("AccelerationDown")
        data object AccelerationMagnitude : LocalAcceleration("AccelerationMagnitude")
    }
    data object TotalEnergy : PlotLeftItem("TotalEnergy")
    data object EnergyRate : PlotLeftItem("EnergyRate")
    data object LiftCoefficient : PlotLeftItem("LiftCoefficient")
    data object DragCoefficient : PlotLeftItem("DragCoefficient")
    sealed class Wingsuit(name: String) : PlotLeftItem(name) {
        data object SphericalErrorProbability : Wingsuit("SphericalErrorProbability")
    }
    sealed class Speed(name: String) : PlotLeftItem(name) {
        data object SpeedScoreAccuracy : Speed("SpeedScoreAccuracy")
    }

    companion object {
        fun fromName(name: String): PlotLeftItem? = when (name) {
            Elevation.name -> Elevation
            HorizontalSpeed.name -> HorizontalSpeed
            VerticalSpeed.name -> VerticalSpeed
            TotalSpeed.name -> TotalSpeed
            Course.name -> Course
            CourseRate.name -> CourseRate
            CourseAccuracy.name -> CourseAccuracy
            GlideRatio.name -> GlideRatio
            DiveAngle.name -> DiveAngle
            DiveRate.name -> DiveRate
            HorizontalAccuracy.name -> HorizontalAccuracy
            VerticalAccuracy.name -> VerticalAccuracy
            SpeedAccuracy.name -> SpeedAccuracy
            NumberOfSatellites.name -> NumberOfSatellites
            Acceleration.name -> Acceleration
            LocalAcceleration.AccelerationForward.name -> LocalAcceleration.AccelerationForward
            LocalAcceleration.AccelerationRight.name -> LocalAcceleration.AccelerationRight
            LocalAcceleration.AccelerationDown.name -> LocalAcceleration.AccelerationDown
            LocalAcceleration.AccelerationMagnitude.name -> LocalAcceleration.AccelerationMagnitude
            TotalEnergy.name -> TotalEnergy
            EnergyRate.name -> EnergyRate
            LiftCoefficient.name -> LiftCoefficient
            DragCoefficient.name -> DragCoefficient
            Wingsuit.SphericalErrorProbability.name -> Wingsuit.SphericalErrorProbability
            Speed.SpeedScoreAccuracy.name -> Speed.SpeedScoreAccuracy
            else -> null
        }
    }
}