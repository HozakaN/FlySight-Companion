package fr.hozakan.flysightble.configfilesmodule.ui.list_files

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCbrt
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.config.UnitSystem

@Composable
fun ListConfigFileMenuActions(
    onCreateConfigFile: () -> Unit
) {
    IconButton(
        onClick = onCreateConfigFile
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
            contentDescription = "New config file"
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
                Text("No config file yet")
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                Button(
                    onClick = onCreateConfigFile
                ) {
                    Text("Create a config file")
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
    deleteConfigFileClicked: () -> Unit
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
                            contentDescription = "Config file actions"
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuExpanded = false
                                deleteDialogOpened = true
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
            if (configFile.description.isNotBlank())  {
                Text(
                    configFile.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                "Dropzone altitude : ${configFile.dzElev} ${unitSystem.distanceText}",
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                "${configFile.speeches.size} speeches",
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                "${configFile.alarms.size} alarms",
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                "${configFile.silenceWindows.size} silence windows",
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
                Text("Delete config file ${configFile.name} ?")
                SimpleDialogActionBar(
                    onDismissRequest = onCancel,
                    onValidate = onConfirm,
                    validateButtonText = "CONFIRM"
                )
            }
        }
    }
}
