package fr.hozakan.flysightble.configfilesmodule.ui.config_detail

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.composablecommons.DropdownContainer
import fr.hozakan.flysightble.composablecommons.EmptyIntTextField
import fr.hozakan.flysightble.composablecommons.ExpandableColumn
import fr.hozakan.flysightble.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightble.composablecommons.distanceUnit
import fr.hozakan.flysightble.composablecommons.hourSpeed
import fr.hozakan.flysightble.composablecommons.rateMaximumLabel
import fr.hozakan.flysightble.composablecommons.rateMinimumLabel
import fr.hozakan.flysightble.composablecommons.toneMaximumLabel
import fr.hozakan.flysightble.composablecommons.toneMinimumLabel
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.model.config.Alarm
import fr.hozakan.flysightble.model.config.AlarmType
import fr.hozakan.flysightble.model.config.DynamicModel
import fr.hozakan.flysightble.model.config.InitMode
import fr.hozakan.flysightble.model.config.RateMode
import fr.hozakan.flysightble.model.config.SilenceWindow
import fr.hozakan.flysightble.model.config.Speech
import fr.hozakan.flysightble.model.config.SpeechMode
import fr.hozakan.flysightble.model.config.SpeechUnit
import fr.hozakan.flysightble.model.config.ToneLimitBehaviour
import fr.hozakan.flysightble.model.config.ToneMode
import fr.hozakan.flysightble.model.config.UnitSystem
import fr.hozakan.flysightble.model.config.Volume

@Composable
fun ConfigDetailScreen(
    configFileName: String,
    onNavigateUp: () -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: ConfigDetailViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = configFileName) {
        viewModel.loadConfigFile(configFileName)
    }

    LaunchedEffect(state.fileSaved) {
        if (state.fileSaved) {
            onNavigateUp()
        }
    }

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
                        viewModel.updateConfigFileName(it)
                    },
                    label = {
                        Text(text = "Config file name")
                    },
                    isError = !state.hasValidFileName
                )
            }
            item {
                ExpandableColumn(
                    headerComposable = {
                        Text("General")
                    }
                ) {
                    UnitSystemContainer(
                        unitSystem = configFile.unitSystem,
                        onSelectionChanged = {
                            viewModel.updateUnitSystem(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    DynamicModelContainer(
                        dynamicModel = configFile.dynamicModel,
                        onSelectionChanged = {
                            viewModel.updateDynamicModel(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Sample period (ms)",
                        intValue = configFile.samplePeriod,
                        onValueChanged = {
                            viewModel.updateSamplePeriod(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = configFile.useSAS,
                            onCheckedChange = {
                                viewModel.updateUseSAS(it)
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
                            viewModel.updateToneMode(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = toneMinimumLabel(configFile.toneMode, configFile.unitSystem),
                        intValue = configFile.toneMinimum,
                        onValueChanged = {
                            viewModel.updateToneMinimum(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = toneMaximumLabel(configFile.toneMode, configFile.unitSystem),
                        intValue = configFile.toneMaximum,
                        onValueChanged = {
                            viewModel.updateToneMaximum(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    LimitBehaviourContainer(
                        limitBehaviour = configFile.toneLimitBehaviour,
                        onSelectionChanged = {
                            viewModel.updateToneLimitBehaviour(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    VolumeContainer(
                        volume = configFile.toneVolume,
                        onSelectionChanged = {
                            viewModel.updateToneVolume(it)
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
                            viewModel.updateRateMode(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = rateMinimumLabel(configFile.rateMode, configFile.unitSystem),
                        intValue = configFile.rateMinimumValue,
                        onValueChanged = {
                            viewModel.updateRateMinimumValue(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = rateMaximumLabel(configFile.rateMode, configFile.unitSystem),
                        intValue = configFile.rateMaximumValue,
                        onValueChanged = {
                            viewModel.updateRateMaximumValue(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Minimum rate (tone/s)",
                        intValue = configFile.rateMinimumValue,
                        onValueChanged = {
                            viewModel.updateRateMinimumValue(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Maximum rate (tone/s)",
                        intValue = configFile.rateMaximumValue,
                        onValueChanged = {
                            viewModel.updateRateMaximumValue(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = configFile.flatLineAtMinimumRate,
                            onCheckedChange = {
                                viewModel.updateFlatLineAtMinimumRate(it)
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
                            viewModel.updateSpeechRate(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    VolumeContainer(
                        volume = configFile.speechVolume,
                        onSelectionChanged = {
                            viewModel.updateSpeechVolume(it)
                        }
                    )
                    if (configFile.speeches.isNotEmpty()) {
                        Spacer(modifier = Modifier.requiredHeight(8.dp))
                    }
                    configFile.speeches.forEachIndexed { index, speech ->
                        SpeechItemContainer(
                            index = index + 1,
                            speech = speech
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
                                viewModel.addSpeech(it)
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
                        label = "Vertical speed (${hourSpeed(configFile.unitSystem)})",
                        intValue = configFile.verticalThreshold,
                        onValueChanged = {
                            viewModel.updateVerticalThreshold(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Horizontal speed (${hourSpeed(configFile.unitSystem)})",
                        intValue = configFile.horizontalThreshold,
                        onValueChanged = {
                            viewModel.updateHorizontalThreshold(it)
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
                            viewModel.updateInitMode(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    OutlinedTextField(
                        value = configFile.initFile ?: "",
                        onValueChange = {
                            viewModel.updateInitFile(it)
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
                        label = "Window above (${distanceUnit(configFile.unitSystem)})",
                        intValue = configFile.windowAbove,
                        onValueChanged = {
                            viewModel.updateWindowAbove(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Window below (${distanceUnit(configFile.unitSystem)})",
                        intValue = configFile.windowBelow,
                        onValueChanged = {
                            viewModel.updateWindowBelow(it)
                        }
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Ground elevation (${distanceUnit(configFile.unitSystem)})",
                        intValue = configFile.dzElev,
                        onValueChanged = {
                            viewModel.updateDzElev(it)
                        }
                    )
                    if (configFile.alarms.isNotEmpty()) {
                        Spacer(modifier = Modifier.requiredHeight(8.dp))
                    }
                    configFile.alarms.forEachIndexed { index, alarm ->
                        AlarmItemContainer(
                            index = index + 1,
                            alarm = alarm,
                            unitSystem = configFile.unitSystem
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
                            unitSystem = configFile.unitSystem,
                            onAlarmAdded = {
                                viewModel.addAlarm(it)
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
                    var enableAnnouncements by remember(configFile.altitudeStep) { mutableStateOf(configFile.altitudeStep > 0) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = enableAnnouncements,
                            onCheckedChange = {
                                enableAnnouncements = it
                            },
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        Text("Enable altitude announcements")
                    }
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    EmptyIntTextField(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enableAnnouncements,
                        label = "Step",
                        intValue = configFile.altitudeStep,
                        onValueChanged = {
                            viewModel.updateAltitudeStep(it)
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
                            unitSystem = configFile.unitSystem
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
                            unitSystem = configFile.unitSystem,
                            onSilenceAdded = {
                                viewModel.addSilenceWindow(it)
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
                        viewModel.saveConfigFile()
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
        var speechUnit: SpeechUnit by remember { mutableStateOf(SpeechUnit.Metric) }
        var speechValue by remember { mutableIntStateOf(0) }
        Card {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                SpeechModeContainer(
                    speechMode = speechMode,
                    onSelectionChanged = {
                        speechMode = it
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                SpeechUnitContainer(
                    speechUnit = speechUnit,
                    onSelectionChanged = {
                        speechUnit = it
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                EmptyIntTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = speechValueLabel(speechMode),
                    intValue = speechValue,
                    onValueChanged = {
                        speechValue = it ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                SimpleDialogActionBar(
                    onDismissRequest = onDismiss,
                    onValidate = {
                        onSpeechAdded(
                            Speech(
                                mode = speechMode,
                                unit = speechUnit,
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
                modifier = Modifier.padding(8.dp)
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
                    label = "Elevation (${distanceUnit(unitSystem)})",
                    intValue = alarmElevation,
                    onValueChanged = {
                        alarmElevation = it ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                if (alarmType == AlarmType.PlayFile) {
                    OutlinedTextField(
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
                modifier = Modifier.padding(8.dp)
            ) {
                EmptyIntTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Window top (${distanceUnit(unitSystem)})",
                    intValue = windowTop,
                    onValueChanged = {
                        windowTop = it ?: 0
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                EmptyIntTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Window bottom (${distanceUnit(unitSystem)})",
                    intValue = windowBottom,
                    onValueChanged = {
                        windowBottom = it ?: 0
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

private fun speechValueLabel(speechMode: SpeechMode): String = when (speechMode) {
    SpeechMode.AltitudeAboveDropzone -> {
        "Step"
    }

    else -> {
        "Decimals"
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
    speech: Speech
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Speech $index")
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(speech.mode.text)
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text("${speechValueLabel(speech.mode)}: ${speech.value}")
        }
    }
}

@Composable
fun AlarmItemContainer(
    index: Int,
    alarm: Alarm,
    unitSystem: UnitSystem
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Alarm $index")
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(alarm.alarmType.text)
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text("Alarm elevation (${distanceUnit(unitSystem)}) : ${alarm.alarmElevation}")
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
    unitSystem: UnitSystem
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Silence window $index")
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text("Top (${distanceUnit(unitSystem)}) : ${silenceWindow.top}")
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text("Bottom (${distanceUnit(unitSystem)}) : ${silenceWindow.bottom}")
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
internal fun SpeechUnitContainer(
    modifier: Modifier = Modifier,
    speechUnit: SpeechUnit,
    onSelectionChanged: (SpeechUnit) -> Unit
) {
    DropdownContainer(
        label = "Speech unit",
        selectedValue = speechUnit.text,
        options = remember { SpeechUnit.entries.map { it.text } },
        onSelectionChanged = { newSpeechUnit ->
            SpeechUnit.fromText(newSpeechUnit)?.let {
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

@Composable
internal fun UnitSystemContainer(
    modifier: Modifier = Modifier,
    unitSystem: UnitSystem,
    onSelectionChanged: (UnitSystem) -> Unit
) {
    DropdownContainer(
        modifier = modifier,
        label = "Unit system",
        selectedValue = unitSystem.text,
        options = remember { UnitSystem.entries.map { it.text } },
        onSelectionChanged = { newUnitSystem ->
            UnitSystem.fromText(newUnitSystem)?.let {
                onSelectionChanged(it)
            }
        }
    )
}

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