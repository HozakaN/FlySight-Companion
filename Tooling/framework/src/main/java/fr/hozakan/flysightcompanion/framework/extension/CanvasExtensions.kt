package fr.hozakan.flysightcompanion.framework.extension

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize

fun Canvas.drawRect(size: IntSize, paint: Paint) {
  drawRect(0f, 0f, size.width.toFloat(), size.height.toFloat(), paint)
}

fun Canvas.drawRoundRect(size: IntSize, radius: Float, paint: Paint) {
  drawRoundRect(0f, 0f, size.width.toFloat(), size.height.toFloat(), radius, radius, paint)
}