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
import fr.hozakan.flysightcompanion.composablecommons.ExpandableColumn
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.composablecommons.rateMaximumLabel
import fr.hozakan.flysightcompanion.composablecommons.rateMinimumLabel
import fr.hozakan.flysightcompanion.composablecommons.speechValueForMode
import fr.hozakan.flysightcompanion.composablecommons.speechValueFromMode
import fr.hozakan.flysightcompanion.composablecommons.speechValueLabel
import fr.hozakan.flysightcompanion.composablecommons.toneMaximumLabel
import fr.hozakan.flysightcompanion.composablecommons.toneMinimumLabel
import fr.hozakan.flysightcompanion.composablecommons.displayableRateValueFromRealOne
import fr.hozakan.flysightcompanion.composablecommons.valueForToneMode
import fr.hozakan.flysightcompanion.composablecommons.realRateValueFromDisplayableOne
import fr.hozakan.flysightcompanion.composablecommons.valueFromToneMode
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.extension.distanceTextResource
import fr.hozakan.flysightcompanion.designsystem.extension.fromText
import fr.hozakan.flysightcompanion.designsystem.extension.speedTextResource
import fr.hozakan.flysightcompanion.designsystem.extension.textResource
import fr.hozakan.flysightcompanion.designsystem.extension.unitNameResource
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
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

    val form = rememberConfigDetailForm(state.editedConfiguration)

    ConfigDetailScreenInternal(
        state = state,
        form = form,
        saveConfigFileClicked = {
            form.toConfigFile()?.let { config ->
                viewModel.saveConfigFile(config)
            }
        },
        onNavigateUp = onNavigateUp
    )

}

@Composable
fun ConfigDetailScreenInternal(
    state: ConfigDetailState,
    form: ConfigDetailForm = rememberConfigDetailForm(),
    saveConfigFileClicked: () -> Unit,
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
//        val configFile = state.configFile
        val unitSystem = state.unitSystem

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
                                    form.updateConfigFileName("")
                                }
                            },
                        value = form.name ?: "",
                        onValueChange = {
                            form.updateConfigFileName(it)
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
                    Column {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (!focusState.hasFocus && form.group == null) {
                                        form.updateConfigFileGroup("")
                                    }
                                },
                            value = form.group ?: "",
                            onValueChange = {
                                form.updateConfigFileGroup(it)
                            },
                            label = {
                                Text(text = stringResource(R.string.config_detail_configuration_group))
                            }
                        )
                        Spacer(modifier = Modifier.requiredHeight(8.dp))
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            headerComposable = {
                                Text(text = stringResource(R.string.config_detail_configuration_section_general))
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            DynamicModelContainer(
                                dynamicModel = form.dynamicModel,
                                onSelectionChanged = {
                                    form.updateDynamicModel(it)
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.samplePeriod == null) {
                                            form.updateSamplePeriodToDefaultValue()
                                        }
                                    },
                                label = stringResource(R.string.config_detail_configuration_section_general_sample_period),
                                intValue = form.samplePeriod,
                                onValueChanged = {
                                    form.updateSamplePeriod(it)
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = form.useSAS,
                                    onCheckedChange = {
                                        form.updateUseSAS(it)
                                    },
                                )
                                Spacer(modifier = Modifier.requiredWidth(8.dp))
                                Text(text = stringResource(R.string.config_detail_configuration_section_general_use_sas))
                            }
                        }
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            headerComposable = {
                                Text(text = stringResource(R.string.config_detail_configuration_section_tone))
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            ToneModeContainer(
                                toneMode = form.toneMode,
                                onSelectionChanged = {
                                    form.updateToneMode(it)
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.toneMinimum == null) {
                                            form.updateToneMinimumToDefaultValue()
                                        }
                                    },
                                label = toneMinimumLabel(form.toneMode, unitSystem),
                                intValue = form.toneMinimum?.valueForToneMode(
                                    form.toneMode,
                                    unitSystem
                                ),
                                onValueChanged = {
                                    form.updateToneMinimum(
                                        it?.valueFromToneMode(
                                            form.toneMode,
                                            unitSystem
                                        )
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.toneMaximum == null) {
                                            form.updateToneMaximumToDefaultValue()
                                        }
                                    },
                                label = toneMaximumLabel(form.toneMode, unitSystem),
                                intValue = form.toneMaximum?.valueForToneMode(
                                    form.toneMode,
                                    unitSystem
                                ),
                                onValueChanged = {
                                    form.updateToneMaximum(
                                        it?.valueFromToneMode(
                                            form.toneMode,
                                            unitSystem
                                        )
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            LimitBehaviourContainer(
                                limitBehaviour = form.toneLimitBehaviour,
                                onSelectionChanged = {
                                    form.updateToneLimitBehaviour(it)
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            VolumeContainer(
                                volume = form.toneVolume,
                                onSelectionChanged = {
                                    form.updateToneVolume(it)
                                }
                            )
                        }
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            expanded = false,
                            headerComposable = {
                                Text(text = stringResource(R.string.config_detail_configuration_section_rate))
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            RateModeContainer(
                                rateMode = form.rateMode,
                                onSelectionChanged = {
                                    form.updateRateMode(it)
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.rateMinimumValue == null) {
                                            form.updateRateMinimumValueToDefaultValue()
                                        }
                                    },
                                label = rateMinimumLabel(form.rateMode, unitSystem),
                                intValue = form.rateMinimumValue?.displayableRateValueFromRealOne(
                                    form.rateMode,
                                    unitSystem
                                ),
                                onValueChanged = {
                                    form.updateRateMinimumValue(
                                        it?.realRateValueFromDisplayableOne(
                                            form.rateMode,
                                            unitSystem
                                        )
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.rateMaximumValue == null) {
                                            form.updateRateMaximumValueToDefaultValue()
                                        }
                                    },
                                label = rateMaximumLabel(form.rateMode, unitSystem),
                                intValue = form.rateMaximumValue?.displayableRateValueFromRealOne(
                                    form.rateMode,
                                    unitSystem
                                ),
                                onValueChanged = {
                                    form.updateRateMaximumValue(
                                        it?.realRateValueFromDisplayableOne(
                                            form.rateMode,
                                            unitSystem
                                        )
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.rateMinimum == null) {
                                            form.updateRateMinimumToDefaultValue()
                                        }
                                    },
                                label = stringResource(
                                    R.string.config_detail_configuration_minimum_rate
                                ),
                                intValue = form.rateMinimum?.div(100),
                                onValueChanged = {
                                    form.updateRateMinimum(it?.times(100))
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.rateMaximum == null) {
                                            form.updateRateMaximumToDefaultValue()
                                        }
                                    },
                                label = stringResource(
                                    R.string.config_detail_configuration_maximum_rate
                                ),
                                intValue = form.rateMaximum?.div(100),
                                onValueChanged = {
                                    form.updateRateMaximum(it?.times(100))
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = form.flatLineAtMinimumRate,
                                    onCheckedChange = {
                                        form.updateFlatLineAtMinimumRate(it)
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
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            headerComposable = {
                                Text(
                                    text = stringResource(
                                        R.string.config_detail_configuration_section_speech
                                    )
                                )
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.speechRate == null) {
                                            form.updateSpeechRateToDefaultValue()
                                        }
                                    },
                                label = stringResource(R.string.config_detail_configuration_period),
                                intValue = form.speechRate,
                                onValueChanged = {
                                    form.updateSpeechRate(it)
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            VolumeContainer(
                                volume = form.speechVolume,
                                onSelectionChanged = {
                                    form.updateSpeechVolume(it)
                                }
                            )
                            if (form.speeches.isNotEmpty()) {
                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                            }
                            form.speeches.forEachIndexed { index, speech ->
                                SpeechItemContainer(
                                    index = index + 1,
                                    speech = speech,
                                    onDeleteClicked = {
                                        form.deleteSpeech(speech)
                                    }
                                )
                                if (index < form.speeches.size - 1) {
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
                                        form.addSpeech(it)
                                        addSpeechClicked = false
                                    },
                                    onDismiss = {
                                        addSpeechClicked = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            headerComposable = {
                                Text(
                                    text = stringResource(
                                        R.string.config_detail_configuration_section_thresholds
                                    )
                                )
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.verticalThreshold == null) {
                                            form.updateVerticalThresholdToDefaultValue()
                                        }
                                    },
                                label = stringResource(
                                    R.string.config_detail_configuration_vertical_speed,
                                    stringResource(unitSystem.speedTextResource)
                                ),
                                intValue = form.verticalThreshold?.speedInUnit(unitSystem),
                                onValueChanged = {
                                    form.updateVerticalThreshold(
                                        it?.fromSpeedUnitToCmPerSec(
                                            unitSystem
                                        )
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.horizontalThreshold == null) {
                                            form.updateHorizontalThresholdToDefaultValue()
                                        }
                                    },
                                label = stringResource(
                                    R.string.config_detail_configuration_horizontal_speed,
                                    stringResource(unitSystem.speedTextResource)
                                ),
                                intValue = form.horizontalThreshold?.speedInUnit(unitSystem),
                                onValueChanged = {
                                    form.updateHorizontalThreshold(
                                        it?.fromSpeedUnitToCmPerSec(
                                            unitSystem
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            headerComposable = {
                                Text(
                                    text = stringResource(
                                        R.string.config_detail_configuration_section_initialization
                                    )
                                )
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            InitModeContainer(
                                initMode = form.initMode,
                                onSelectionChanged = {
                                    form.updateInitMode(it)
                                }
                            )
                            if (form.initMode == InitMode.PlayFile) {
                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth()
                                        .onFocusChanged { focusState ->
                                            if (!focusState.hasFocus && form.initFile == null) {
                                                form.updateInitFile("")
                                            }
                                        },
                                    value = form.initFile ?: "",
                                    onValueChange = {
                                        form.updateInitFile(it)
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
                        }
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            headerComposable = {
                                Text(
                                    text = stringResource(
                                        R.string.config_detail_configuration_section_alarms
                                    )
                                )
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.windowAbove == null) {
                                            form.updateWindowAbove(0)
                                        }
                                    },
                                label = stringResource(
                                    R.string.config_detail_configuration_window_above,
                                    stringResource(unitSystem.distanceTextResource)
                                ),
                                intValue = form.windowAbove?.distanceInUnit(unitSystem),
                                onValueChanged = {
                                    form.updateWindowAbove(it?.fromDistanceUnitToMeter(unitSystem))
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.windowBelow == null) {
                                            form.updateWindowBelow(0)
                                        }
                                    },
                                label = stringResource(
                                    R.string.config_detail_configuration_window_below,
                                    stringResource(unitSystem.distanceTextResource)
                                ),
                                intValue = form.windowBelow?.distanceInUnit(unitSystem),
                                onValueChanged = {
                                    form.updateWindowBelow(it?.fromDistanceUnitToMeter(unitSystem))
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.dzElev == null) {
                                            form.updateDzElev(0)
                                        }
                                    },
                                label = stringResource(
                                    R.string.config_detail_configuration_ground_elevation,
                                    stringResource(unitSystem.distanceTextResource)
                                ),
                                intValue = form.dzElev?.distanceInUnit(unitSystem),
                                onValueChanged = {
                                    form.updateDzElev(it?.fromDistanceUnitToMeter(unitSystem))
                                }
                            )
                            if (form.alarms.isNotEmpty()) {
                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                            }
                            val sortedAlarms =
                                remember(form.alarms) { form.alarms.sortedByDescending { it.alarmElevation } }
                            sortedAlarms.forEachIndexed { index, alarm ->
                                AlarmItemContainer(
                                    index = index + 1,
                                    alarm = alarm,
                                    unitSystem = unitSystem,
                                    onDeleteClicked = {
                                        form.deleteAlarm(alarm)
                                    }
                                )
                                if (index < form.alarms.size - 1) {
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
                                        form.addAlarm(it)
                                        addAlarmClicked = false
                                    },
                                    onDismiss = {
                                        addAlarmClicked = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            headerComposable = {
                                Text(
                                    text = stringResource(
                                        R.string.config_detail_configuration_section_altitude
                                    )
                                )
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            DistanceUnitContainer(
                                label = stringResource(R.string.config_detail_configuration_units),
                                unitSystem = form.altitudeUnit,
                                onSelectionChanged = {
                                    form.updateAltitudeUnit(it)
                                }
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            EmptyIntTextField(
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        if (!focusState.hasFocus && form.altitudeStep == null) {
                                            form.updateAltitudeStep(0)
                                        }
                                    },
                                label = stringResource(R.string.config_detail_configuration_step),
                                intValue = form.altitudeStep?.distanceInUnit(form.altitudeUnit),
                                onValueChanged = {
                                    form.updateAltitudeStep(it?.fromDistanceUnitToMeter(form.altitudeUnit))
                                }
                            )
                        }
                    }
                }
                item {
                    Card {
                        ExpandableColumn(
                            headerComposable = {
                                Text(
                                    text = stringResource(
                                        R.string.config_detail_configuration_section_silence
                                    )
                                )
                            },
                            contentPaddingValues = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        ) {
                            form.silenceWindows.forEachIndexed { index, silenceWindow ->
                                SilenceItemContainer(
                                    index = index + 1,
                                    silenceWindow = silenceWindow,
                                    unitSystem = unitSystem,
                                    onDeleteClicked = {
                                        form.deleteSilenceWindow(silenceWindow)
                                    }
                                )
                                if (index < form.silenceWindows.size - 1) {
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
                                        form.addSilenceWindow(it)
                                        addSilenceClicked = false
                                    },
                                    onDismiss = {
                                        addSilenceClicked = false
                                    }
                                )
                            }
                        }
                    }
                }
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
                    onClick = saveConfigFileClicked,
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
            editedConfiguration = defaultConfigFile(),
            unitSystem = UnitSystem.Metric,
            configFileFound = true
        ),
        saveConfigFileClicked = {},
    ) { }
}