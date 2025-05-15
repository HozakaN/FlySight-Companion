package fr.hozakan.flysightcompanion.sessionmodule.ui.profile

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
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import fr.hozakan.flysightcompanion.composablecommons.NumberInputField
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.extension.fromText
import fr.hozakan.flysightcompanion.designsystem.extension.textResource
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.session.configuration.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSourceType

@Composable
fun SessionProfileMenuActions(
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
fun SessionProfileScreen(
    configurationName: String,
    onNavigateUp: () -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: SessionProfileViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = configurationName) {
        viewModel.loadSessionConfiguration(configurationName)
    }

    if (state.fileSaved?.getContentIfNotHandled() == true) {
        onNavigateUp()
    }

    val form = rememberSessionProfileForm(state.sessionProfile)

    SessionProfileScreenInternal(
        state = state,
        form = form,
        saveConfigurationClicked = {
            form.toSessionConfiguration()?.let { config ->
                viewModel.saveSessionConfiguration(config)
            }
        },
        onNavigateUp = onNavigateUp
    )
}

@Composable
fun SessionProfileScreenInternal(
    state: SessionProfileState,
    form: SessionProfileForm = rememberSessionProfileForm(),
    saveConfigurationClicked: (SessionProfileForm) -> Unit,
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
                Text(text = stringResource(R.string.session_profile_not_found))
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
                                    form.updateSessionProfileName("")
                                }
                            },
                        value = form.name ?: "",
                        onValueChange = {
                            form.updateSessionProfileName(it)
                        },
                        label = {
                            Text(
                                text = stringResource(
                                    if (form.hasValidProfileName) {
                                        R.string.config_detail_configuration_name
                                    } else {
                                        R.string.config_detail_configuration_name_invalid
                                    }
                                )
                            )
                        },
                        isError = !form.hasValidProfileName
                    )
                }
                item {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.hasFocus && form.description == null) {
                                    form.updateProfileDescription("")
                                }
                            },
                        value = form.description ?: "",
                        onValueChange = {
                            form.updateProfileDescription(it)
                        },
                        label = {
                            Text(text = stringResource(R.string.config_detail_configuration_description))
                        }
                    )
                }
                item {
                    AssociatedConfigFileContainer(
                        configFiles = state.configFiles,
                        selectedConfigFile = form.configFile,
                        onConfigFileSelected = {
                            form.updateConfigFile(it)
                        }
                    )
                }

                // Show Map switch
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FText(
                                    text = "Show grid lines",
                                    configuration = FlySightTheme.typography.plainScreenTextLarge
                                )
                                Switch(
                                    checked = form.showGridLines,
                                    onCheckedChange = { form.updateShowGridLines(it) }
                                )
                            }

                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FText(
                                    text = "Show map in background",
                                    configuration = FlySightTheme.typography.plainScreenTextLarge
                                )
                                Switch(
                                    checked = form.showMap,
                                    onCheckedChange = { form.updateShowMap(it) }
                                )
                            }

                            Spacer(modifier = Modifier.requiredHeight(16.dp))

                            FText(
                                text = "Competition Window",
                                configuration = FlySightTheme.typography.plainScreenTextLarge
                            )

                            Spacer(modifier = Modifier.requiredHeight(8.dp))

                            // Competition Window
                            NumberInputField(
                                label = "Top (m)",
                                value = form.competitionWindowTop,
                                onValueChange = { form.updateCompetitionWindowTop(it) }
                            )

                            Spacer(modifier = Modifier.requiredHeight(8.dp))

                            NumberInputField(
                                label = "Bottom (m)",
                                value = form.competitionWindowBottom,
                                onValueChange = { form.updateCompetitionWindowBottom(it) }
                            )
                        }
                    }
                }

                // Performance Lane section
                item {
                    PerformanceLaneContainer(form, state)
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
                    onClick = { saveConfigurationClicked(form) },
                    enabled = form.isValid && form.hasValidProfileName && form.isDirty
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
private fun PerformanceLaneContainer(form: SessionProfileForm, state: SessionProfileState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FText(
                    text = "Performance Lane",
                    configuration = FlySightTheme.typography.plainScreenTextLarge
                )
                Switch(
                    checked = form.displayPerformanceLane,
                    onCheckedChange = { form.updateDisplayPerformanceLane(it) }
                )
            }

            if (form.displayPerformanceLane) {

                if (form.showMap) {
                    Spacer(modifier = Modifier.requiredHeight(16.dp))

                    // Display options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FText(text = "Show in map")
                        Switch(
                            checked = form.displayPerformanceLaneInMap,
                            onCheckedChange = { form.updateDisplayPerformanceLaneInMap(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.requiredHeight(8.dp))

                // Reference point picker
                ReferencePointSelector(
                    referencePoints = state.referencePoints,
                    selectedReferencePoint = form.referencePoint,
                    onReferencePointSelected = { form.updateReferencePoint(it) }
                )

                Spacer(modifier = Modifier.requiredHeight(16.dp))

                // Performance Lane Width
                NumberInputField(
                    label = "Performance Lane Width (m)",
                    value = form.performanceLaneWidth,
                    onValueChange = { form.updatePerformanceLaneWidth(it) }
                )

                Spacer(modifier = Modifier.requiredHeight(16.dp))

                FText(
                    text = "Exit Detection Window",
                    configuration = FlySightTheme.typography.plainScreenTextMedium
                )

                Spacer(modifier = Modifier.requiredHeight(8.dp))

                // Exit Detection Window
                NumberInputField(
                    label = "Top (m)",
                    value = form.exitDetectionWindowTop,
                    onValueChange = { form.updateExitDetectionWindowTop(it) }
                )
                FText(
                    text = "Do not detect an exit if above this altitude (negative to disable this check)",
                    configuration = FlySightTheme.typography.captionText,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                Spacer(modifier = Modifier.requiredHeight(8.dp))

                NumberInputField(
                    label = "Bottom (m)",
                    value = form.exitDetectionWindowBottom,
                    onValueChange = { form.updateExitDetectionWindowBottom(it) }
                )
                FText(
                    text = "Do not detect an exit if below this altitude (negative to disable exit detection)",
                    configuration = FlySightTheme.typography.captionText,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                Spacer(modifier = Modifier.requiredHeight(16.dp))

                FText(
                    text = "Exit Detection Parameters",
                    configuration = FlySightTheme.typography.plainScreenTextMedium
                )

                Spacer(modifier = Modifier.requiredHeight(8.dp))

                // Exit detection parameters
                NumberInputField(
                    label = "Consecutive points down (exit)",
                    value = form.exitPointsDown,
                    onValueChange = { form.updateExitPointsDown(it) }
                )
                FText(
                    text = "Consecutive points down to indicate an exit",
                    configuration = FlySightTheme.typography.captionText,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))

                NumberInputField(
                    label = "Consecutive points up (reset)",
                    value = form.exitPointsUp,
                    onValueChange = { form.updateExitPointsUp(it) }
                )
                FText(
                    text = "Consecutive points up to reset the exit altitude",
                    configuration = FlySightTheme.typography.captionText,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))

                NumberInputField(
                    label = "Down threshold (cm/s)",
                    value = form.exitDownThresh,
                    onValueChange = { form.updateExitDownThresh(it) }
                )
                FText(
                    text = "Speed (cm/s) to indicate down (positive) (initialize exit altitude)",
                    configuration = FlySightTheme.typography.captionText,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))

                NumberInputField(
                    label = "Up threshold (cm/s)",
                    value = form.exitUpThresh,
                    onValueChange = { form.updateExitUpThresh(it) }
                )
                FText(
                    text = "Speed (cm/s) to indicate up (negative) (reset exit altitude)",
                    configuration = FlySightTheme.typography.captionText,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                Spacer(modifier = Modifier.requiredHeight(16.dp))

                FText(
                    text = "Alerts",
                    configuration = FlySightTheme.typography.plainScreenTextMedium
                )

                Spacer(modifier = Modifier.requiredHeight(4.dp))

                // Alert settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FText(text = "Visual alert when exit detected")
                    Switch(
                        checked = form.showVisualAlertWhenExitDetected,
                        onCheckedChange = { form.updateShowVisualAlertWhenExitDetected(it) }
                    )
                }

                Spacer(modifier = Modifier.requiredHeight(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FText(text = "Audio alert when exit detected")
                    Switch(
                        checked = form.playAudioAlertWhenExitDetected,
                        onCheckedChange = { form.updatePlayAudioAlertWhenExitDetected(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferencePointSelector(
    referencePoints: List<ReferencePoint>,
    selectedReferencePoint: ReferencePoint?,
    onReferencePointSelected: (ReferencePoint?) -> Unit
) {
    val selectableValues = remember(referencePoints) {
        referencePoints.map { it.name }
    }

    DropdownContainer(
        label = "Reference Point",
        selectedValue = selectedReferencePoint?.name ?: "Select a reference point",
        options = selectableValues,
        onSelectionChanged = { newSelection ->
            onReferencePointSelected(referencePoints.firstOrNull { it.name == newSelection })
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AssociatedConfigFileContainer(
    modifier: Modifier = Modifier,
    configFiles: List<ConfigFile>,
    selectedConfigFile: ConfigFile?,
    onConfigFileSelected: (ConfigFile?) -> Unit
) {
    val selectableValues: List<String> = remember(configFiles) {
        configFiles.map { it.name }
    }

    DropdownContainer(
        label = "Associated config file",
        selectedValue = selectedConfigFile?.name ?: "Select a configuration",
        options = selectableValues,
        onSelectionChanged = { newSource ->
            onConfigFileSelected(configFiles.first { it.name == newSource })
        },
        modifier = modifier
    )
}

@Composable
internal fun StaticSessionSourceContainer(
    modifier: Modifier = Modifier,
    sessionSource: SessionSourceType,
    onSelectionChanged: (SessionSourceType) -> Unit
) {
    val context = LocalContext.current
    DropdownContainer(
        label = stringResource(R.string.config_detail_configuration_dynamic_model),
        selectedValue = stringResource(sessionSource.textResource),
        options = remember { SessionSourceType.entries.map { context.getString(it.textResource) } },
        onSelectionChanged = { newSource ->
            SessionSourceType.fromText(context, newSource)?.let {
                onSelectionChanged(it)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun SessionConfigScreenInternalPreview() {
    SessionProfileScreenInternal(
        state = SessionProfileState(
            sessionProfile = SessionProfile.default(),
            configFiles = emptyList(),
            configurationFound = true
        ),
        saveConfigurationClicked = {},
        onNavigateUp = {}
    )
}
