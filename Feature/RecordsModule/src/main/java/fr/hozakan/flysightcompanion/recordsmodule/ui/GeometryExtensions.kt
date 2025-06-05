package fr.hozakan.flysightcompanion.recordsmodule.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

internal const val PI_F = PI.toFloat()
internal const val TO_DEGREES = 180f / PI_F
internal const val TO_RADIANS = PI_F / 180f
internal fun Float.toRadians() = this * TO_RADIANS
internal fun Float.toDegrees() = this * TO_DEGREES

// These exist for Size but not IntSize
internal val IntSize.center get() = Offset(width * 0.5f, height * 0.5f)
internal operator fun IntSize.times(scale: Float) =
    IntSize((width * scale).roundToInt(), (height * scale).roundToInt())

// New extensions
internal val IntSize.radius get() = min(width, height) * 0.5f
internal val Size.radius get() = min(width, height) * 0.5f

internal val Offset.minCoordinate get() = min(x, y) // exists for Size but not Offset
internal fun Offset.distanceTo(other: Offset) = sqrt(
    (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y),
)

internal fun Offset.midpoint(other: Offset) = (this + other) * 0.5f
internal fun Offset.length() = sqrt(x * x + y * y)
internal fun Offset.angle() = atan2(y, x)

internal fun Offset.roundToInt() = IntOffset(x.roundToInt(), y.roundToInt())

internal fun Offset.Companion.fromAngle(radians: Float, magnitude: Float) =
    Offset(cos(radians) * magnitude, sin(radians) * magnitude)

internal fun Offset.intersectCircle(center: Offset, radius: Float): List<Offset> {
    val x1 = this.x
    val y1 = this.y
    val x2 = center.x
    val y2 = center.y
    val circleX = center.x
    val circleY = center.y
    val dx = x2 - x1
    val dy = y2 - y1

    // Paramètres pour l'équation du cercle
    val a = dx * dx + dy * dy
    val b = 2 * ((x1 - circleX) * dx + (y1 - circleY) * dy)
    val c = (x1 - circleX).pow(2) + (y1 - circleY).pow(2) - radius.pow(2)

    // Calcul du discriminant
    val discriminant = b * b - 4 * a * c

    // Liste des points d'intersection
    val intersections = mutableListOf<Offset>()

    if (discriminant < 0) {
        // Pas d'intersection
        return intersections
    }

    // Résolution des paramètres t
    val sqrtDiscriminant = sqrt(discriminant)
    val t1 = (-b + sqrtDiscriminant) / (2 * a)
    val t2 = (-b - sqrtDiscriminant) / (2 * a)

    val p1x = x1 + t1 * dx
    val p1y = y1 + t1 * dy
    intersections.add(Offset(p1x, p1y))
    val p2x = x1 + t2 * dx
    val p2y = y1 + t2 * dy
    intersections.add(Offset(p2x, p2y))

    return intersections
}
