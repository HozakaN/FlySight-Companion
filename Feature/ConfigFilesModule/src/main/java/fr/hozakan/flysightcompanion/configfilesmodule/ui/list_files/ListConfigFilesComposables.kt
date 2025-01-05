package fr.hozakan.flysightcompanion.configfilesmodule.ui.list_files

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.designsystem.extension.distanceTextResource
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.config.UnitSystem

@Composable
fun ListConfigFileMenuActions(
    onCreateConfigFile: () -> Unit
) {
    IconButton(
        onClick = onCreateConfigFile
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
            contentDescription = stringResource(R.string.list_config_file_menu_action_new_config_file_content_description)
        )
    }
}

@Composable
fun ListConfigFilesScreen(
    onConfigSelected: (ConfigFile) -> Unit,
    onCreateConfigFile: () -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: ListConfigFilesViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {

        val configFiles = state.configFiles

        if (configFiles.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.list_config_file_no_config_file))
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                Button(
                    onClick = onCreateConfigFile
                ) {
                    Text(text = stringResource(R.string.list_config_file_create_config_file))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(configFiles) { configFile ->
                    ConfigFileItem(
                        configFile = configFile,
                        unitSystem = state.unitSystem,
                        onConfigSelected = {
                            onConfigSelected(configFile)
                        },
                        deleteConfigFileClicked = {
                            viewModel.deleteConfigFile(configFile)
                        },
                        onDuplicateClicked = {
                            viewModel.duplicateConfigFile(configFile)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigFileItem(
    configFile: ConfigFile,
    unitSystem: UnitSystem,
    onConfigSelected: () -> Unit,
    deleteConfigFileClicked: () -> Unit,
    onDuplicateClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onConfigSelected
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    configFile.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                var menuExpanded by remember { mutableStateOf(false) }
                var deleteDialogOpened by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = {
                            menuExpanded = !menuExpanded
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.list_config_file_item_menu_action_content_description)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.misc_delete),
                                    textAlign = TextAlign.Center
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                deleteDialogOpened = true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.misc_duplicate),
                                    textAlign = TextAlign.Center
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDuplicateClicked()
                            }
                        )
                    }
                }

                if (deleteDialogOpened) {
                    DeleteConfigFileDialog(
                        configFile = configFile,
                        onConfirm = {
                            deleteConfigFileClicked()
                            deleteDialogOpened = false
                        },
                        onCancel = {
                            deleteDialogOpened = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            if (configFile.description.isNotBlank()) {
                Text(
                    configFile.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(
                    R.string.list_config_file_dz_elev_info,
                    configFile.dzElev,
                    stringResource(unitSystem.distanceTextResource)
                )
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(
                    R.string.list_config_file_speech_count,
                    configFile.speeches.size
                )
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(
                    R.string.list_config_file_alarm_count,
                    configFile.alarms.size
                )
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = stringResource(
                    R.string.list_config_file_silence_window_count,
                    configFile.silenceWindows.size
                )
            )
        }
    }
}

@Composable
fun DeleteConfigFileDialog(
    configFile: ConfigFile,
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
                        R.string.list_config_file_delete_file_dialog,
                        configFile.name
                    )
                )
                SimpleDialogActionBar(
                    onCancel = onCancel,
                    onValidate = onConfirm,
                    validateButtonText = stringResource(R.string.misc_confirm).uppercase()
                )
            }
        }
    }
}
