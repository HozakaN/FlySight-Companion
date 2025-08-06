package fr.hozakan.flysightcompanion.composablecommons

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun BatteryLevelContainerChargingPreview() {
    BatteryLevelContainer(
        batteryLevel = 50,
        isCharging = true
    )
}

@Preview
@Composable
fun BatteryLevelContainerNotChargingPreview() {
    BatteryLevelContainer(
        batteryLevel = 25,
        isCharging = false
    )
}
