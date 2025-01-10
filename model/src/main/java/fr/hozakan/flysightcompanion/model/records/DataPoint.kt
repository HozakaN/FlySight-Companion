package fr.hozakan.flysightcompanion.model.records

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import kotlin.math.atan2
import kotlin.math.sqrt

typealias DataPoints = List<DataPoint>

interface DataPoint {
    val dateTime: LocalDateTime
    val hasGeodetic: Boolean
    val latitude: Double
    val longitude: Double
    val hMSL: Double
    val velN: Double
    val velE: Double
    val velD: Double
    val hAcc: Double
    val vAcc: Double
    val sAcc: Double
    val numSV: Int
}

fun dataPoint(
    dateTime: LocalDateTime,
    hasGeodetic: Boolean,
    latitude: Double,
    longitude: Double,
    hMSL: Double,
    velN: Double,
    velE: Double,
    velD: Double,
    hAcc: Double,
    vAcc: Double,
    sAcc: Double,
    numSV: Int
): DataPoint = DataPointImpl(
    dateTime = dateTime,
    hasGeodetic = hasGeodetic,
    latitude = latitude,
    longitude = longitude,
    hMSL = hMSL,
    velN = velN,
    velE = velE,
    velD = velD,
    hAcc = hAcc,
    vAcc = vAcc,
    sAcc = sAcc,
    numSV = numSV
)

data class DataPointImpl(
    override val dateTime: LocalDateTime,
    override val hasGeodetic: Boolean,
    override val latitude: Double,
    override val longitude: Double,
    override val hMSL: Double,
    override val velN: Double,
    override val velE: Double,
    override val velD: Double,
    override val hAcc: Double,
    override val vAcc: Double,
    override val sAcc: Double,
    override val numSV: Int
) : DataPoint

fun DataPoint.toComputableDataPoint(): ComputableDataPoint {
    return ComputableDataPoint(
        dataPoint = this,
        heading = 0.0,
        cAcc = 0.0,
        t = 0.0,
        x = 0.0,
        y = 0.0,
        z = 0.0,
        dist2D = 0.0,
        dist3D = 0.0,
        curv = 0.0,
        accel = 0.0,
        ax = 0.0,
        ay = 0.0,
        az = 0.0,
        amag = 0.0,
        lift = 0.0,
        drag = 0.0,
        vx = 0.0,
        vy = 0.0,
        theta = 0.0,
        omega = 0.0
    )
}

typealias ComputableDataPoints = List<ComputableDataPoint>

data class ComputableDataPoint(
    val dataPoint: DataPoint,
    val heading: Double,
    val cAcc: Double,
    val t: Double,
    val x: Double,
    val y: Double,
    val z: Double,
    val dist2D: Double,
    val dist3D: Double,
    val curv: Double,
    val accel: Double,
    val ax: Double,
    val ay: Double,
    val az: Double,
    val amag: Double,
    val lift: Double,
    val drag: Double,
    val vx: Double,
    val vy: Double,
    val theta: Double,
    val omega: Double
) : DataPoint by dataPoint

infix fun ComputableDataPoint.interpolateWith(other: ComputableDataPoint): Pair<ComputableDataPoint, ComputableDataPoint> = this to other

infix fun Pair<ComputableDataPoint, ComputableDataPoint>.using(a: Double): ComputableDataPoint {
    val dateTime1 = first.dateTime.toEpochSecond(ZoneOffset.UTC)
    val dateTime2 = second.dateTime.toEpochSecond(ZoneOffset.UTC)
    val computedDateTime = Date((dateTime1 + a * (dateTime2 - dateTime1)).toLong())
    val hasGeodetic = first.hasGeodetic && second.hasGeodetic

    val lat = first.latitude + a * (second.latitude - first.latitude)
    val lon = first.longitude + a * (second.longitude - first.longitude)
    val hMSL = first.hMSL + a * (second.hMSL - first.hMSL)

    val velN = first.velN + a * (second.velN - first.velN)
    val velE = first.velE + a * (second.velE - first.velE)
    val velD = first.velD + a * (second.velD - first.velD)

    val hAcc = first.hAcc + a * (second.hAcc - first.hAcc)
    val vAcc = first.vAcc + a * (second.vAcc - first.vAcc)
    val sAcc = first.sAcc + a * (second.sAcc - first.sAcc)

    val numSV = if (a < 0.5) first.numSV else second.numSV

    val t = first.t + a * (second.t - first.t)
    val x = first.x + a * (second.x - first.x)
    val y = first.y + a * (second.y - first.y)
    val z = first.z + a * (second.z - first.z)

    val dist2D = first.dist2D + a * (second.dist2D - first.dist2D)
    val dist3D = first.dist3D + a * (second.dist3D - first.dist3D)

    val curv = first.curv + a * (second.curv - first.curv)
    val accel = first.accel + a * (second.accel - first.accel)

    val ax = first.ax + a * (second.ax - first.ax)
    val ay = first.ay + a * (second.ay - first.ay)
    val az = first.az + a * (second.az - first.az)
    val amag = first.amag + a * (second.amag - first.amag)

    val lift = first.lift + a * (second.lift - first.lift)
    val drag = first.drag + a * (second.drag - first.drag)

    val heading = first.heading + a * (second.heading - first.heading)
    val cAcc = first.cAcc + a * (second.cAcc - first.cAcc)

    val vx = first.vx + a * (second.vx - first.vx)
    val vy = first.vy + a * (second.vy - first.vy)

    val theta = first.theta + a * (second.theta - first.theta)
    val omega = first.omega + a * (second.omega - first.omega)

    return ComputableDataPoint(
        dataPoint = DataPointImpl(
            dateTime = computedDateTime.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime(),
            hasGeodetic = hasGeodetic,
            latitude = lat,
            longitude = lon,
            hMSL = hMSL,
            velN = velN,
            velE = velE,
            velD = velD,
            hAcc = hAcc,
            vAcc = vAcc,
            sAcc = sAcc,
            numSV = numSV
        ),
        t = t,
        x = x,
        y = y,
        z = z,
        dist2D = dist2D,
        dist3D = dist3D,
        curv = curv,
        accel = accel,
        ax = ax,
        ay = ay,
        az = az,
        amag = amag,
        lift = lift,
        drag = drag,
        heading = heading,
        cAcc = cAcc,
        vx = vx,
        vy = vy,
        theta = theta,
        omega = omega
    )
}

val ComputableDataPoint.totalSpeed: Double
    get() = sqrt(vx * vx + vy * vy + velD * velD)

val ComputableDataPoint.diveAngle: Double
    get() = atan2(velD, sqrt(vx * vx + vy * vy)) / Math.PI * 180