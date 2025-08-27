package fr.hozakan.flysightcompanion.composablecommons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BatteryLevelContainer(
    batteryLevel: Int,
    isCharging: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.requiredSize(width = 42.dp, height = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val batteryWidth = size.width * 0.7f
                val batteryHeight = size.height * 0.9f
                val cornerRadius = 4.dp.toPx()
                val strokeWidth = 2.dp.toPx()

                // Draw battery body
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(
                        (size.width - batteryWidth) / 2,
                        (size.height - batteryHeight) / 2
                    ),
                    size = androidx.compose.ui.geometry.Size(batteryWidth, batteryHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                    style = Stroke(width = strokeWidth)
                )

                // Draw battery terminal
                drawRoundRect(
                    color = Color.Gray,
                    topLeft = Offset(
                        size.width * 0.7f + (size.width - batteryWidth) / 2,
                        size.height * 0.25f
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        strokeWidth * 1.5f,
                        size.height * 0.5f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius / 2),
                    style = Stroke(width = strokeWidth)
                )

                // Draw battery level
                val levelPercentage = batteryLevel / 100f
                val levelWidth = (batteryWidth - strokeWidth * 2) * levelPercentage
                val levelHeight = batteryHeight - strokeWidth * 2

                if (levelWidth > 0) {
                    drawRect(
                        color = when {
                            batteryLevel > 50 -> Color.Green
                            batteryLevel > 20 -> Color.Yellow
                            else -> Color.Red
                        },
                        topLeft = Offset(
                            ((size.width - batteryWidth) / 2) + strokeWidth,
                            ((size.height - batteryHeight) / 2) + strokeWidth
                        ),
                        size = androidx.compose.ui.geometry.Size(levelWidth, levelHeight)
                    )
                }
            }
        }
//        if (isCharging) {
//            Spacer(modifier = Modifier.requiredWidth(4.dp))
//            Box(
//                modifier = Modifier.requiredSize(width = 12.dp, height = 16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.ElectricBolt,
//                    contentDescription = "Charging",
//                    tint = Color.Yellow
//                )
//            }
//        }
        Spacer(modifier = Modifier.requiredWidth(4.dp))
        Text(
            text = "$batteryLevel%",
            style = MaterialTheme.typography.bodySmall,
            color = when {
                batteryLevel > 50 -> Color.Green
                batteryLevel > 20 -> Color.Yellow
                else -> Color.Red
            }
        )
    }
}
