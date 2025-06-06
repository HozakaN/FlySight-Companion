package fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.GnssData
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@Composable
fun DeviceDetailMenuActions(
    deviceId: String,
    onShowDeviceConfigClicked: (config: ConfigFile) -> Unit
) {

    val factory = LocalViewModelFactory.current

    val viewModel: DeviceDetailViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = deviceId) {
        viewModel.loadDevice(deviceId)
    }

    var configFileState by remember { mutableStateOf<LoadingState<ConfigFile>?>(null) }
    LaunchedEffect(key1 = state.device?.configFile) {
        val configFileStateFlow = state.device?.configFile
        if (configFileStateFlow == null) {
            configFileState = null
        } else {
            configFileStateFlow.collect {
                configFileState = it
            }
        }
    }

    when (val immutableConfigFileState = configFileState) {
        is LoadingState.Loaded -> {
            TextButton(
                onClick = {
                    onShowDeviceConfigClicked(immutableConfigFileState.value)
                }
            ) {
                Text(text = stringResource(R.string.device_detail_show_config))
            }
        }

        else -> {}
    }
}

@Composable
fun DeviceDetailScreen(
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

    val toastEvent = state.toastEvent?.getContentIfNotHandled()
    val context = LocalContext.current
    LaunchedEffect(toastEvent) {
        if (toastEvent != null) {
            Toast.makeText(context, toastEvent, Toast.LENGTH_LONG).show()
        }
    }

    var configFileState by remember { mutableStateOf<LoadingState<ConfigFile>?>(null) }
    LaunchedEffect(key1 = state.device?.configFile) {
        val configFileStateFlow = state.device?.configFile
        if (configFileStateFlow == null) {
            configFileState = null
        } else {
            configFileStateFlow.collect {
                configFileState = it
            }
        }
    }

//    val logs by state.device?.logs?.collectAsState() ?: return
//
//    LazyColumn(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        items(logs.reversed()) {
//            Text(it)
//        }
//    }
//
//    return

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
//                item {
//                    GnssDataContainer(state.device)
//                }
//
//                item {
//                    LogsContainer(state.device)
//                }
                Timber.d("Hoz3 state.hasFirmwareUpdate: ${state.hasFirmwareUpdate}, showFirmwareUpdateInfo: ${state.showFirmwareUpdateInfo}, currentDirectoryPath: ${state.currentDirectoryPath}")
                if (state.hasFirmwareUpdate && state.showFirmwareUpdateInfo && (state.currentDirectoryPath.isEmpty() || state.currentDirectoryPath.size == 1)) {
                    item {
                        UpdateInfoContainer(
                            onUpdateClicked = {
                                viewModel.updateFirmware()
                            },
                            onDismissClicked = {
                                viewModel.closeFirmwareUpdateInfo()
                            }
                        )
                    }
                }
                item {
                    BreadCrumb(
                        modifier = Modifier.padding(8.dp),
                        path = state.currentDirectoryPath,
                        onPathPartClicked = { path ->
                            viewModel.loadDirectory(path)
                        }
                    )
                }
                items(state.directoryContent) { fileInfo ->
                    Row(
                        modifier = Modifier.clickable {
                            viewModel.onFileClicked(fileInfo)
                        }
                    ) {
                        if (fileInfo.isDirectory) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = stringResource(
                                    R.string.device_detail_folder,
                                    fileInfo.fileName
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = stringResource(
                                    R.string.device_detail_file,
                                    fileInfo.fileName
                                )
                            )
                        }
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        Text(text = fileInfo.fileName)
                    }
                }
            }
        }
        if (state.isInTrackFolder) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        viewModel.downloadRecord()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(R.string.device_detail_download_record)
                    )
                }
            }
        }
        state.uploadingRecord?.let { uploadingRecord ->
            Dialog(
                onDismissRequest = {}
            ) {
                Card {
                    Row(
                        modifier = Modifier.padding(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        FText(
                            modifier = Modifier.fillMaxWidth(),
                            text = uploadingRecord,
                            configuration = FlySightTheme.typography.plainScreenTextLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GnssDataContainer(device: FlySightDevice?) {
    if (device == null) return
    var data: GnssData? by remember { mutableStateOf(null) }
    LaunchedEffect(device) {
        device.gnssFeed.collect {
            data = it
        }
    }
    Box(
        modifier = Modifier.padding(8.dp)
    ) {
        Card {
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 56.dp)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("data : $data")
            }
        }
    }

}

@Composable
fun LogsContainer(device: FlySightDevice?) {
    if (device == null) return
    val logs by device.logs.collectAsState()

    Box(
        modifier = Modifier.padding(8.dp)
    ) {
        Card {
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 56.dp)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(logs.joinToString("\n"))
            }
        }
    }

}

@Composable
fun UpdateInfoContainer(
    onUpdateClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    Card(
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row {
                Box(
                    modifier = Modifier.requiredHeight(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FText(
                        text = "New firmware available!",
                        configuration = FlySightTheme.typography.cardTitle
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .requiredSize(48.dp)
                        .clickable {
                            onDismissClicked()
                        }
                        .padding(8.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss firmware update info",
                )
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            SimpleDialogActionBar(
                showCancelButton = false,
                onValidate = onUpdateClicked,
                validateButtonText = "Update",
            )
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
                        contentDescription = stringResource(R.string.device_detail_path_separator),
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
