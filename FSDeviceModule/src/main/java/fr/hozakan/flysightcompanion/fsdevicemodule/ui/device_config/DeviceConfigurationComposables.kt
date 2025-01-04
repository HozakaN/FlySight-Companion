package fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_config

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.composablecommons.rateMaximumLabel
import fr.hozakan.flysightcompanion.composablecommons.rateMinimumLabel
import fr.hozakan.flysightcompanion.composablecommons.toneMaximumLabel
import fr.hozakan.flysightcompanion.composablecommons.toneMinimumLabel
import fr.hozakan.flysightcompanion.designsystem.extension.distanceTextResource
import fr.hozakan.flysightcompanion.designsystem.extension.speedTextResource
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.extension.speedInUnit
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.extension.textResource
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.defaultConfigFile

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
            text = stringResource(if (showConfigAsRaw) R.string.device_configuration_formatted else R.string.device_configuration_raw)
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
                        text = stringResource(R.string.config_detail_configuration_section_general),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_conf_name)
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
                        text = stringResource(R.string.device_configuration_conf_desc)
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
                        text = stringResource(R.string.device_configuration_conf_group)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = config.group
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_dynamic_model)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = stringResource(config.dynamicModel.textResource)
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_sample_period)
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
                        text = stringResource(R.string.config_detail_configuration_section_tone),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_mode)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = stringResource(config.toneMode.textResource)
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
                        text = stringResource(R.string.device_configuration_limit_behaviour)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = stringResource(config.toneLimitBehaviour.textResource)
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_volume)
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
                        text = stringResource(R.string.config_detail_configuration_section_rate),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_mode)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = stringResource(config.rateMode.textResource)
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
                        text = stringResource(R.string.device_configuration_minimum_rate)
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
                        text = stringResource(R.string.device_configuration_maximum_rate)
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
                        text = stringResource(R.string.device_configuration_flatline)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = stringResource(
                            if (config.flatLineAtMinimumRate) {
                                R.string.misc_yes
                            } else {
                                R.string.misc_no
                            }
                        )
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = stringResource(
                            R.string.device_configuration_speech,
                            config.speeches.size
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_period)
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
                        text = stringResource(R.string.device_configuration_volume)
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
                        text = stringResource(
                            R.string.config_detail_configuration_speech_label,
                            index + 1
                        )
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${stringResource(speech.mode.textResource)} ${stringResource(speech.unit.speedTextResource)} ${speech.value}"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = stringResource(R.string.config_detail_configuration_section_thresholds),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "${
                            stringResource(
                                R.string.config_detail_configuration_vertical_speed,
                                stringResource(unitSystem.speedTextResource)
                            )
                        } :"
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
                        text = "${
                            stringResource(
                                R.string.config_detail_configuration_horizontal_speed,
                                stringResource(unitSystem.speedTextResource)
                            )
                        } :"
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
                        text = stringResource(R.string.config_detail_configuration_section_miscellaneous),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_timezone_offset)
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
                        text = stringResource(R.string.device_configuration_use_sas)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = stringResource(
                            if (config.useSAS) {
                                R.string.misc_yes
                            } else {
                                R.string.misc_no
                            }
                        )
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = stringResource(R.string.config_detail_configuration_section_initialization),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_mode)
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = stringResource(config.initMode.textResource)
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = stringResource(R.string.device_configuration_filename)
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
                        text = stringResource(
                            R.string.device_configuration_alarm,
                            config.alarms.size
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "${
                            stringResource(
                                R.string.config_detail_configuration_window_above,
                                stringResource(unitSystem.distanceTextResource)
                            )
                        } :"
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
                        text = "${
                            stringResource(
                                R.string.config_detail_configuration_window_below,
                                stringResource(unitSystem.distanceTextResource)
                            )
                        } :"
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
                        text = "${
                            stringResource(
                                R.string.config_detail_configuration_ground_elevation,
                                stringResource(unitSystem.distanceTextResource)
                            )
                        } :"
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
                        text = "${
                            stringResource(
                                R.string.device_configuration_alarm,
                                index + 1
                            )
                        } :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "${stringResource(alarm.alarmType.textResource)} ${alarm.alarmElevation} ${alarm.alarmFile}"
                    )
                }
            }
            item {
                Column {
                    Text(
                        text = stringResource(R.string.config_detail_configuration_section_altitude),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            item {
                Row {
                    Text(
                        text = "${stringResource(R.string.config_detail_configuration_units)} :"
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = stringResource(config.altitudeUnit.distanceTextResource)
                    )
                }
            }
            item {
                Row {
                    Text(
                        text = "${stringResource(R.string.config_detail_configuration_step)} :"
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
                        text = stringResource(
                            R.string.device_configuration_silence_label,
                            config.silenceWindows.size
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(modifier = Modifier.fillMaxSize())
                }
            }
            itemsIndexed(config.silenceWindows) { index, silence ->
                Row {
                    Text(
                        text = stringResource(
                            R.string.device_configuration_silence,
                            index + 1
                        )
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