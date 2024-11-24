package fr.hozakan.flysightble.configfilesmodule.ui.config_detail

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.composablecommons.DropdownContainer
import fr.hozakan.flysightble.composablecommons.EmptyIntTextField
import fr.hozakan.flysightble.composablecommons.ExpandableColumn
import fr.hozakan.flysightble.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightble.composablecommons.rateMaximumLabel
import fr.hozakan.flysightble.composablecommons.rateMinimumLabel
import fr.hozakan.flysightble.composablecommons.speechValueForMode
import fr.hozakan.flysightble.composablecommons.speechValueFromMode
import fr.hozakan.flysightble.composablecommons.speechValueLabel
import fr.hozakan.flysightble.composablecommons.toneMaximumLabel
import fr.hozakan.flysightble.composablecommons.toneMinimumLabel
import fr.hozakan.flysightble.composablecommons.valueForRateMode
import fr.hozakan.flysightble.composablecommons.valueForToneMode
import fr.hozakan.flysightble.composablecommons.valueFromRateMode
import fr.hozakan.flysightble.composablecommons.valueFromToneMode
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.framework.extension.distanceInUnit
import fr.hozakan.flysightble.framework.extension.fromDistanceUnitToMeter
import fr.hozakan.flysightble.framework.extension.fromSpeedUnitToCmPerSec
import fr.hozakan.flysightble.framework.extension.speedInUnit
import fr.hozakan.flysightble.model.config.Alarm
import fr.hozakan.flysightble.model.config.AlarmType
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.InitMode
import fr.hozakan.flysightble.model.config.RateMode
import fr.hozakan.flysightble.model.config.SilenceWindow
import fr.hozakan.flysightble.model.config.Speech
import fr.hozakan.flysightble.model.config.SpeechMode
import fr.hozakan.flysightble.model.config.ToneLimitBehaviour
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.config.UnitSystem
import fr.hozakan.flysightble.model.config.Volume
import fr.hozakan.flysightble.model.defaultConfigFile

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
            contentDescription = "Unit system picker"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(text = UnitSystem.Metric.unitName) },
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
            text = { Text(text = UnitSystem.Imperial.unitName) },
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

    LaunchedEffect(state.fileSaved) {
        if (state.fileSaved) {
            onNavigateUp()
        }
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
                Text("Config file not found")
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
                        Text(text = "Config name")
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
                        Text(text = "Description")
                    }
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = configFile.kind,
                    onValueChange = {
                        updateConfigFileKind(it)
                    },
                    label = {
                        Text(text = "Kind")
                    }
                )
            }
            item {
                ExpandableColumn(
                    headerComposable = {
                        Text("General")
                    }
                ) {
//                    UnitSystemContainer(
//                        unitSystem = configFile.unitSystem,
//                        onSelectionChanged = {
//                            updateUnitSystem(it)
//                        }
//                    )
//                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    DynamicModelContainer(
                        dynamicModel = configFile.dynamicModel,
                        onSelectionChanged = {
                            updateDynamicModel(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Sample period (ms)",
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
                        Text("Adjust speeds to sea level")
                    }
                }
            }
            item {
                ExpandableColumn(
                    headerComposable = {
                        Text("Tone")
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
                    headerComposable = {
                        Text("Rate")
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
                        label = "Minimum rate (tone/s)",
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
                        label = "Maximum rate (tone/s)",
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
                        Text("Flat line at minimum rate")
                    }
                }
                ExpandableColumn(
                    headerComposable = {
                        Text("Speech")
                    }
                ) {
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Period (s)",
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
                            Text("Add speech")
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
                        Text("Thresholds")
                    }
                ) {
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Vertical speed (${unitSystem.speedText})",
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
                        label = "Horizontal speed (${unitSystem.speedText})",
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
                        Text("Initialization")
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
                            Text(text = "Filename")
                        }
                    )
                }
                ExpandableColumn(
                    headerComposable = {
                        Text("Alarms")
                    }
                ) {
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Window above (${unitSystem.distanceText})",
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
                        label = "Window below (${unitSystem.distanceText})",
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
                        label = "Ground elevation (${unitSystem.distanceText})",
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
                            Text("Add alarm")
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
                        Text("Altitude announcements")
                    }
                ) {
                    DistanceUnitContainer(
                        label = "Units",
                        unitSystem = configFile.altitudeUnit,
                        onSelectionChanged = {
                            updateAltitudeUnit(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Step",
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
                        Text("Silence")
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
                            Text("Add Silence window")
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
                    onDismissRequest = onNavigateUp,
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
                            label = "Units",
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
                            label = "Units",
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
                    onDismissRequest = onDismiss,
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
                    label = "Elevation (${unitSystem.distanceText})",
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
                            Text(text = "Filename")
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                }
                SimpleDialogActionBar(
                    onDismissRequest = onDismiss,
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
                    label = "Window top (${unitSystem.distanceText})",
                    intValue = windowTop.distanceInUnit(unitSystem),
                    onValueChanged = {
                        windowTop = it?.fromDistanceUnitToMeter(unitSystem) ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                EmptyIntTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Window bottom (${unitSystem.distanceText})",
                    intValue = windowBottom.distanceInUnit(unitSystem),
                    onValueChanged = {
                        windowBottom = it?.fromDistanceUnitToMeter(unitSystem) ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                SimpleDialogActionBar(
                    onDismissRequest = onDismiss,
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
    DropdownContainer(
        modifier = modifier,
        label = "Init mode",
        selectedValue = initMode.text,
        options = remember { InitMode.entries.map { it.text } },
        onSelectionChanged = { newInitMode ->
            InitMode.fromText(newInitMode)?.let {
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
                Text("Speech $index")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDeleteClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete speech"
                    )
                }
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(speech.mode.text)
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
    onDeleteClicked: ()  -> Unit
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
                Text("Alarm $index")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDeleteClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete alarm"
                    )
                }
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(alarm.alarmType.text)
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                "Alarm elevation (${unitSystem.distanceText}) : ${
                    alarm.alarmElevation.distanceInUnit(
                        unitSystem
                    )
                }"
            )
            if (alarm.alarmType == AlarmType.PlayFile) {
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                Text("Filename: ${alarm.alarmFile}")
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
                Text("Silence window $index")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDeleteClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete silence"
                    )
                }
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text("Top (${unitSystem.distanceText}) : ${silenceWindow.top.distanceInUnit(unitSystem)}")
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                "Bottom (${unitSystem.distanceText}) : ${
                    silenceWindow.bottom.distanceInUnit(
                        unitSystem
                    )
                }"
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
    DropdownContainer(
        label = "Speech mode",
        selectedValue = speechMode.text,
        options = remember { SpeechMode.entries.map { it.text } },
        onSelectionChanged = { newSpeechMode ->
            SpeechMode.fromText(newSpeechMode)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun SpeedUnitContainer(
    modifier: Modifier = Modifier,
    label: String = "Speed unit",
    unitSystem: UnitSystem,
    onSelectionChanged: (UnitSystem) -> Unit
) {
    DropdownContainer(
        label = label,
        selectedValue = unitSystem.speedText,
        options = remember { UnitSystem.entries.map { it.speedText } },
        onSelectionChanged = { newUnit ->
            UnitSystem.fromSpeedText(newUnit)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Composable
internal fun DistanceUnitContainer(
    modifier: Modifier = Modifier,
    label: String = "Distance unit",
    unitSystem: UnitSystem,
    onSelectionChanged: (UnitSystem) -> Unit
) {
    DropdownContainer(
        label = label,
        selectedValue = unitSystem.distanceText,
        options = remember { UnitSystem.entries.map { it.distanceText } },
        onSelectionChanged = { newUnit ->
            UnitSystem.fromDistanceText(newUnit)?.let {
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
    DropdownContainer(
        label = "Alarm type",
        selectedValue = alarmType.text,
        options = remember { AlarmType.entries.map { it.text } },
        onSelectionChanged = { newAlarmType ->
            AlarmType.fromText(newAlarmType)?.let {
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
    DropdownContainer(
        label = "Dynamic model",
        selectedValue = dynamicModel.text,
        options = remember { DynamicModel.entries.map { it.text } },
        onSelectionChanged = { newModel ->
            DynamicModel.fromText(newModel)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

//@Composable
//internal fun UnitSystemContainer(
//    modifier: Modifier = Modifier,
//    unitSystem: UnitSystem,
//    onSelectionChanged: (UnitSystem) -> Unit
//) {
//    DropdownContainer(
//        modifier = modifier,
//        label = "Unit system",
//        selectedValue = unitSystem.text,
//        options = remember { UnitSystem.entries.map { it.text } },
//        onSelectionChanged = { newUnitSystem ->
//            UnitSystem.fromText(newUnitSystem)?.let {
//                onSelectionChanged(it)
//            }
//        }
//    )
//}

@Composable
internal fun ToneModeContainer(
    modifier: Modifier = Modifier,
    toneMode: ToneMode,
    onSelectionChanged: (ToneMode) -> Unit
) {
    DropdownContainer(
        label = "Tone mode",
        selectedValue = toneMode.text,
        options = remember { ToneMode.entries.map { it.text } },
        onSelectionChanged = { newMode ->
            ToneMode.fromText(newMode)?.let {
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
    DropdownContainer(
        label = "Limit behaviour",
        selectedValue = limitBehaviour.text,
        options = remember { ToneLimitBehaviour.entries.map { it.text } },
        onSelectionChanged = { newMode ->
            ToneLimitBehaviour.fromText(newMode)?.let {
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
        label = "Volume",
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
    DropdownContainer(
        label = "Rate mode",
        selectedValue = rateMode.text,
        options = remember { RateMode.entries.map { it.text } },
        onSelectionChanged = { newMode ->
            RateMode.fromText(newMode)?.let {
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
            hasValidFileName = true,
            fileSaved = false
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