package fr.hozakan.flysightble.fsdevicemodule.ui.device_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory

@Composable
fun DeviceDetailComposables(
    deviceId: String
) {
    val factory = LocalViewModelFactory.current

    val viewModel: DeviceDetailViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = deviceId) {
        viewModel.loadDevice(deviceId)
    }
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
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
            Row(modifier = rowModifier) {
                if (index > 0) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = "path separator",
                    )
                }
                Text(pathPart)
            }
        }
    }
}
