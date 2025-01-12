package fr.hozakan.flysightcompanion.recordsmodule.ui.plot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradient
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.window.core.layout.WindowWidthSizeClass
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.recordsmodule.ui.angle
import fr.hozakan.flysightcompanion.recordsmodule.ui.angleToHue
import fr.hozakan.flysightcompanion.recordsmodule.ui.composeColor
import fr.hozakan.flysightcompanion.recordsmodule.ui.distanceTo
import fr.hozakan.flysightcompanion.recordsmodule.ui.fromAngle
import fr.hozakan.flysightcompanion.recordsmodule.ui.hexCode
import fr.hozakan.flysightcompanion.recordsmodule.ui.hsvToCoord
import fr.hozakan.flysightcompanion.recordsmodule.ui.intersectCircle
import fr.hozakan.flysightcompanion.recordsmodule.ui.length
import fr.hozakan.flysightcompanion.recordsmodule.ui.radius
import fr.hozakan.flysightcompanion.recordsmodule.ui.toHSV
import timber.log.Timber
import kotlin.math.min

@Composable
fun CustomColorPickerDialog(
    initialColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        var currentColorText by remember(initialColor) { mutableStateOf(initialColor.hexCode) }
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val displaySideBySide =
            windowSizeClass.windowWidthSizeClass.hashCode() >= WindowWidthSizeClass.MEDIUM.hashCode()
        Card {
            Column(
                modifier = if (displaySideBySide) {
                    Modifier.requiredWidth(300.dp)
                } else {
                    Modifier.fillMaxWidth()
                }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val selectedComposeColor = remember(currentColorText) { "#$currentColorText".composeColor }
                if (displaySideBySide) {

                } else {
                    CustomColorPicker(
                        initialColor = selectedComposeColor ?: Color.Black,
                        onCurrentColorChanged = { color ->
                            currentColorText = color.hexCode
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
//                    FText(
//                        text = currentColor.hexCode.uppercase(),
//                        configuration = FlySightTheme.typography.cardTitle,
//                        color = currentColor
//                    )
                    TextField(
                        prefix = {
                            Text(text = "#")
                        },
                        value = currentColorText.uppercase(),
                        onValueChange = {
                            currentColorText = it
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(64.dp)
                            .background(color = Color.Red)
                    )
                }
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                SimpleDialogActionBar(
                    onCancel = onDismissRequest,
                    validateEnabled = selectedComposeColor != null,
                    onValidate = {
                        selectedComposeColor?.let { newColor ->
                            onColorSelected(newColor)
                            onDismissRequest()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CustomColorPicker(
    initialColor: Color,
    onCurrentColorChanged: (Color) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.requiredSize(360.dp)
    ) {
        val sideSize = with(LocalDensity.current) {
            min(maxWidth.toPx(), maxHeight.toPx())
        }
        val size = Size(sideSize, sideSize)
        var selectedPoint by remember(initialColor) {
            val (h, s, _) = initialColor.toHSV()
            mutableStateOf(hsvToCoord(h, s, size.center))
        }
        var selectedColor by remember(initialColor) { mutableStateOf(initialColor) }
        Box {
            Canvas(
                modifier = Modifier
                    .requiredSize(360.dp)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val (color, _) = coordToColor(
                                offset,
                                size.center,
                                size.radius
                            )
                            selectedPoint = offset
                            selectedColor = color
                            onCurrentColorChanged(color)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val offset =
                                if (change.position.distanceTo(size.center) < size.radius) {
                                    change.position
                                } else {
                                    val intersections = change.position.intersectCircle(
                                        size.center,
                                        size.radius
                                    )
                                    if (change.position.x > size.center.x) {
                                        intersections.maxBy { it.x }
                                    } else {
                                        intersections.minBy { it.x }
                                    }
                                }
                            val (color, _) = coordToColor(
                                offset,
                                size.center,
                                size.radius
                            )
                            selectedPoint = offset
                            selectedColor = color
                            onCurrentColorChanged(color)
                        }
                    }
            ) {
                drawIntoCanvas { canvas ->
                    canvas.drawHsvColorGradient(size)
                }
            }
            Canvas(
                modifier = Modifier.requiredSize(256.dp)
            ) {
                drawColorIndicator(selectedPoint, selectedColor)
            }
        }
    }
}

/**
 * Draws hsv color gradient with hue and saturation on a canvas.
 */
internal fun Canvas.drawHsvColorGradient(size: Size) {
    val center = size.center
    val radius = size.minDimension * 0.5f
    hsvSweepGradient.applyTo(size, huePaint, 1f)
    saturationGradient.applyTo(size, saturationPaint, 1f)
    drawCircle(center, radius, huePaint)
    drawCircle(center, radius, saturationPaint)
}

private val huePaint: Paint = Paint().apply { isAntiAlias = true }
private val saturationPaint: Paint = Paint().apply { isAntiAlias = true }

private val hsvSweepGradient = Brush.sweepGradient(
    0.000f to Color.Red,
    0.166f to Color.Magenta,
    0.333f to Color.Blue,
    0.499f to Color.Cyan,
    0.666f to Color.Green,
    0.833f to Color.Yellow,
    0.999f to Color.Red,
    // center = center,
)

private val saturationGradient = Brush.radialGradient(
    0f to Color(0xFFFFFFFF),
    1f to Color(0x00FFFFFF),
    // center = center,
    // radius = radius,
    tileMode = TileMode.Clamp,
) as RadialGradient

private const val SELECTOR_RADIUS: Float = 50f
private const val BORDER_WIDTH: Float = 10f

private fun DrawScope.drawColorIndicator(pos: Offset, color: Color) {
    drawCircle(color, SELECTOR_RADIUS, Offset(pos.x, pos.y))
    drawCircle(
        Color.White,
        SELECTOR_RADIUS - (BORDER_WIDTH / 2),
        Offset(pos.x, pos.y),
        style = Stroke(width = BORDER_WIDTH),
    )
}

private fun coordToColor(point: Offset, center: Offset, radius: Float): Pair<Color, Offset> {
    val vector = point - center
    val angle = vector.angle()
    val hue = angleToHue(angle) // hue: 0 to 360
    val sat = min(vector.length() / radius, 1f) // saturation: 0 to 1
    // adjust point in case saturation was out of bounds
    val newPoint = Offset.fromAngle(angle, sat * radius) + center
    return try {
        Color.hsv(hue, sat, 1f) to newPoint
    } catch (e: IllegalArgumentException) {
        // Just in case something goes wrong...
        Color.hsv(0f, 0f, 0f) to newPoint
    }
}