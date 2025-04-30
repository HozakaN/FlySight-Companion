package fr.hozakan.flysightcompanion.sessionmodule.ui.pick_config

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.sessionmodule.ui.config.SessionConfigViewModel

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
fun PickConfigScreen(
) {
    val factory = LocalViewModelFactory.current

    val viewModel: PickConfigViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    val sessionConfigurations = state.sessionConfigurations


    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {

        when (sessionConfigurations) {
            is LoadingState.Error -> {}
            LoadingState.Idle -> {}
            is LoadingState.Loaded -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp
                    ),
                ) {
                    items(sessionConfigurations.value) {
                        Card(
                            onClick = {
                                viewModel.onSessionConfigurationSelected(it)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                FText(
                                    text = it.name,
                                    configuration = FlySightTheme.typography.plainScreenTextLarge
                                )
                            }
                        }
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
}