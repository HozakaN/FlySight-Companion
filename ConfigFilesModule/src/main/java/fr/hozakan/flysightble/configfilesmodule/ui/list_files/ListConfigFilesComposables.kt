package fr.hozakan.flysightble.configfilesmodule.ui.list_files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.model.ConfigFile

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
                        onConfigSelected = {
                            onConfigSelected(configFile)
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
    onConfigSelected: () -> Unit
) {
    Card(
        onClick = onConfigSelected
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                configFile.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
