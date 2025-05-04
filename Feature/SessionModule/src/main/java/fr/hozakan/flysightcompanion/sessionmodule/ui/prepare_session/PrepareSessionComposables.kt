package fr.hozakan.flysightcompanion.sessionmodule.ui.prepare_session

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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.composablecommons.DropdownContainer
import fr.hozakan.flysightcompanion.designsystem.extension.fromText
import fr.hozakan.flysightcompanion.designsystem.extension.textResource
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSourceType

@Composable
fun PickConfigMenuActions(
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
fun PrepareSessionScreen(
    onCreateConfigurationClicked: () -> Unit,
    onEditConfigurationClicked: (SessionProfile) -> Unit,
    onSessionReady: (SessionProfile, SessionSource?) -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: PrepareSessionViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    if (state.doneEvent?.getContentIfNotHandled() == true) {
        val selectedProfile = state.selectedProfile
        if (selectedProfile != null) {
            onSessionReady(selectedProfile, state.selectedSource)
        }
    }

    PrepareSessionScreenInternal(
        state = state,
        onSessionConfigurationSelected = { viewModel.onSessionProfileSelected(it) },
        onCreateConfigurationClicked = onCreateConfigurationClicked,
        onEditConfigurationClicked = onEditConfigurationClicked,
        onNextClicked = { viewModel.onNextClicked() },
        onPrevClicked = { viewModel.onPrevClicked() },
        onSourceTypeSelected = {
            viewModel.onSourceTypeSelected(it)
        },
        onSourceSelected = {
            viewModel.onSourceSelected(it)
        }
    )
}

@Composable
fun PrepareSessionScreenInternal(
    state: PrepareSessionState,
    onSessionConfigurationSelected: (SessionProfile) -> Unit,
    onCreateConfigurationClicked: () -> Unit,
    onEditConfigurationClicked: (SessionProfile) -> Unit,
    onSourceTypeSelected: (SessionSourceType) -> Unit,
    onSourceSelected: (SessionSource) -> Unit,
    onPrevClicked: () -> Unit,
    onNextClicked: () -> Unit
) {

    val phase = state.prepareSessionPhase

    val sessionConfigurations = state.sessionProfiles

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        when (phase) {
            PrepareSessionPhase.SelectProfile -> SelectProfileScreen(
                sessionProfiles = sessionConfigurations,
                selectedProfile = state.selectedProfile,
                onSessionProfileSelected = {
                    onSessionConfigurationSelected(it)
                },
                onNextClicked = onNextClicked,
                onCreateConfigurationClicked = onCreateConfigurationClicked,
                onEditConfigurationClicked = onEditConfigurationClicked
            )

            PrepareSessionPhase.SelectSource -> SelectSourceScreen(
                availableSources = state.availableSources,
                selectedSourceType = state.selectedSourceType,
                selectedSource = state.selectedSource,
                onSourceTypeSelected = onSourceTypeSelected,
                onSourceSelected = onSourceSelected,
                onPrevClicked = onPrevClicked,
                onNextClicked = onNextClicked
            )
        }
    }
}

@Composable
fun SelectProfileScreen(
    sessionProfiles: LoadingState<List<SessionProfile>>,
    selectedProfile: SessionProfile?,
    onSessionProfileSelected: (SessionProfile) -> Unit,
    onNextClicked: () -> Unit,
    onCreateConfigurationClicked: () -> Unit,
    onEditConfigurationClicked: (SessionProfile) -> Unit
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
                        onClick = onCreateConfigurationClicked
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
                                    onClick = onCreateConfigurationClicked,
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
                            Card(
                                border = if (profile.name == selectedProfile?.name) {
                                    BorderStroke(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    null
                                },
                                onClick = {
                                    onSessionProfileSelected(profile)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .requiredHeight(80.dp)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    FText(
                                        text = profile.name,
                                        configuration = FlySightTheme.typography.plainScreenTextLarge
                                    )
                                }
                            }
                        }
                    }
                    PrevNextBar(
                        prevEnabled = false,
                        onPrevClicked = {},
                        nextEnabled = selectedProfile != null,
                        onNextClicked = onNextClicked,
                        showStepCounter = true,
                        currentStep = 1,
                        maxStep = 2
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
fun SelectSourceScreen(
    availableSources: List<SessionSource>,
    selectedSourceType: SessionSourceType,
    selectedSource: SessionSource?,
    onSourceTypeSelected: (SessionSourceType) -> Unit,
    onSourceSelected: (SessionSource) -> Unit,
    onPrevClicked: () -> Unit,
    onNextClicked: () -> Unit
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
            SessionSourceType.Local -> {
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
                                    """.trimMargin()
                            .trim()
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                }
            }

            SessionSourceType.FlySight -> {

            }

            SessionSourceType.Record -> {
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
        PrevNextBar(
            prevEnabled = true,
            onPrevClicked = onPrevClicked,
            nextEnabled = selectedSource != null || selectedSourceType == SessionSourceType.Local,
            onNextClicked = onNextClicked,
            showStepCounter = true,
            currentStep = 2,
            maxStep = 2
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
