package fr.hozakan.flysightcompanion.sessionmodule.ui.config

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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.composablecommons.DropdownContainer
import fr.hozakan.flysightcompanion.composablecommons.EmptyIntTextField
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.composablecommons.speechValueForMode
import fr.hozakan.flysightcompanion.composablecommons.speechValueFromMode
import fr.hozakan.flysightcompanion.composablecommons.speechValueLabel
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.extension.distanceTextResource
import fr.hozakan.flysightcompanion.designsystem.extension.fromText
import fr.hozakan.flysightcompanion.designsystem.extension.speedTextResource
import fr.hozakan.flysightcompanion.designsystem.extension.textResource
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.extension.distanceInUnit
import fr.hozakan.flysightcompanion.framework.extension.fromDistanceUnitToMeter
import fr.hozakan.flysightcompanion.model.config.Alarm
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.config.InitMode
import fr.hozakan.flysightcompanion.model.config.SilenceWindow
import fr.hozakan.flysightcompanion.model.config.Speech
import fr.hozakan.flysightcompanion.model.config.SpeechMode
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.session.configuration.SessionConfiguration
import fr.hozakan.flysightcompanion.model.session.configuration.StaticSessionSource

@Composable
fun SessionConfigMenuActions(
//    onCreateConfigFile: () -> Unit
) {
    IconButton(
        onClick = {} //onCreateConfigFile
    ) {
//        Icon(
//            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
//            contentDescription = stringResource(R.string.list_config_file_menu_action_new_config_file_content_description)
//        )
    }
}

@Composable
fun SessionConfigScreen(
    configurationName: String,
    onNavigateUp: () -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: SessionConfigViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = configurationName) {
        viewModel.loadSessionConfiguration(configurationName)
    }

    val form = rememberSessionConfigurationForm(state.sessionConfiguration)

    SessionConfigScreenInternal(
        state = state,
        form = form,
        onSessionSourceChanged = {},
        saveConfigurationClicked = {},
        onNavigateUp = onNavigateUp
    )
}

@Composable
fun SessionConfigScreenInternal(
    state: SessionConfigState,
    form: SessionConfigurationForm = rememberSessionConfigurationForm(),
    onSessionSourceChanged: (StaticSessionSource) -> Unit,
    saveConfigurationClicked: (SessionConfigurationForm) -> Unit,
    onNavigateUp: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (!state.configurationFound) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.session_configuration_file_not_found))
            }
            return@Surface
        }

        Column {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.hasFocus && form.name == null) {
                                    form.updateSessionConfigurationName("")
                                }
                            },
                        value = form.name ?: "",
                        onValueChange = {
                            form.updateSessionConfigurationName(it)
                        },
                        label = {
                            Text(
                                text = stringResource(
                                    if (form.hasValidFileName) {
                                        R.string.config_detail_configuration_name
                                    } else {
                                        R.string.config_detail_configuration_name_invalid
                                    }
                                )
                            )
                        },
                        isError = !form.hasValidFileName
                    )
                }
                item {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.hasFocus && form.description == null) {
                                    form.updateConfigFileDescription("")
                                }
                            },
                        value = form.description ?: "",
                        onValueChange = {
                            form.updateConfigFileDescription(it)
                        },
                        label = {
                            Text(text = stringResource(R.string.config_detail_configuration_description))
                        }
                    )
                }
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(
                                PaddingValues(
                                    start = 8.dp,
                                    end = 8.dp,
                                    bottom = 8.dp
                                )
                            )
                        ) {
                            StaticSessionSourceContainer(
                                sessionSource = state.staticSessionSource,
                                onSelectionChanged = {
                                    onSessionSourceChanged(it)
                                }
                            )
                        }
                    }
                }
//                item {
//                    Card {
//                        ExpandableColumn(
//                            headerComposable = {
//                                Text(
//                                    text = stringResource(
//                                        R.string.config_detail_configuration_section_silence
//                                    )
//                                )
//                            },
//                            contentPaddingValues = PaddingValues(
//                                start = 8.dp,
//                                end = 8.dp,
//                                bottom = 8.dp
//                            )
//                        ) {
//                            form.silenceWindows.forEachIndexed { index, silenceWindow ->
//                                SilenceItemContainer(
//                                    index = index + 1,
//                                    silenceWindow = silenceWindow,
//                                    unitSystem = unitSystem,
//                                    onDeleteClicked = {
//                                        form.deleteSilenceWindow(silenceWindow)
//                                    }
//                                )
//                                if (index < form.silenceWindows.size - 1) {
//                                    Spacer(modifier = Modifier.requiredHeight(8.dp))
//                                }
//                            }
//                            Spacer(modifier = Modifier.requiredHeight(8.dp))
//                            var addSilenceClicked by remember { mutableStateOf(false) }
//                            Box(
//                                modifier = Modifier.fillMaxWidth(),
//                                contentAlignment = Alignment.TopCenter
//                            ) {
//                                Button(
//                                    onClick = {
//                                        addSilenceClicked = true
//                                    }
//                                ) {
//                                    Text(
//                                        text = stringResource(
//                                            R.string.config_detail_configuration_add_silence_window
//                                        )
//                                    )
//                                }
//                            }
//                            if (addSilenceClicked) {
//                                AddSilenceWindowDialog(
//                                    unitSystem = unitSystem,
//                                    onSilenceAdded = {
//                                        form.addSilenceWindow(it)
//                                        addSilenceClicked = false
//                                    },
//                                    onDismiss = {
//                                        addSilenceClicked = false
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
            }
            Row(
                modifier = Modifier
                    .requiredHeight(80.dp)
                    .fillMaxWidth()
                    .padding(end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                var showCancelDialog by remember { mutableStateOf(false) }

                TextButton(
                    onClick = {
                        if (form.isDirty) {
                            showCancelDialog = true
                        } else {
                            onNavigateUp()
                        }
                    }
                ) {
                    FText(
                        text = stringResource(R.string.misc_cancel),
                        configuration = FlySightTheme.typography.plainScreenTextLarge
                    )
                }
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                TextButton(
                    onClick = { saveConfigurationClicked(form) },
                    enabled = form.isValid && form.hasValidFileName && form.isDirty
                ) {
                    FText(
                        text = stringResource(R.string.misc_save),
                        configuration = FlySightTheme.typography.plainScreenTextLarge
                    )
                }

                if (showCancelDialog) {
                    Dialog(
                        onDismissRequest = {
                            showCancelDialog = false
                        }
                    ) {
                        Card {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                FText(
                                    text = stringResource(R.string.config_detail_cancel_changes),
                                    configuration = FlySightTheme.typography.plainScreenTextLarge
                                )
                                SimpleDialogActionBar(
                                    cancelButtonText = stringResource(R.string.misc_do_not_discard),
                                    validateButtonText = stringResource(R.string.misc_discard),
                                    onCancel = {
                                        showCancelDialog = false
                                    },
                                    onValidate = {
                                        showCancelDialog = false
                                        onNavigateUp()
                                    }
                                )
                            }
                        }
                    }
                }
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
        Surface {
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
        Surface {
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
        Surface {
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
internal fun StaticSessionSourceContainer(
    modifier: Modifier = Modifier,
    sessionSource: StaticSessionSource,
    onSelectionChanged: (StaticSessionSource) -> Unit
) {
    val context = LocalContext.current
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_dynamic_model),
        selectedValue = stringResource(sessionSource.textResource),
        options = remember { StaticSessionSource.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newSource ->
            StaticSessionSource.fromText(context, newSource)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun SessionConfigScreenInternalPreview() {
    SessionConfigScreenInternal(
        state = SessionConfigState(
            sessionConfiguration = SessionConfiguration.default(),
            configurationFound = true,
            staticSessionSource = StaticSessionSource.Local
        ),
        onSessionSourceChanged = {},
        saveConfigurationClicked = {},
        onNavigateUp = {}
    )
}