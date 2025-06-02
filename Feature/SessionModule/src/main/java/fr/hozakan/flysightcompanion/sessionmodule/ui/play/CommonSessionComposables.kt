package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.math.meterSecondToKmh
import fr.hozakan.flysightcompanion.model.ui.SpeedOrientation
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionController


@Composable
internal fun SpeedContainer(orientation: SpeedOrientation, player: SessionController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "",
            modifier = Modifier.graphicsLayer {
                rotationZ = when (orientation) {
                    SpeedOrientation.Horizontal -> 0f
                    SpeedOrientation.Vertical -> 90f
                    SpeedOrientation.Total -> 45f
                }
            },
            tint = Color.Green
        )
        val gnssData by player.gnssFlow.collectAsState(initial = null)

        FText(
            text = "${
                when (orientation) {
                    SpeedOrientation.Horizontal -> remember(gnssData?.gSpeed) { gnssData?.gSpeed?.meterSecondToKmh() }
                    SpeedOrientation.Vertical -> remember(gnssData?.velD) { gnssData?.velD?.meterSecondToKmh() }
                    SpeedOrientation.Total -> remember(gnssData?.speed) { gnssData?.speed?.meterSecondToKmh() }
                }
            }",
            configuration = FlySightTheme.typography.sessionPlayerValue,
            color = Color.Green
        )

        FText(
            text = " km/h",
            configuration = FlySightTheme.typography.sessionPlayerText,
            color = Color.Green
        )
    }
}

@Composable
internal fun TagAndValueContainer(tag: String, value: String, suffix: String = "") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FText(
            text = tag,
            configuration = FlySightTheme.typography.sessionPlayerText,
            color = Color.Green
        )
        Spacer(modifier = Modifier.requiredWidth(8.dp))
        FText(
            text = value,
            configuration = FlySightTheme.typography.sessionPlayerValue,
            color = Color.Green
        )
        if (suffix.isNotBlank()) {
            FText(
                text = suffix,
                configuration = FlySightTheme.typography.sessionPlayerText,
                color = Color.Green
            )
        }
    }
}