package fr.hozakan.flysightble.fsdevicemodule.ui.device_config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.composablecommons.rateMaximumLabel
import fr.hozakan.flysightble.composablecommons.rateMinimumLabel
import fr.hozakan.flysightble.composablecommons.toneMaximumLabel
import fr.hozakan.flysightble.composablecommons.toneMinimumLabel
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.framework.extension.speedInUnit
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.UnitSystem
import fr.hozakan.flysightble.model.defaultConfigFile

@Composable
fun DeviceConfigurationMenuActions(
    conf: ConfigFile
) {
    val factory = LocalViewModelFactory.current

    val viewModel: DeviceConfigurationViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = conf) {
        viewModel.loadConfiguration(conf)
    }

    val showConfigAsRaw = state.showConfigAsRaw

    TextButton(
        onClick = {
            viewModel.updateShowConfigAsRaw(!showConfigAsRaw)
        }
    ) {
        Text(
            text = if (showConfigAsRaw) "Formatted" else "Raw"
        )
    }

}

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
        rawConf = state.rawConfiguration,
        config = config,
        unitSystem = state.unitSystem,
        showConfigAsRaw = state.showConfigAsRaw
    )
}

@Composable
fun DeviceConfigurationScreenInternal(
    rawConf: String,
    config: ConfigFile,
    unitSystem: UnitSystem,
    showConfigAsRaw: Boolean
) {
    if (showConfigAsRaw) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text(text = rawConf)
            }
        }
    } else {
        FormattedConfiguration(config, unitSystem)
    }
}

@Composable
private fun FormattedConfiguration(
    config: ConfigFile,
    unitSystem: UnitSystem
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
                        text = "Configuration description :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.description
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Configuration kind :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.kind
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
                        text = toneMinimumLabel(config.toneMode, unitSystem)
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
                        text = toneMaximumLabel(config.toneMode, unitSystem)
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
                        text = rateMinimumLabel(config.rateMode, unitSystem)
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
                        text = rateMaximumLabel(config.rateMode, unitSystem)
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
                        text = "Speech (${config.speeches.size})",
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
                        text = "${speech.mode.text} ${speech.unit.speedText} ${speech.value}"
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
                        text = "Vertical speed (${unitSystem.speedText}) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.verticalThreshold.speedInUnit(unitSystem)}"
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "Horizontal speed (${unitSystem.speedText}) :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${config.horizontalThreshold * 0.036}"
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
                        text = "Alarm (${config.alarms.size}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "Window above (${unitSystem.distanceText}) :"
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
                        text = "Window below (${unitSystem.distanceText}) :"
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
                        text = "Ground elevation (${unitSystem.distanceText}) :"
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
                        text = config.altitudeUnit.distanceText
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
                        text = "Silence (${config.silenceWindows.size})",
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
        rawConf = "",
        config = defaultConfigFile(),
        unitSystem = UnitSystem.Metric,
        showConfigAsRaw = false
    )
}