package fr.hozakan.flysightcompanion.sessionmodule.ui.prepare_session

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.composablecommons.DropdownContainer
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.extension.fromText
import fr.hozakan.flysightcompanion.designsystem.extension.textResource
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.theme.TextConfiguration
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.locationmodule.LocationAvailabilityState
import fr.hozakan.flysightcompanion.model.session.FlyBlindConfiguration
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.model.session.profile.SessionSource
import fr.hozakan.flysightcompanion.model.session.profile.SessionSourceType
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.sessionmodule.ui.player.ReferencePointSelector
import fr.hozakan.flysightcompanion.sessionmodule.ui.profile.FlyBlindProfileForm
import fr.hozakan.flysightcompanion.sessionmodule.ui.profile.rememberFlyBlindProfileForm

@Composable
fun PrepareSessionMenuActions(
    onManageReferencePointsClicked: () -> Unit
) {
    IconButton(
        onClick = onManageReferencePointsClicked
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_globe_location_pin_24),
            contentDescription = "Manage reference points"
        )
    }
}

@Composable
fun PrepareSessionScreen(
    onCreateConfigurationClicked: () -> Unit,
    onEditConfigurationClicked: (SessionProfile) -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: PrepareSessionViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    PrepareSessionScreenInternal(
        state = state,
        onSessionProfileSelected = { viewModel.onSessionProfileSelected(it) },
        onCreateProfileClicked = onCreateConfigurationClicked,
        onEditProfileClicked = onEditConfigurationClicked,
        onDuplicateProfileClicked = { viewModel.duplicateSessionProfile(it) },
        onDeleteProfileClicked = { viewModel.deleteSessionProfile(it) },
        onNextClicked = { viewModel.onNextClicked() },
        onPrevClicked = { viewModel.onPrevClicked() },
        onSourceTypeSelected = {
            viewModel.onSourceTypeSelected(it)
        },
        onSourceSelected = {
            viewModel.onSourceSelected(it)
        },
        onRequestLocationPermission = { viewModel.requestLocationPermission() },
        onCheckLocationSettings = { viewModel.checkLocationSettings() },
        onSessionTypeSelected = { viewModel.onSessionTypeSelected(it) },
        onFlyBlindFormFilled = { viewModel.onFlyBlindConfigurationChanged(it) }
    )
}

@Composable
fun PrepareSessionScreenInternal(
    state: PrepareSessionState,
    onSessionProfileSelected: (SessionProfile) -> Unit,
    onCreateProfileClicked: () -> Unit,
    onEditProfileClicked: (SessionProfile) -> Unit,
    onDuplicateProfileClicked: (SessionProfile) -> Unit,
    onDeleteProfileClicked: (SessionProfile) -> Unit,
    onSourceTypeSelected: (SessionSourceType) -> Unit,
    onSourceSelected: (SessionSource) -> Unit,
    onPrevClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onCheckLocationSettings: () -> Unit,
    onSessionTypeSelected: (SessionType) -> Unit,
    onFlyBlindFormFilled: (FlyBlindConfiguration) -> Unit
) {

    val phase = state.prepareSessionPhase

    val sessionProfiles = state.sessionProfiles

    val flyBlindForm =
        rememberFlyBlindProfileForm(initialConfiguration = state.flyBlindConfiguration)

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        when (phase) {
            PrepareSessionPhase.SelectSessionType -> SelectSessionTypeScreen(
                availableTypes = state.availableSessionTypes,
                selectedSessionType = state.selectedSessionType,
                onSessionTypeSelected = { sessionType ->
                    onSessionTypeSelected(sessionType)
                },
                onNextClicked = onNextClicked
            )

            PrepareSessionPhase.SelectProfile -> when (state.selectedSessionType) {
                SessionType.Hud -> HudProfileSelection(
                    sessionProfiles = sessionProfiles,
                    selectedProfile = state.selectedProfile,
                    onSessionProfileSelected = onSessionProfileSelected,
                    onPrevClicked = onPrevClicked,
                    onNextClicked = onNextClicked,
                    onCreateProfileClicked = onCreateProfileClicked,
                    onEditProfileClicked = onEditProfileClicked,
                    onDuplicateProfileClicked = onDuplicateProfileClicked,
                    onDeleteProfileClicked = onDeleteProfileClicked
                )

                SessionType.PlaneDisplay -> {}
                SessionType.FlyBlind -> FlyBlindProfileSelection(
                    form = flyBlindForm,
                    availableReferencePoints = state.referencePoints,
                    onNextClicked = {
                        flyBlindForm.toFlyBlindConfiguration()?.let { config ->
                            onFlyBlindFormFilled(config)
                            onNextClicked()
                        }
                    },
                    onPrevClicked = onPrevClicked,
                )

                SessionType.SpaceInvaders -> {}
                SessionType.FlyToDraw -> {}
            }

            PrepareSessionPhase.SelectSource -> SelectSourceScreen(
                selectedSessionType = state.selectedSessionType,
                availableSources = state.availableSources,
                selectedSourceType = state.selectedSourceType,
                selectedSource = state.selectedSource,
                locationAvailabilityState = state.locationAvailabilityState,
                onSourceTypeSelected = onSourceTypeSelected,
                onSourceSelected = onSourceSelected,
                onPrevClicked = onPrevClicked,
                onNextClicked = onNextClicked,
                onRequestLocationPermission = onRequestLocationPermission,
                onCheckLocationSettings = onCheckLocationSettings
            )
        }
    }
}

@Composable
fun FlyBlindProfileSelection(
    form: FlyBlindProfileForm,
    availableReferencePoints: List<ReferencePoint>,
    onNextClicked: () -> Unit,
    onPrevClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        // Reference Point Selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                ReferencePointSelector(
                    referencePoints = availableReferencePoints,
                    selectedReferencePoint = form.referencePoint,
                    onReferencePointSelected = { form.updateReferencePoint(it) }
                )

            }
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))

        // DZ Elevation
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                OutlinedTextField(
                    value = form.dzElev,
                    onValueChange = { newValue ->
                        // Only allow numeric input
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            form.updateDzElev(newValue)
                        }
                    },
                    label = {
                        FText(
                            text = "Dropzone elevation (m)",
                            configuration = TextConfiguration.Default
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.requiredHeight(16.dp))

        // Belly Flying Toggle
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FText(
                            text = "Flying Position",
                            configuration = FlySightTheme.typography.cardTitle
                        )
                    }

                    Switch(
                        checked = form.isBellyFlying,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SwitchDefaults.colors().uncheckedThumbColor,
                        ),
                        onCheckedChange = { form.updateIsBellyFlying(it) },
                        thumbContent = {
                            Icon(
                                imageVector = if (form.isBellyFlying)
                                    Icons.Default.KeyboardArrowDown
                                else
                                    Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier.requiredSize(16.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.requiredWidth(8.dp))

                    FText(
                        text = if (form.isBellyFlying) "Belly" else "Back",
                        configuration = FlySightTheme.typography.plainScreenTextMedium
                    )
                }
                Spacer(modifier = Modifier.requiredHeight(4.dp))
                FText(
                    text = "Left and Right indications are inverted depending on the position",
                    configuration = FlySightTheme.typography.captionText
                )
            }
        }
        
        Spacer(modifier = Modifier.requiredHeight(16.dp))
        
        // Audio Updates Frequency
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                val audioOptions = remember {
                    listOf(
                        Pair("Disabled", -1L),
                        Pair("Every 5 seconds", 5000L),
                        Pair("Every 10 seconds", 10000L),
                        Pair("Every 15 seconds", 15000L),
                        Pair("Every 30 seconds", 30000L),
                        Pair("Every minute", 60000L)
                    )
                }
                
                val selectedOptionText = remember(form.timeBetweenAudioUpdates) {
                    audioOptions.find { it.second == form.timeBetweenAudioUpdates }?.first ?: "Disabled"
                }
                
                FText(
                    text = "Audio Updates",
                    configuration = FlySightTheme.typography.cardTitle
                )
                
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                
                FText(
                    text = "How often should audio directions be announced",
                    configuration = FlySightTheme.typography.captionText
                )
                
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                
                DropdownContainer(
                    label = "Frequency",
                    selectedValue = selectedOptionText,
                    options = audioOptions.map { it.first },
                    onSelectionChanged = { selectedText ->
                        audioOptions.find { it.first == selectedText }?.let {
                            form.updateTimeBetweenAudioUpdates(it.second)
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.requiredHeight(16.dp))
        
        // Keep Map On Exit Toggle
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FText(
                            text = "Keep Map After Exit",
                            configuration = FlySightTheme.typography.cardTitle
                        )
                        
                        Spacer(modifier = Modifier.requiredHeight(4.dp))
                        
                        FText(
                            text = "Display the map even after exit detection",
                            configuration = FlySightTheme.typography.captionText
                        )
                    }

                    Switch(
                        checked = form.keepMapOnExit,
                        onCheckedChange = { form.updateKeepMapOnExit(it) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        PrevNextBar(
            prevEnabled = true,
            onPrevClicked = onPrevClicked,
            nextEnabled = form.isValid,
            onNextClicked = onNextClicked,
            showStepCounter = true,
            currentStep = 2,
            maxStep = 3
        )
    }
}

@Composable
fun HudProfileSelection(
    sessionProfiles: LoadingState<List<SessionProfile>>,
    selectedProfile: SessionProfile?,
    onSessionProfileSelected: (SessionProfile) -> Unit,
    onPrevClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onCreateProfileClicked: () -> Unit,
    onEditProfileClicked: (SessionProfile) -> Unit,
    onDuplicateProfileClicked: (SessionProfile) -> Unit,
    onDeleteProfileClicked: (SessionProfile) -> Unit
) {
    when (sessionProfiles) {
        is LoadingState.Error -> {}
        LoadingState.Idle -> {}
        is LoadingState.Loaded -> {
            val configurations = sessionProfiles.value
            if (configurations.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FText(
                        text = "No session profile found",
                        configuration = FlySightTheme.typography.plainScreenTextLarge
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Button(
                        onClick = onCreateProfileClicked
                    ) {
                        FText(
                            text = "Create new one",
                            configuration = FlySightTheme.typography.plainScreenButtonText
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(8.dp),
                ) {
                    FText(
                        text = "Select the profile to use",
                        configuration = FlySightTheme.typography.cardTitle
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            Column {
                                Button(
                                    onClick = onCreateProfileClicked,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FText(
                                        text = "Create new profile",
                                        configuration = FlySightTheme.typography.plainScreenButtonText
                                    )
                                }
                                Spacer(modifier = Modifier.requiredHeight(8.dp))
                            }
                        }
                        items(sessionProfiles.value) { profile ->
                            SessionProfileItem(
                                profile = profile,
                                isSelected = profile.name == selectedProfile?.name,
                                onClick = { onSessionProfileSelected(profile) },
                                onEditClick = { onEditProfileClicked(profile) },
                                onDuplicateClick = { onDuplicateProfileClicked(profile) },
                                onDeleteClick = { onDeleteProfileClicked(profile) }
                            )
                        }
                    }
                    PrevNextBar(
                        prevEnabled = true,
                        onPrevClicked = onPrevClicked,
                        nextEnabled = selectedProfile != null,
                        onNextClicked = onNextClicked,
                        showStepCounter = true,
                        currentStep = 2,
                        maxStep = 3
                    )
                }
            }
        }

        is LoadingState.Loading -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                FText(
                    text = "Loading...",
                    configuration = FlySightTheme.typography.plainScreenTextLarge
                )
            }
        }
    }
}

@Composable
fun SessionProfileItem(
    profile: SessionProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var deleteDialogOpened by remember { mutableStateOf(false) }

    Card(
        border = if (isSelected) {
            BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                FText(
                    text = profile.name,
                    configuration = FlySightTheme.typography.plainScreenTextLarge
                )

                if (profile.description?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.requiredHeight(4.dp))
                    FText(
                        text = profile.description,
                        configuration = FlySightTheme.typography.captionText
                    )
                }
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = ""
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.misc_edit)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.misc_duplicate)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDuplicateClick()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.misc_delete)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            deleteDialogOpened = true
                        }
                    )
                }
            }
        }
    }

    if (deleteDialogOpened) {
        DeleteProfileDialog(
            profile = profile,
            onConfirm = {
                onDeleteClick()
                deleteDialogOpened = false
            },
            onCancel = {
                deleteDialogOpened = false
            }
        )
    }
}

@Composable
fun DeleteProfileDialog(
    profile: SessionProfile,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel
    ) {
        Card {
            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.misc_named_delete,
                        profile.name
                    )
                )
                SimpleDialogActionBar(
                    onCancel = onCancel,
                    onValidate = onConfirm,
                    validateButtonText = stringResource(R.string.misc_delete).uppercase()
                )
            }
        }
    }
}

@Composable
fun SelectSessionTypeScreen(
    availableTypes: List<SessionType>,
    selectedSessionType: SessionType,
    onSessionTypeSelected: (SessionType) -> Unit,
    onNextClicked: () -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp),
    ) {
        FText(
            text = "Select the type of session",
            configuration = FlySightTheme.typography.cardTitle
        )
        Spacer(modifier = Modifier.requiredHeight(8.dp))
        val context = LocalContext.current
        DropdownContainer(
            label = "Session type",
            selectedValue = stringResource(selectedSessionType.textResource),
            options = availableTypes.map { stringResource(it.textResource) },
            onSelectionChanged = {
                SessionType.fromText(context, it)?.let { sessionType ->
                    onSessionTypeSelected(sessionType)
                }
            }
        )

        Spacer(modifier = Modifier.requiredHeight(8.dp))

        when (selectedSessionType) {
            SessionType.Hud -> {
                SessionTypeExplanation(
                    icon = R.drawable.outline_head_mounted_device_24,
                    title = stringResource(SessionType.Hud.textResource),
                    explanation = "A Head-up Display for PPC competitions"
                )
            }

            SessionType.PlaneDisplay -> {
                SessionTypeExplanation(
                    icon = R.drawable.outline_tv_with_assistant_24,
                    title = stringResource(SessionType.PlaneDisplay.textResource),
                    explanation = "A display to put on plane dashboards for PPC competitions, so we can all make sure the competition exit window requirements are met."
                )
            }

            SessionType.FlyBlind -> {
                SessionTypeExplanation(
                    icon = R.drawable.outline_track_changes_24,
                    title = stringResource(SessionType.FlyBlind.textResource),
                    explanation = "A Head-up Display with indications to navigate to a reference point"
                )
            }

            SessionType.SpaceInvaders -> {
                SessionTypeExplanation(
                    icon = R.drawable.space_invader_vector,
                    title = stringResource(SessionType.SpaceInvaders.textResource),
                    explanation = "Defend your dropzone against incoming invaders by shooting at them"
                )
            }

            SessionType.FlyToDraw -> {
                SessionTypeExplanation(
                    icon = R.drawable.outline_crossword_24,
                    title = stringResource(SessionType.FlyToDraw.textResource),
                    explanation = "Wingsuit Pixel War!!"
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        PrevNextBar(
            prevEnabled = false,
            onPrevClicked = {},
            nextEnabled = selectedSessionType == SessionType.Hud ||
                    selectedSessionType == SessionType.PlaneDisplay ||
                    selectedSessionType == SessionType.FlyBlind,
            onNextClicked = onNextClicked,
            showStepCounter = true,
            currentStep = 1,
            maxStep = if (selectedSessionType == SessionType.PlaneDisplay) 2 else 3
        )
    }
}

@Composable
fun SelectSourceScreen(
    selectedSessionType: SessionType,
    availableSources: List<SessionSource>,
    selectedSourceType: SessionSourceType,
    selectedSource: SessionSource?,
    locationAvailabilityState: LocationAvailabilityState,
    onSourceTypeSelected: (SessionSourceType) -> Unit,
    onSourceSelected: (SessionSource) -> Unit,
    onPrevClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onCheckLocationSettings: () -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp),
    ) {
        FText(
            text = "Select the source to use",
            configuration = FlySightTheme.typography.cardTitle
        )
        Spacer(modifier = Modifier.requiredHeight(8.dp))
        val context = LocalContext.current
        DropdownContainer(
            label = "Source type",
            selectedValue = stringResource(selectedSourceType.textResource),
            options = SessionSourceType.entries.map { stringResource(it.textResource) },
            onSelectionChanged = {
                SessionSourceType.fromText(context, it)?.let { sourceType ->
                    onSourceTypeSelected(sourceType)
                }
            }
        )
        val filteredSources = remember(selectedSourceType, availableSources) {
            availableSources.filter { it.sessionSourceType == selectedSourceType }
        }
        Spacer(modifier = Modifier.requiredHeight(8.dp))

        when (selectedSourceType) {
            SessionSourceType.Local -> LocalSourceTypeContent(
                locationAvailabilityState = locationAvailabilityState,
                onRequestLocationPermission = onRequestLocationPermission,
                onCheckLocationSettings = onCheckLocationSettings
            )

            SessionSourceType.FlySight -> FlySightSourceTypeContent()
            SessionSourceType.Record -> RecordSourceTypeContent()
        }

        Spacer(modifier = Modifier.requiredHeight(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredSources) { source ->
                Card(
                    border = if (source == selectedSource) {
                        BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
                    onClick = {
                        onSourceSelected(source)
                    }
                ) {
                    when (source) {
                        is SessionSource.Record -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .requiredHeight(80.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                FText(
                                    text = source.file.phoneFilePath
                                )
                            }
                        }

                        is SessionSource.FlySight -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .requiredHeight(80.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                FText(
                                    text = source.fsName
                                )
                            }
                        }

                        SessionSource.Local -> {}
                    }
                }
            }
        }

        val nextEnabled = when {
            selectedSourceType == SessionSourceType.Local &&
                    locationAvailabilityState != LocationAvailabilityState.LocationAvailable -> false

            selectedSource != null || selectedSourceType == SessionSourceType.Local -> true
            else -> false
        }

        PrevNextBar(
            prevEnabled = true,
            onPrevClicked = onPrevClicked,
            nextEnabled = nextEnabled,
            onNextClicked = onNextClicked,
            showStepCounter = true,
            currentStep = if (selectedSessionType == SessionType.Hud || selectedSessionType == SessionType.FlyBlind) 3 else 2,
            maxStep = if (selectedSessionType == SessionType.Hud || selectedSessionType == SessionType.FlyBlind) 3 else 2
        )
    }
}

@Composable
fun LocalSourceTypeContent(
    locationAvailabilityState: LocationAvailabilityState,
    onRequestLocationPermission: () -> Unit,
    onCheckLocationSettings: () -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Smartphone,
                contentDescription = ""
            )
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            FText(
                text = "Onboard GPS",
                configuration = FlySightTheme.typography.cardTitle
            )
        }
        Spacer(modifier = Modifier.requiredHeight(8.dp))

        FText(
            text = """Use the GPS from your phone as the source of data.
                        |No need to use a FlySight.
                        |Location services are enabled and ready to use.
                        """.trimMargin()
                .trim()
        )
        when (locationAvailabilityState) {
            LocationAvailabilityState.ForegroundLocationNotAllowed -> {
                Spacer(modifier = Modifier.requiredHeight(56.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        FText(
                            text = "Location permission not granted",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Button(
                        onClick = onRequestLocationPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = ""
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        FText(
                            text = "Grant Location Permission"
                        )
                    }
                }
            }

            LocationAvailabilityState.SettingNotEnabled -> {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        FText(
                            text = "Location service not enabled",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Button(
                        onClick = onCheckLocationSettings,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = ""
                        )
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        FText(
                            text = "Enable Location Services"
                        )
                    }
                }
            }

            LocationAvailabilityState.LocationAvailable -> {}
        }
    }
}

@Composable
fun SessionTypeExplanation(
    @DrawableRes icon: Int,
    title: String,
    explanation: String
) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.requiredSize(24.dp),
                painter = painterResource(icon),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            FText(
                text = title,
                configuration = FlySightTheme.typography.cardTitle
            )
        }
        Spacer(modifier = Modifier.requiredHeight(8.dp))
        FText(
            text = explanation
        )
        Spacer(modifier = Modifier.requiredHeight(8.dp))
    }
}

@Composable
fun FlySightSourceTypeContent() {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.requiredSize(24.dp),
                painter = painterResource(R.drawable.flysight_logo_only_2),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            FText(
                text = "FlySight Device",
                configuration = FlySightTheme.typography.cardTitle
            )
        }
        Spacer(modifier = Modifier.requiredHeight(8.dp))
        FText(
            text = """Connect to your FlySight device via Bluetooth to receive real-time data.
                |Select your device from the list below.
                |
                |You need to power on the FlySight to receive data from it.
                """.trimMargin()
                .trim()
        )
        Spacer(modifier = Modifier.requiredHeight(8.dp))
    }
}

@Composable
fun RecordSourceTypeContent() {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = ""
            )
            Spacer(modifier = Modifier.requiredWidth(8.dp))
            FText(
                text = "TRACK.CSV file",
                configuration = FlySightTheme.typography.cardTitle
            )
        }
        Spacer(modifier = Modifier.requiredHeight(8.dp))
        FText(
            text = """We use a TRACK.CSV file generated by a FlySight from a previous session.
                    |This allows to check and adjust a profile before using it up there.
                """.trimMargin()
                .trim()
        )
    }
}

@Composable
fun PrevNextBar(
    modifier: Modifier = Modifier.fillMaxWidth(),
    prevEnabled: Boolean,
    onPrevClicked: () -> Unit,
    nextEnabled: Boolean,
    onNextClicked: () -> Unit,
    showStepCounter: Boolean = false,
    currentStep: Int = 0,
    maxStep: Int = 0
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (showStepCounter) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Step $currentStep on $maxStep",
                textAlign = TextAlign.Center
            )
        }
        Row {
            Button(
                enabled = prevEnabled,
                onClick = onPrevClicked
            ) {
                FText(
                    text = "Previous"
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                enabled = nextEnabled,
                onClick = onNextClicked
            ) {
                FText(
                    text = "Next"
                )
            }
        }
    }
}

@Preview
@Composable
fun PrevNextBarPreview() {
    PrevNextBar(
        prevEnabled = true,
        onPrevClicked = {},
        nextEnabled = true,
        onNextClicked = {}
    )
}