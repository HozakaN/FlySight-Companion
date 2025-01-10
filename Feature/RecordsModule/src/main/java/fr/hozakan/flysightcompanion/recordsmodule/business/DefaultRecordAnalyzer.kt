package fr.hozakan.flysightcompanion.recordsmodule.business

import fr.hozakan.flysightcompanion.framework.extension.firstNotNullConsecutive
import fr.hozakan.flysightcompanion.model.records.AnalyzeOptions
import fr.hozakan.flysightcompanion.model.records.ComputableDataPoint
import fr.hozakan.flysightcompanion.model.records.DataPoints
import fr.hozakan.flysightcompanion.model.records.GroundReference
import fr.hozakan.flysightcompanion.model.records.RecordAnalyze
import fr.hozakan.flysightcompanion.model.records.diveAngle
import fr.hozakan.flysightcompanion.model.records.interpolateWith
import fr.hozakan.flysightcompanion.model.records.toComputableDataPoint
import fr.hozakan.flysightcompanion.model.records.totalSpeed
import fr.hozakan.flysightcompanion.model.records.using
import fr.hozakan.flysightcompanion.recordsmodule.A_GRAVITY
import fr.hozakan.flysightcompanion.recordsmodule.GAS_CONST
import fr.hozakan.flysightcompanion.recordsmodule.LAPSE_RATE
import fr.hozakan.flysightcompanion.recordsmodule.MM_AIR
import fr.hozakan.flysightcompanion.recordsmodule.SL_PRESSURE
import fr.hozakan.flysightcompanion.recordsmodule.SL_TEMP
import net.sf.geographiclib.Geodesic
import java.time.ZoneOffset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class DefaultRecordAnalyzer : RecordAnalyzer {

    private lateinit var options: AnalyzeOptions

    override suspend fun analyze(
        dataPoints: DataPoints,
        options: AnalyzeOptions
    ): RecordAnalyze {
        this.options = options
        val ground = when (val groundReference = options.groundReference) {
            GroundReference.Automatic -> dataPoints.last().hMSL
            is GroundReference.Fixed -> groundReference.value
        }
        val exitTime: Double
        val computedDataPoints = dataPoints.map { it.toComputableDataPoint() }
            .computeTime()
            .computeAltitude(ground = ground)
            .computeAcceleration()
            .pairWithExitTime()
            .also {
                exitTime = it.second
            }
            .updateTimeWithExitValue()
            .computeVelocity()
            .computeDistances()
            .computeCumulativeHeading(theta0 = options.courseReference.content ?: 0.0)
            .computeVelocityDependantParameters()
            .computeAerodynamics()
        return RecordAnalyze(
            options = options,
            exitTime = exitTime,
            dataPoints = computedDataPoints
        )
    }

    private fun List<ComputableDataPoint>.computeAerodynamics(): List<ComputableDataPoint> {
        return mapIndexed { index, dp ->

            // Acceleration
            val accelN = getSlope(index) { it.vy }
            val accelE = getSlope(index) { it.vx }
            var accelD = getSlope(index) { it.velD }

            // Substract acceleration due to gravity
            accelD -= A_GRAVITY

            // Calculate acceleration due to drag
            val vel = dp.totalSpeed
            val proj = (accelN * dp.vy + accelE * dp.vx + accelD * dp.velD) / vel

            val dragN = proj * dp.vy / vel
            val dragE = proj * dp.vx / vel
            val dragD = proj * dp.velD / vel

            val accelDrag = sqrt(dragN * dragN + dragE * dragE + dragD * dragD)

            // Calculate acceleration due to lift
            val liftN = accelN - dragN
            val liftE = accelE - dragE
            val liftD = accelD - dragD

            val accelLift = sqrt(liftN * liftN + liftE * liftE + liftD * liftD)

            // From https://en.wikipedia.org/wiki/Atmospheric_pressure#Altitude_variation
            val airPressure =
                SL_PRESSURE * (1 - LAPSE_RATE * dp.hMSL / SL_TEMP).pow(A_GRAVITY * MM_AIR / GAS_CONST / LAPSE_RATE)

            // From https://en.wikipedia.org/wiki/Lapse_rate
            val temperature = SL_TEMP - LAPSE_RATE * dp.hMSL

            // From https://en.wikipedia.org/wiki/Density_of_air
            val airDensity = airPressure / (GAS_CONST / MM_AIR) / temperature

            // From https://en.wikipedia.org/wiki/Dynamic_pressure
            val dynamicPressure = airDensity * vel * vel / 2

            dp.copy(
                lift = options.mass * accelLift / dynamicPressure / options.planformArea,
                drag = options.mass * accelDrag / dynamicPressure / options.planformArea
            )
        }
    }

    private fun Pair<List<ComputableDataPoint>, Double>.updateTimeWithExitValue(): List<ComputableDataPoint> {
        return first.map { dp ->
            val end = dp.dataPoint.dateTime.toEpochSecond(ZoneOffset.UTC)
            dp.copy(
                t = (end - second) / 1_000
            )
        }
    }

    private fun List<ComputableDataPoint>.computeAcceleration(): List<ComputableDataPoint> {
        return mapIndexed { index, dp ->
            val accelN = getSlope(index) { it.velN }
            val accelE = getSlope(index) { it.velE }
            val accelD = getSlope(index) { it.velD }

            // Calculate acceleration in direction of flight
            val vh = sqrt(dp.velN * dp.velN + dp.velE * dp.velE)
            val ax = (accelN * dp.velN + accelE * dp.velE) / vh

            // Calculate acceleration perpendicular to flight
            val ay = (accelE * dp.velN - accelN * dp.velE) / vh

            // Calculate vertical acceleration
            val az = accelD

            // Calculate total acceleration
            val amag = sqrt(accelN * accelN + accelE * accelE + accelD * accelD)

            dp.copy(
                ax = ax,
                ay = ay,
                az = az,
                amag = amag
            )
        }
    }

    private fun List<ComputableDataPoint>.getSlope(
        center: Int,
        valueProvider: (ComputableDataPoint) -> Double
    ): Double {
        val iMin = max(0, center - 4)
        val iMax = min(size - 1, center + 4)

        var sumX = 0.0
        var sumY = 0.0
        var sumXX = 0.0
        var sumXY = 0.0
        this.subList(fromIndex = iMin, toIndex = iMax + 1).forEach { dp ->
            val x = dp.t
            val y = valueProvider(dp)
            sumX += x
            sumY += y
            sumXX += x * x
            sumXY += x * y
        }
        val n = iMax - iMin + 1
        return (sumXY - sumX * sumY / n) / (sumXX - sumX * sumX / n)
    }

    private fun List<ComputableDataPoint>.computeAltitude(
        ground: Double
    ): List<ComputableDataPoint> = map { dataPoint ->
        dataPoint.copy(z = dataPoint.hMSL - ground)
    }

    private fun List<ComputableDataPoint>.computeTime(): List<ComputableDataPoint> {
        val dp0 = first()
        val start = dp0.dateTime.toEpochSecond(ZoneOffset.UTC)
        return map { dataPoint ->
            val t = dataPoint.dateTime.toEpochSecond(ZoneOffset.UTC) - start
            dataPoint.copy(t = t.toDouble())
        }
    }

    private fun List<ComputableDataPoint>.computeVelocity(): List<ComputableDataPoint> {
        if (isEmpty()) return this

        val dp0 = interpolateDataT(0)
        return if (options.windAdjustment) {
            map { dp ->
                val distance = dp0.distanceTo(dp)
                val bearing = dp0.bearingWith(dp)

                val x = distance * sin(bearing) - options.windE * dp.t
                val y = distance * cos(bearing) - options.windN * dp.t
                val vx = dp.vx - options.windE
                val vy = dp.vy - options.windN
                dp.copy(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy
                )
            }
        } else {
            map { dp ->
                val distance = dp0.distanceTo(dp)
                val bearing = dp0.bearingWith(dp)

                dp.copy(
                    x = distance * sin(bearing),
                    y = distance * cos(bearing),
                    vx = dp.velE,
                    vy = dp.velN
                )
            }
        }
    }

    private fun List<ComputableDataPoint>.computeDistances(): List<ComputableDataPoint> {
        var dist2D = 0.0
        var dist3D = 0.0
        val intermediate = mapIndexed { index, dp ->
            if (index == 0) {
                dp
            } else {
                val prev = get(index - 1)
                val dx = dp.x - prev.x
                val dy = dp.y - prev.y
                val dh = sqrt(dx * dx + dy * dy)
                val dz = dp.hMSL - prev.hMSL
                dist2D += dh
                dist3D += sqrt(dh * dh + dz * dz)
                dp.copy(
                    dist2D = dist2D,
                    dist3D = dist3D
                )
            }
        }

        // Adjust for exit
        val dp0 = interpolateDataT(0)
        return intermediate.map { dp ->
            dp.copy(
                x = dp.x - dp0.x,
                y = dp.y - dp0.y,
                dist2D = dp.dist2D - dp0.dist2D,
                dist3D = dp.dist3D - dp0.dist3D
            )
        }
    }

    private fun List<ComputableDataPoint>.computeCumulativeHeading(theta0: Double): List<ComputableDataPoint> {
        var previousHeading = 0.0
        return mapIndexed { index, dp ->
            var heading = atan2(dp.vx, dp.vy) / Math.PI * 100
            val totalSpeed = dp.totalSpeed
            val cAcc = if (totalSpeed != 0.0) dp.sAcc / size else 0.0

            //Adjust heading
            if (index > 0) {
                while (heading < previousHeading - 180) heading + -360
                while (heading > previousHeading + 180) heading -= 360
            }

            // Relative heading
            val theta = heading - theta0

            previousHeading = heading
            dp.copy(
                heading = heading,
                cAcc = cAcc,
                theta = theta
            )

        }
    }

    private fun List<ComputableDataPoint>.pairWithExitTime(): Pair<List<ComputableDataPoint>, Double> {
        return this to (firstNotNullConsecutive { dp1, dp2 ->
            // Get interpolation coefficient
            val velD = A_GRAVITY
            val a = (velD - dp1.velD) / (dp2.velD - dp1.velD)

            // Check vertical speed
            if (a < 0 || 1 < a) return@firstNotNullConsecutive null

            // Check accuracy
            val vAcc = dp1.vAcc + a * (dp2.vAcc - dp1.vAcc)
            if (vAcc > 10) return@firstNotNullConsecutive null

            // Check acceleration
            val az1 = dp1.z
            val az2 = dp2.z
            val az = az1 + a * (az2 - az1)
            if (az < A_GRAVITY / 5f) return@firstNotNullConsecutive null

            // Determine exit
            val t1 = dp1.dateTime.toEpochSecond(ZoneOffset.UTC)
            val t2 = dp2.dateTime.toEpochSecond(ZoneOffset.UTC)
            val start = t1 + a * (t2 - t1) - velD / az * 1000f
            start
        } ?: this.first().dateTime.toEpochSecond(ZoneOffset.UTC).toDouble())
    }

    private fun List<ComputableDataPoint>.computeVelocityDependantParameters(): List<ComputableDataPoint> {
        return mapIndexed { index, dp ->
            dp.copy(
                curv = getSlope(index) { it.diveAngle },
                accel = getSlope(index) { it.totalSpeed },
                omega = getSlope(index) { it.theta }
            )
        }
    }

    private fun List<ComputableDataPoint>.interpolateDataT(timing: Int): ComputableDataPoint {
        val i1 = this.indexOfLast { it.t < timing }
        val i2 = this.indexOfFirst { it.t > timing }

        if (i1 < 0) return this.first()
        if (i2 >= size) return this.last()

        val dp1 = get(i1)
        val dp2 = get(i2)

        return dp1 interpolateWith dp2 using ((timing - dp1.t) / (dp2.t - dp1.t))
    }

    private fun ComputableDataPoint.distanceTo(
        other: ComputableDataPoint
    ): Double =
        if (!options.windAdjustment && hasGeodetic && other.hasGeodetic) {
            val geodesic = Geodesic.WGS84
            geodesic.Inverse(latitude, longitude, other.latitude, other.longitude).s12
        } else {
            sqrt((other.x - x).pow(2) + (other.y - y).pow(2))
        }

    private fun ComputableDataPoint.bearingWith(other: ComputableDataPoint): Double =
        if (!options.windAdjustment && hasGeodetic && other.hasGeodetic) {
            val geodesic = Geodesic.WGS84
            geodesic.Inverse(
                latitude,
                longitude,
                other.latitude,
                other.longitude
            ).azi1 / 180 * Math.PI
        } else {
            atan2(other.x - x, other.y - y) / Math.PI * 100
        }
}


