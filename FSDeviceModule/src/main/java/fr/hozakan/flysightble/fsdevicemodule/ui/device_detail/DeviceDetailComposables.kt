package fr.hozakan.flysightble.fsdevicemodule.ui.device_detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.composablecommons.ExpandableColumn
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.model.FileInfo
import fr.hozakan.flysightble.model.FileState

@Composable
fun DeviceDetailComposables(
    deviceId: String,
    onFileClicked: (filePath: List<String>) -> Unit,
    onNavigateUp: () -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: DeviceDetailViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = deviceId) {
        viewModel.loadDevice(deviceId)
    }

    BackHandler(enabled = true) {
        if (state.currentDirectoryPath.size > 1) {
            viewModel.loadDirectory(state.currentDirectoryPath.dropLast(1))
        } else {
            onNavigateUp()
        }
    }

    val event = state.fileClicked?.getContentIfNotHandled()

    if (event != null) {
        onFileClicked(event)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            ExpandableColumn(
                headerComposable = {
                    Text("Config file")
                },
                contentComposable = {
                    val text = when (state.configFile) {
                        is FileState.Error -> "Error fetching config file"
                        FileState.Loading -> "Loading config file"
                        FileState.Nothing -> "No config file"
                        is FileState.Success -> (state.configFile as FileState.Success).content
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = text
                            )
                        }
                    }
                }
            )
            BreadCrumb(
                modifier = Modifier.padding(8.dp),
                path = state.currentDirectoryPath,
                onPathPartClicked = { path ->
                    viewModel.loadDirectory(path)
                }
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.directoryContent) { fileInfo ->
                    Row(
                        modifier = Modifier.clickable {
                            viewModel.onFileClicked(fileInfo)
                        }
                    ) {
                        if (fileInfo.isDirectory) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Folder ${fileInfo.fileName}"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = "File ${fileInfo.fileName}"
                            )
                        }
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        Text(text = fileInfo.fileName)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BreadCrumb(
    modifier: Modifier,
    path: List<String>,
    onPathPartClicked: (List<String>) -> Unit
) {
    FlowRow(
        modifier = modifier
    ) {
        path.forEachIndexed { index, pathPart ->
            val rowModifier = if (index >= path.size - 1) {
                Modifier
            } else {
                Modifier.clickable {
                    onPathPartClicked(path.subList(0, index + 1))
                }
            }
            Row(
                modifier = rowModifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (index > 0) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = "path separator",
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                }
                Text(
                    text = pathPart,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
