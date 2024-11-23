package fr.hozakan.flysightble.fsdevicemodule.ui.device_config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.composablecommons.distanceUnit
import fr.hozakan.flysightble.composablecommons.hourSpeed
import fr.hozakan.flysightble.composablecommons.rateMaximumLabel
import fr.hozakan.flysightble.composablecommons.rateMinimumLabel
import fr.hozakan.flysightble.composablecommons.toneMaximumLabel
import fr.hozakan.flysightble.composablecommons.toneMinimumLabel
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.defaultConfigFile

@Composable
fun DeviceConfigurationScreen(
    conf: ConfigFile
) {
    val factory = LocalViewModelFactory.current

    val viewModel: DeviceConfigurationViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = conf) {
        viewModel.loadConfiguration(conf)
    }

    val config = state.configuration

    DeviceConfigurationScreenInternal(
        config = config
    )
}

@Composable
fun DeviceConfigurationScreenInternal(
    config: ConfigFile
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "General",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Configuration name :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.name
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Dynamic model :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.dynamicModel.text
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Sample period (ms) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.samplePeriod}"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Tone",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Mode :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.toneMode.text
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = toneMinimumLabel(config.toneMode, config.unitSystem)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.toneMinimum}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = toneMaximumLabel(config.toneMode, config.unitSystem)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.toneMaximum}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Limit behaviour :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.toneLimitBehaviour.text
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Volume :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.toneVolume.text
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Rate",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Mode :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.rateMode.text
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = rateMinimumLabel(config.rateMode, config.unitSystem)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.rateMinimum}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = rateMaximumLabel(config.rateMode, config.unitSystem)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.rateMaximum}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Minimum rate (tone/s) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.rateMinimumValue}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Maximum rate (tone/s) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.rateMaximumValue}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Flat line at minimum rate :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = if (config.flatLineAtMinimumRate) "Yes" else "No"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Speech",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Period (s) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.speechRate}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Volume :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.speechVolume.text
                    )
                }
            }
            itemsIndexed(config.speeches) { index, speech ->
                Row {
                    Text(
                        text = "Speech ${index + 1} :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${speech.mode.text} ${speech.unit.text} ${speech.value}"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Thresholds",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Vertical speed (${hourSpeed(config.unitSystem)}) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.verticalThreshold}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Horizontal speed (${hourSpeed(config.unitSystem)}) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.horizontalThreshold}"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Miscellaneous",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Timezone offset (s) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.tzOffset}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Use SAS :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = if (config.useSAS) "Yes" else "No"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Initialization",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Mode :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.initMode.text
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Filename :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.initFile ?: ""
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Alarm",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Window above (${distanceUnit(config.unitSystem)}) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.windowAbove}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Window below (${distanceUnit(config.unitSystem)}) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.windowBelow}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Ground elevation (${distanceUnit(config.unitSystem)}) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.dzElev}"
                    )
                }
            }
            itemsIndexed(config.alarms) { index, alarm ->
                Row {
                    Text(
                        text = "Alarm ${index + 1} :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${alarm.alarmType.text} ${alarm.alarmElevation} ${alarm.alarmFile}"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Altitude",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Units :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.altitudeUnit.text
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Step :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.altitudeStep}"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = "Silence :",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            itemsIndexed(config.silenceWindows) { index, silence ->
                Row {
                    Text(
                        text = "Silence ${index + 1} :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${silence.top} - ${silence.bottom}"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun DeviceConfigurationScreenInternalPreview() {
    DeviceConfigurationScreenInternal(
        defaultConfigFile()
    )
}