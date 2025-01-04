package fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.composablecommons.DropdownContainer
import fr.hozakan.flysightcompanion.composablecommons.EmptyIntTextField
import fr.hozakan.flysightcompanion.composablecommons.ExpandableColumn
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.composablecommons.rateMaximumLabel
import fr.hozakan.flysightcompanion.composablecommons.rateMinimumLabel
import fr.hozakan.flysightcompanion.composablecommons.speechValueForMode
import fr.hozakan.flysightcompanion.composablecommons.speechValueFromMode
import fr.hozakan.flysightcompanion.composablecommons.speechValueLabel
import fr.hozakan.flysightcompanion.composablecommons.toneMaximumLabel
import fr.hozakan.flysightcompanion.composablecommons.toneMinimumLabel
import fr.hozakan.flysightcompanion.composablecommons.valueForRateMode
import fr.hozakan.flysightcompanion.composablecommons.valueForToneMode
import fr.hozakan.flysightcompanion.composablecommons.valueFromRateMode
import fr.hozakan.flysightcompanion.composablecommons.valueFromToneMode
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.extension.distanceTextResource
import fr.hozakan.flysightcompanion.designsystem.extension.fromText
import fr.hozakan.flysightcompanion.designsystem.extension.speedTextResource
import fr.hozakan.flysightcompanion.designsystem.extension.textResource
import fr.hozakan.flysightcompanion.designsystem.extension.unitNameResource
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.extension.distanceInUnit
import fr.hozakan.flysightcompanion.framework.extension.fromDistanceUnitToMeter
import fr.hozakan.flysightcompanion.framework.extension.fromSpeedUnitToCmPerSec
import fr.hozakan.flysightcompanion.framework.extension.speedInUnit
import fr.hozakan.flysightcompanion.model.config.Alarm
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.config.DynamicModel
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.config.RateMode
import fr.hozakan.flysightcompanion.model.config.SilenceWindow
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.ToneLimitBehaviour
import fr.hozakan.flysightcompanion.model.config.ToneMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.config.Volume
import fr.hozakan.flysightcompanion.model.defaultConfigFile

@Composable
fun ConfigDetailMenuActions() {

    val factory = LocalViewModelFactory.current

    val viewModel: ConfigDetailViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val unitSystem = state.unitSystem

    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            expanded = !expanded
        }
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(
                R.string.config_detail_menu_action_unit_system_picker_content_description
            )
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(UnitSystem.Metric.unitNameResource)) },
            leadingIcon = {
                RadioButton(
                    selected = unitSystem == UnitSystem.Metric,
                    onClick = {
                        viewModel.updateUnitSystem(UnitSystem.Metric)
                        expanded = false
                    }
                )
            },
            onClick = {
                viewModel.updateUnitSystem(UnitSystem.Metric)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(UnitSystem.Imperial.unitNameResource)) },
            leadingIcon = {
                RadioButton(
                    selected = unitSystem == UnitSystem.Imperial,
                    onClick = {
                        viewModel.updateUnitSystem(UnitSystem.Imperial)
                        expanded = false
                    }
                )
            },
            onClick = {
                viewModel.updateUnitSystem(UnitSystem.Imperial)
                expanded = false
            }
        )
    }
}

@Composable
fun ConfigDetailScreen(
    configName: String,
    onNavigateUp: () -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: ConfigDetailViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = configName) {
        viewModel.loadConfigFile(configName)
    }

    if (state.fileSaved?.getContentIfNotHandled() == true) {
        onNavigateUp()
    }

    ConfigDetailScreenInternal(
        state = state,
        updateConfigFileName = {
            viewModel.updateConfigFileName(it)
        },
        updateConfigFileDescription = {
            viewModel.updateConfigFileDescription(it)
        },
        updateConfigFileKind = {
            viewModel.updateConfigFileKind(it)
        },
        updateDynamicModel = {
            viewModel.updateDynamicModel(it)
        },
        updateSamplePeriod = {
            viewModel.updateSamplePeriod(it)
        },
        updateUseSAS = {
            viewModel.updateUseSAS(it)
        },
        updateToneMode = {
            viewModel.updateToneMode(it)
        },
        updateToneMinimum = {
            viewModel.updateToneMinimum(it)
        },
        updateToneMaximum = {
            viewModel.updateToneMaximum(it)
        },
        updateToneLimitBehaviour = {
            viewModel.updateToneLimitBehaviour(it)
        },
        updateToneVolume = {
            viewModel.updateToneVolume(it)
        },
        updateRateMode = {
            viewModel.updateRateMode(it)
        },
        updateRateMinimumValue = {
            viewModel.updateRateMinimumValue(it)
        },
        updateRateMaximumValue = {
            viewModel.updateRateMaximumValue(it)
        },
        updateRateMinimum = {
            viewModel.updateRateMinimum(it)
        },
        updateRateMaximum = {
            viewModel.updateRateMaximum(it)
        },
        updateFlatLineAtMinimumRate = {
            viewModel.updateFlatLineAtMinimumRate(it)
        },
        updateSpeechRate = {
            viewModel.updateRateMaximumValue(it)
        },
        updateSpeechVolume = {
            viewModel.updateSpeechVolume(it)
        },
        addSpeech = {
            viewModel.addSpeech(it)
        },
        deleteSpeech = {
            viewModel.deleteSpeech(it)
        },
        updateVerticalThreshold = {
            viewModel.updateVerticalThreshold(it)
        },
        updateHorizontalThreshold = {
            viewModel.updateHorizontalThreshold(it)
        },
        updateInitMode = {
            viewModel.updateInitMode(it)
        },
        updateInitFile = {
            viewModel.updateInitFile(it)
        },
        updateWindowAbove = {
            viewModel.updateWindowAbove(it)
        },
        updateWindowBelow = {
            viewModel.updateWindowBelow(it)
        },
        updateDzElev = {
            viewModel.updateDzElev(it)
        },
        addAlarm = {
            viewModel.addAlarm(it)
        },
        deleteAlarm = {
            viewModel.deleteAlarm(it)
        },
        updateAltitudeUnit = {
            viewModel.updateAltitudeUnit(it)
        },
        updateAltitudeStep = {
            viewModel.updateAltitudeStep(it)
        },
        addSilenceWindow = {
            viewModel.addSilenceWindow(it)
        },
        deleteSilenceWindow = {
            viewModel.deleteSilenceWindow(it)
        },
        saveConfigFile = {
            viewModel.saveConfigFile()
        },
        onNavigateUp = onNavigateUp
    )

}

@Composable
fun ConfigDetailScreenInternal(
    state: ConfigDetailState,
    updateConfigFileName: (String) -> Unit,
    updateConfigFileDescription: (String) -> Unit,
    updateConfigFileKind: (String) -> Unit,
    updateDynamicModel: (DynamicModel) -> Unit,
    updateSamplePeriod: (Int) -> Unit,
    updateUseSAS: (Boolean) -> Unit,
    updateToneMode: (ToneMode) -> Unit,
    updateToneMinimum: (Int) -> Unit,
    updateToneMaximum: (Int) -> Unit,
    updateToneLimitBehaviour: (ToneLimitBehaviour) -> Unit,
    updateToneVolume: (Volume) -> Unit,
    updateRateMode: (RateMode) -> Unit,
    updateRateMaximumValue: (Int) -> Unit,
    updateRateMinimumValue: (Int) -> Unit,
    updateRateMaximum: (Int) -> Unit,
    updateRateMinimum: (Int) -> Unit,
    updateFlatLineAtMinimumRate: (Boolean) -> Unit,
    updateSpeechRate: (Int) -> Unit,
    updateSpeechVolume: (Volume) -> Unit,
    addSpeech: (Speech) -> Unit,
    deleteSpeech: (Speech) -> Unit,
    updateVerticalThreshold: (Int) -> Unit,
    updateHorizontalThreshold: (Int) -> Unit,
    updateInitMode: (InitMode) -> Unit,
    updateInitFile: (String) -> Unit,
    updateWindowAbove: (Int) -> Unit,
    updateWindowBelow: (Int) -> Unit,
    updateDzElev: (Int) -> Unit,
    addAlarm: (Alarm) -> Unit,
    deleteAlarm: (Alarm) -> Unit,
    updateAltitudeUnit: (UnitSystem) -> Unit,
    updateAltitudeStep: (Int) -> Unit,
    addSilenceWindow: (SilenceWindow) -> Unit,
    deleteSilenceWindow: (SilenceWindow) -> Unit,
    saveConfigFile: () -> Unit,
    onNavigateUp: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (!state.configFileFound) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.config_detail_config_file_not_found))
            }
            return@Surface
        }
        val configFile = state.configFile
        val unitSystem = state.unitSystem

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = configFile.name,
                    onValueChange = {
                        updateConfigFileName(it)
                    },
                    label = {
                        Text(text = stringResource(R.string.config_detail_configuration_name))
                    },
                    isError = !state.hasValidFileName
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = configFile.description,
                    onValueChange = {
                        updateConfigFileDescription(it)
                    },
                    label = {
                        Text(text = stringResource(R.string.config_detail_configuration_description))
                    }
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = configFile.group,
                    onValueChange = {
                        updateConfigFileKind(it)
                    },
                    label = {
                        Text(text = stringResource(R.string.config_detail_configuration_group))
                    }
                )
            }
            item {
                ExpandableColumn(
                    headerComposable = {
                        Text(text = stringResource(R.string.config_detail_configuration_section_general))
                    }
                ) {
                    DynamicModelContainer(
                        dynamicModel = configFile.dynamicModel,
                        onSelectionChanged = {
                            updateDynamicModel(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.config_detail_configuration_section_general_sample_period),
                        intValue = configFile.samplePeriod,
                        onValueChanged = {
                            if (it != null) {
                                updateSamplePeriod(it)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = configFile.useSAS,
                            onCheckedChange = {
                                updateUseSAS(it)
                            },
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        Text(text = stringResource(R.string.config_detail_configuration_section_general_use_sas))
                    }
                }
            }
            item {
                ExpandableColumn(
                    headerComposable = {
                        Text(text = stringResource(R.string.config_detail_configuration_section_tone))
                    }
                ) {
                    ToneModeContainer(
                        toneMode = configFile.toneMode,
                        onSelectionChanged = {
                            updateToneMode(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = toneMinimumLabel(configFile.toneMode, unitSystem),
                        intValue = configFile.toneMinimum.valueForToneMode(
                            configFile.toneMode,
                            unitSystem
                        ),
                        onValueChanged = {
                            if (it != null) {
                                updateToneMinimum(
                                    it.valueFromToneMode(
                                        configFile.toneMode,
                                        unitSystem
                                    )
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = toneMaximumLabel(configFile.toneMode, unitSystem),
                        intValue = configFile.toneMaximum.valueForToneMode(
                            configFile.toneMode,
                            unitSystem
                        ),
                        onValueChanged = {
                            if (it != null) {
                                updateToneMaximum(
                                    it.valueFromToneMode(
                                        configFile.toneMode,
                                        unitSystem
                                    )
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    LimitBehaviourContainer(
                        limitBehaviour = configFile.toneLimitBehaviour,
                        onSelectionChanged = {
                            updateToneLimitBehaviour(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    VolumeContainer(
                        volume = configFile.toneVolume,
                        onSelectionChanged = {
                            updateToneVolume(it)
                        }
                    )
                }
            }
            item {
                ExpandableColumn(
                    expanded = false,
                    headerComposable = {
                        Text(text = stringResource(R.string.config_detail_configuration_section_rate))
                    }
                ) {
                    RateModeContainer(
                        rateMode = configFile.rateMode,
                        onSelectionChanged = {
                            updateRateMode(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = rateMinimumLabel(configFile.rateMode, unitSystem),
                        intValue = configFile.rateMinimumValue.valueForRateMode(
                            configFile.rateMode,
                            unitSystem
                        ),
                        onValueChanged = {
                            if (it != null) {
                                updateRateMinimumValue(
                                    it.valueFromRateMode(
                                        configFile.rateMode,
                                        unitSystem
                                    )
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = rateMaximumLabel(configFile.rateMode, unitSystem),
                        intValue = configFile.rateMaximumValue.valueForRateMode(
                            configFile.rateMode,
                            unitSystem
                        ),
                        onValueChanged = {
                            if (it != null) {
                                updateRateMaximumValue(
                                    it.valueFromRateMode(
                                        configFile.rateMode,
                                        unitSystem
                                    )
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(
                            R.string.config_detail_configuration_minimum_rate
                        ),
                        intValue = configFile.rateMinimum,
                        onValueChanged = {
                            if (it != null) {
                                updateRateMinimum(it)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(
                            R.string.config_detail_configuration_maximum_rate
                        ),
                        intValue = configFile.rateMaximum,
                        onValueChanged = {
                            if (it != null) {
                                updateRateMaximum(it)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = configFile.flatLineAtMinimumRate,
                            onCheckedChange = {
                                updateFlatLineAtMinimumRate(it)
                            },
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        Text(
                            text = stringResource(
                                R.string.config_detail_configuration_flatline_at_minimum_rate
                            )
                        )
                    }
                }
                ExpandableColumn(
                    headerComposable = {
                        Text(
                            text = stringResource(
                                R.string.config_detail_configuration_section_speech
                            )
                        )
                    }
                ) {
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.config_detail_configuration_period),
                        intValue = configFile.speechRate,
                        onValueChanged = {
                            if (it != null) {
                                updateSpeechRate(it)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    VolumeContainer(
                        volume = configFile.speechVolume,
                        onSelectionChanged = {
                            updateSpeechVolume(it)
                        }
                    )
                    if (configFile.speeches.isNotEmpty()) {
                        Spacer(modifier = Modifier.requiredHeight(8.dp))
                    }
                    configFile.speeches.forEachIndexed { index, speech ->
                        SpeechItemContainer(
                            index = index + 1,
                            speech = speech,
                            onDeleteClicked = {
                                deleteSpeech(speech)
                            }
                        )
                        if (index < configFile.speeches.size - 1) {
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    var addSpeechClicked by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Button(
                            onClick = {
                                addSpeechClicked = true
                            }
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.config_detail_configuration_add_speech
                                )
                            )
                        }
                    }
                    if (addSpeechClicked) {
                        AddSpeechDialog(
                            onSpeechAdded = {
                                addSpeech(it)
                                addSpeechClicked = false
                            },
                            onDismiss = {
                                addSpeechClicked = false
                            }
                        )
                    }
                }
                ExpandableColumn(
                    headerComposable = {
                        Text(
                            text = stringResource(
                                R.string.config_detail_configuration_section_thresholds
                            )
                        )
                    }
                ) {
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(
                            R.string.config_detail_configuration_vertical_speed,
                            stringResource(unitSystem.speedTextResource)
                        ),
                        intValue = configFile.verticalThreshold.speedInUnit(unitSystem),
                        onValueChanged = {
                            if (it != null) {
                                updateVerticalThreshold(it.fromSpeedUnitToCmPerSec(unitSystem))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(
                            R.string.config_detail_configuration_horizontal_speed,
                            stringResource(unitSystem.speedTextResource)
                        ),
                        intValue = configFile.horizontalThreshold.speedInUnit(unitSystem),
                        onValueChanged = {
                            if (it != null) {
                                updateHorizontalThreshold(it.fromSpeedUnitToCmPerSec(unitSystem))
                            }
                        }
                    )
                }
                ExpandableColumn(
                    headerComposable = {
                        Text(
                            text = stringResource(
                                R.string.config_detail_configuration_section_initialization
                            )
                        )
                    }
                ) {
                    InitModeContainer(
                        initMode = configFile.initMode,
                        onSelectionChanged = {
                            updateInitMode(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = configFile.initFile ?: "",
                        onValueChange = {
                            updateInitFile(it)
                        },
                        label = {
                            Text(
                                text = stringResource(
                                    R.string.config_detail_configuration_alarm_filename_label
                                )
                            )
                        }
                    )
                }
                ExpandableColumn(
                    headerComposable = {
                        Text(
                            text = stringResource(
                                R.string.config_detail_configuration_section_alarms
                            )
                        )
                    }
                ) {
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(
                            R.string.config_detail_configuration_window_above,
                            stringResource(unitSystem.distanceTextResource)
                        ),
                        intValue = configFile.windowAbove.distanceInUnit(unitSystem),
                        onValueChanged = {
                            if (it != null) {
                                updateWindowAbove(it.fromDistanceUnitToMeter(unitSystem))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(
                            R.string.config_detail_configuration_window_below,
                            stringResource(unitSystem.distanceTextResource)
                        ),
                        intValue = configFile.windowBelow.distanceInUnit(unitSystem),
                        onValueChanged = {
                            if (it != null) {
                                updateWindowBelow(it.fromDistanceUnitToMeter(unitSystem))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(
                            R.string.config_detail_configuration_ground_elevation,
                            stringResource(unitSystem.distanceTextResource)
                        ),
                        intValue = configFile.dzElev.distanceInUnit(unitSystem),
                        onValueChanged = {
                            if (it != null) {
                                updateDzElev(it.fromDistanceUnitToMeter(unitSystem))
                            }
                        }
                    )
                    if (configFile.alarms.isNotEmpty()) {
                        Spacer(modifier = Modifier.requiredHeight(8.dp))
                    }
                    val sortedAlarms =
                        remember(configFile.alarms) { configFile.alarms.sortedByDescending { it.alarmElevation } }
                    sortedAlarms.forEachIndexed { index, alarm ->
                        AlarmItemContainer(
                            index = index + 1,
                            alarm = alarm,
                            unitSystem = unitSystem,
                            onDeleteClicked = {
                                deleteAlarm(alarm)
                            }
                        )
                        if (index < configFile.alarms.size - 1) {
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    var addAlarmClicked by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Button(
                            onClick = {
                                addAlarmClicked = true
                            }
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.config_detail_configuration_add_alarm
                                )
                            )
                        }
                    }
                    if (addAlarmClicked) {
                        AddAlarmDialog(
                            unitSystem = unitSystem,
                            onAlarmAdded = {
                                addAlarm(it)
                                addAlarmClicked = false
                            },
                            onDismiss = {
                                addAlarmClicked = false
                            }
                        )
                    }
                }
                ExpandableColumn(
                    headerComposable = {
                        Text(
                            text = stringResource(
                                R.string.config_detail_configuration_section_altitude
                            )
                        )
                    }
                ) {
                    DistanceUnitContainer(
                        label = stringResource(R.string.config_detail_configuration_units),
                        unitSystem = configFile.altitudeUnit,
                        onSelectionChanged = {
                            updateAltitudeUnit(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.config_detail_configuration_step),
                        intValue = configFile.altitudeStep.distanceInUnit(configFile.altitudeUnit),
                        onValueChanged = {
                            if (it != null) {
                                updateAltitudeStep(it.fromDistanceUnitToMeter(configFile.altitudeUnit))
                            }
                        }
                    )
                }
                ExpandableColumn(
                    headerComposable = {
                        Text(
                            text = stringResource(
                                R.string.config_detail_configuration_section_silence
                            )
                        )
                    }
                ) {
                    configFile.silenceWindows.forEachIndexed { index, silenceWindow ->
                        SilenceItemContainer(
                            index = index + 1,
                            silenceWindow = silenceWindow,
                            unitSystem = unitSystem,
                            onDeleteClicked = {
                                deleteSilenceWindow(silenceWindow)
                            }
                        )
                        if (index < configFile.silenceWindows.size - 1) {
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    var addSilenceClicked by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Button(
                            onClick = {
                                addSilenceClicked = true
                            }
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.config_detail_configuration_add_silence_window
                                )
                            )
                        }
                    }
                    if (addSilenceClicked) {
                        AddSilenceWindowDialog(
                            unitSystem = unitSystem,
                            onSilenceAdded = {
                                addSilenceWindow(it)
                                addSilenceClicked = false
                            },
                            onDismiss = {
                                addSilenceClicked = false
                            }
                        )
                    }
                }
            }
            item {
                SimpleDialogActionBar(
                    onCancel = onNavigateUp,
                    onValidate = {
                        saveConfigFile()
                    }
                )
            }
        }
    }
}

@Composable
fun AddSpeechDialog(
    onSpeechAdded: (Speech) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        var speechMode: SpeechMode by remember { mutableStateOf(SpeechMode.HorizontalSpeed) }
        var unitSystem: UnitSystem by remember { mutableStateOf(UnitSystem.Metric) }
        var speechValue by remember { mutableIntStateOf(0) }
        Card {
            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                SpeechModeContainer(
                    speechMode = speechMode,
                    onSelectionChanged = {
                        speechMode = it
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                when (speechMode) {
                    SpeechMode.HorizontalSpeed,
                    SpeechMode.VerticalSpeed,
                    SpeechMode.TotalSpeed -> {
                        SpeedUnitContainer(
                            label = stringResource(R.string.config_detail_configuration_units),
                            unitSystem = unitSystem,
                            onSelectionChanged = {
                                unitSystem = it
                            }
                        )
                        Spacer(modifier = Modifier.requiredHeight(8.dp))
                    }

                    SpeechMode.GlideRatio,
                    SpeechMode.InverseGlideRatio,
                    SpeechMode.DiveAngle -> {
                    }

                    SpeechMode.AltitudeAboveDropzone -> {
                        DistanceUnitContainer(
                            label = stringResource(R.string.config_detail_configuration_units),
                            unitSystem = unitSystem,
                            onSelectionChanged = {
                                unitSystem = it
                            }
                        )
                        Spacer(modifier = Modifier.requiredHeight(8.dp))
                    }
                }
                EmptyIntTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = speechValueLabel(speechMode, unitSystem),
                    intValue = speechValue.speechValueForMode(speechMode, unitSystem),
                    onValueChanged = {
                        speechValue = it?.speechValueFromMode(speechMode, unitSystem) ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                SimpleDialogActionBar(
                    onCancel = onDismiss,
                    onValidate = {
                        onSpeechAdded(
                            Speech(
                                mode = speechMode,
                                unit = unitSystem,
                                value = speechValue
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AddAlarmDialog(
    unitSystem: UnitSystem,
    onAlarmAdded: (Alarm) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        var alarmType: AlarmType by remember { mutableStateOf(AlarmType.NoAlarm) }
        var alarmElevation by remember { mutableIntStateOf(0) }
        var fileName by remember { mutableStateOf("") }

        Card {
            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                AlarmTypeContainer(
                    alarmType = alarmType,
                    onSelectionChanged = {
                        alarmType = it
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                EmptyIntTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(
                        R.string.config_detail_configuration_alarm_elevation_label,
                        stringResource(unitSystem.distanceTextResource)
                    ),
                    intValue = alarmElevation.distanceInUnit(unitSystem),
                    onValueChanged = {
                        alarmElevation = it?.fromDistanceUnitToMeter(unitSystem) ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                if (alarmType == AlarmType.PlayFile) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = fileName,
                        onValueChange = {
                            fileName = it
                        },
                        label = {
                            Text(
                                text = stringResource(
                                    R.string.config_detail_configuration_alarm_filename_label
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                }
                SimpleDialogActionBar(
                    onCancel = onDismiss,
                    onValidate = {
                        onAlarmAdded(
                            Alarm(
                                alarmType = alarmType,
                                alarmElevation = alarmElevation,
                                alarmFile = fileName
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AddSilenceWindowDialog(
    unitSystem: UnitSystem,
    onSilenceAdded: (SilenceWindow) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        var windowTop by remember { mutableIntStateOf(0) }
        var windowBottom by remember { mutableIntStateOf(0) }

        Card {
            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                EmptyIntTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(
                        R.string.config_detail_configuration_silence_window_top_label,
                        stringResource(unitSystem.distanceTextResource)
                    ),
                    intValue = windowTop.distanceInUnit(unitSystem),
                    onValueChanged = {
                        windowTop = it?.fromDistanceUnitToMeter(unitSystem) ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                EmptyIntTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(
                        R.string.config_detail_configuration_silence_window_bottom_label,
                        stringResource(unitSystem.distanceTextResource)
                    ),
                    intValue = windowBottom.distanceInUnit(unitSystem),
                    onValueChanged = {
                        windowBottom = it?.fromDistanceUnitToMeter(unitSystem) ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                SimpleDialogActionBar(
                    onCancel = onDismiss,
                    onValidate = {
                        onSilenceAdded(
                            SilenceWindow(
                                top = windowTop,
                                bottom = windowBottom
                            )
                        )
                    }
                )
            }
        }
    }
}


@Composable
fun InitModeContainer(
    modifier: Modifier = Modifier,
    initMode: InitMode,
    onSelectionChanged: (InitMode) -> Unit,
) {
    val context = LocalContext.current
    DropdownContainer(
        modifier = modifier,
        label = stringResource(R.string.config_detail_configuration_init_mode),
        selectedValue = stringResource(initMode.textResource),
        options = remember { InitMode.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newInitMode ->
            InitMode.fromText(context, newInitMode)?.let {
                onSelectionChanged(it)
            }
        }
    )
}

@Composable
fun SpeechItemContainer(
    index: Int,
    speech: Speech,
    onDeleteClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.config_detail_configuration_speech_label,
                        index
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDeleteClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(
                            R.string.config_detail_configuration_delete_speech
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(speech.mode.textResource)
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                "${speechValueLabel(speech.mode, speech.unit)}: ${
                    speech.value.speechValueForMode(
                        speech.mode,
                        speech.unit
                    )
                }"
            )
        }
    }
}

@Composable
fun AlarmItemContainer(
    index: Int,
    alarm: Alarm,
    unitSystem: UnitSystem,
    onDeleteClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.config_detail_configuration_alarm_label,
                        index
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDeleteClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(
                            R.string.config_detail_configuration_delete_alarm
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(alarm.alarmType.textResource)
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(
                    R.string.config_detail_configuration_alarm_elevation,
                    stringResource(unitSystem.distanceTextResource),
                    alarm.alarmElevation.distanceInUnit(
                        unitSystem
                    )
                )
            )
            if (alarm.alarmType == AlarmType.PlayFile) {
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                Text(
                    text = stringResource(
                        R.string.config_detail_configuration_alarm_filename,
                        alarm.alarmFile
                    )
                )
            }
        }
    }
}

@Composable
fun SilenceItemContainer(
    index: Int,
    silenceWindow: SilenceWindow,
    unitSystem: UnitSystem,
    onDeleteClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.config_detail_configuration_silence_window_label,
                        index
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDeleteClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(
                            R.string.config_detail_configuration_delete_silence
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(
                    R.string.config_detail_configuration_silence_window_top,
                    stringResource(unitSystem.distanceTextResource),
                    silenceWindow.top.distanceInUnit(unitSystem)
                )
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(
                    R.string.config_detail_configuration_silence_window_bottom,
                    stringResource(unitSystem.distanceTextResource),
                    silenceWindow.bottom.distanceInUnit(unitSystem)
                )
            )
        }
    }
}

@Composable
internal fun SpeechModeContainer(
    modifier: Modifier = Modifier,
    speechMode: SpeechMode,
    onSelectionChanged: (SpeechMode) -> Unit
) {
    val context = LocalContext.current
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_speech_mode),
        selectedValue = stringResource(speechMode.textResource),
        options = remember { SpeechMode.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newSpeechMode ->
            SpeechMode.fromText(context, newSpeechMode)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun SpeedUnitContainer(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.config_detail_configuration_speed_unit),
    unitSystem: UnitSystem,
    onSelectionChanged: (UnitSystem) -> Unit
) {
    val resources = LocalContext.current.resources
    val options = remember { UnitSystem.entries.map { resources.getString(it.speedTextResource) } }

    DropdownContainer(
        label = label,
        selectedValue = stringResource(unitSystem.speedTextResource),
        options = options,
        onSelectionChanged = { newUnit ->
            UnitSystem.fromValue(options.indexOf(newUnit) + 1)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun DistanceUnitContainer(
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.config_detail_configuration_distance_unit),
    unitSystem: UnitSystem,
    onSelectionChanged: (UnitSystem) -> Unit
) {
    val resources = LocalContext.current.resources
    val options =
        remember { UnitSystem.entries.map { resources.getString(it.distanceTextResource) } }
    DropdownContainer(
        label = label,
        selectedValue = stringResource(unitSystem.distanceTextResource),
        options = options,
        onSelectionChanged = { newUnit ->
            UnitSystem.fromValue(options.indexOf(newUnit) + 1)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun AlarmTypeContainer(
    modifier: Modifier = Modifier,
    alarmType: AlarmType,
    onSelectionChanged: (AlarmType) -> Unit
) {
    val context = LocalContext.current
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_alarm_type),
        selectedValue = stringResource(alarmType.textResource),
        options = remember { AlarmType.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newAlarmType ->
            AlarmType.fromText(context, newAlarmType)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun DynamicModelContainer(
    modifier: Modifier = Modifier,
    dynamicModel: DynamicModel,
    onSelectionChanged: (DynamicModel) -> Unit
) {
    val context = LocalContext.current
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_dynamic_model),
        selectedValue = stringResource(dynamicModel.textResource),
        options = remember { DynamicModel.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newModel ->
            DynamicModel.fromText(context, newModel)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun ToneModeContainer(
    modifier: Modifier = Modifier,
    toneMode: ToneMode,
    onSelectionChanged: (ToneMode) -> Unit
) {
    val context = LocalContext.current
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_tone_mode),
        selectedValue = stringResource(toneMode.textResource),
        options = remember { ToneMode.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newMode ->
            ToneMode.fromText(context, newMode)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun LimitBehaviourContainer(
    modifier: Modifier = Modifier,
    limitBehaviour: ToneLimitBehaviour,
    onSelectionChanged: (ToneLimitBehaviour) -> Unit
) {
    val context = LocalContext.current
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_limit_behavior),
        selectedValue = stringResource(limitBehaviour.textResource),
        options = remember { ToneLimitBehaviour.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newMode ->
            ToneLimitBehaviour.fromText(context, newMode)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun VolumeContainer(
    modifier: Modifier = Modifier,
    volume: Volume,
    onSelectionChanged: (Volume) -> Unit
) {
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_volume),
        selectedValue = volume.text,
        options = remember { Volume.entries.map { it.text } },
        onSelectionChanged = { newMode ->
            Volume.fromText(newMode)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun RateModeContainer(
    modifier: Modifier = Modifier,
    rateMode: RateMode,
    onSelectionChanged: (RateMode) -> Unit
) {
    val context = LocalContext.current
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_rate_mode),
        selectedValue = stringResource(rateMode.textResource),
        options = remember { RateMode.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newMode ->
            RateMode.fromText(context, newMode)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun ConfigDetailScreenInternalPreview() {
    ConfigDetailScreenInternal(
        state = ConfigDetailState(
            configFile = defaultConfigFile(),
            unitSystem = UnitSystem.Metric,
            configFileFound = true,
            hasValidFileName = true
        ),
        updateConfigFileName = {},
        updateConfigFileDescription = {},
        updateConfigFileKind = {},
        updateDynamicModel = {},
        updateSamplePeriod = {},
        updateUseSAS = {},
        updateToneMode = {},
        updateToneMinimum = {},
        updateToneMaximum = {},
        updateToneLimitBehaviour = {},
        updateToneVolume = {},
        updateRateMode = {},
        updateRateMinimumValue = {},
        updateRateMaximumValue = {},
        updateRateMinimum = {},
        updateRateMaximum = {},
        updateFlatLineAtMinimumRate = {},
        updateSpeechRate = {},
        updateSpeechVolume = {},
        addSpeech = {},
        deleteSpeech = {},
        updateVerticalThreshold = {},
        updateHorizontalThreshold = {},
        updateInitMode = {},
        updateInitFile = {},
        updateWindowAbove = {},
        updateWindowBelow = {},
        updateDzElev = {},
        addAlarm = {},
        deleteAlarm = {},
        updateAltitudeUnit = {},
        updateAltitudeStep = {},
        addSilenceWindow = {},
        deleteSilenceWindow = {},
        saveConfigFile = {},
    ) { }
}