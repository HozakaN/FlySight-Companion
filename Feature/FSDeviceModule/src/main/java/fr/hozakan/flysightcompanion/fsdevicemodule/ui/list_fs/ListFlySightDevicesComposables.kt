package fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.composablecommons.SimpleVerticalDialogActionBar
import fr.hozakan.flysightcompanion.designsystem.extension.distanceTextResource
import fr.hozakan.flysightcompanion.designsystem.theme.CustomColors
import fr.hozakan.flysightcompanion.designsystem.theme.TextConfiguration
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.fsdevicemodule.R as LocalR
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.GnssData

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ListFlySightDevicesMenuActions() {

    val factory = LocalViewModelFactory.current

    val viewModel: ListFlySightDevicesViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.hasBluetoothPermission
            && state.bluetoothState == BluetoothService.BluetoothState.Available
            && state.devices.isNotEmpty()
        ) {
            if (state.refreshingDeviceList is LoadingState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.requiredSize(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.requiredWidth(8.dp))
            }
            IconButton(
                onClick = {
                    viewModel.addDevice()
                },
            ) {
                Icon(
                    painter = painterResource(LocalR.drawable.new_window),
                    contentDescription = stringResource(R.string.list_device_add_new)
                )
            }
            Spacer(modifier = Modifier.requiredWidth(8.dp))
        }
    }
    var showInfoDialog by remember { mutableStateOf(false) }
    IconButton(
        onClick = {
            showInfoDialog = true
        },
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = stringResource(R.string.list_device_info)
        )
    }
    if (showInfoDialog) {
        Dialog(
            onDismissRequest = {
                showInfoDialog = false
            }
        ) {
            Card {
                Column(
                    modifier = Modifier.requiredHeight(120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Version ${state.versionName}")
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        TextButton(
                            onClick = {
                                showInfoDialog = false
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.misc_ok).uppercase(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@Composable
fun ListFlySightDevicesScreen(
    onDeviceSelected: (FlySightDevice) -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: ListFlySightDevicesViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    ListFlySightDevicesScreenInternal(
        state = state,
        onDeviceSelected = onDeviceSelected,
        onRequestBluetoothPermissionClicked = {
            viewModel.requestBluetoothPermission()
        },
        onEnableBluetoothClicked = {
            viewModel.enableBluetooth()
        },
        onCancelScanClicked = {
            viewModel.onCancelScanClicked()
        },
        refreshBluetoothDeviceListClicked = {
            viewModel.refreshBluetoothDeviceList()
        },
        onAddDeviceClicked = {
            viewModel.addDevice()
        },
        onConnectDeviceClicked = {
            viewModel.connectDevice(it)
        },
        onUploadConfigToSystemClicked = {
            viewModel.uploadConfigToSystem(it)
        },
        onUpdateSystemConfigClicked = {
            viewModel.updateSystemConfig(it)
        },
        onPushConfigToDeviceClicked = {
            viewModel.pushConfigToDevice(it)
        },
        onChangeDeviceConfigurationClicked = {
            viewModel.changeDeviceConfiguration(it)
        },
        onUploadRecordToSystem = {
            viewModel.uploadRecordToSystem(it)
        },
        onPreventDialogForFirmwareVersion = {
            viewModel.preventDialogForFirmwareVersion(it)
        },
        onUpdateFirmwareClicked = {
            viewModel.updateFirmware(it)
        }
    )
}

@Composable
internal fun ListFlySightDevicesScreenInternal(
    state: ListFlySightDevicesState,
    onDeviceSelected: (FlySightDevice) -> Unit,
    onRequestBluetoothPermissionClicked: () -> Unit,
    onEnableBluetoothClicked: () -> Unit,
    onCancelScanClicked: () -> Unit,
    refreshBluetoothDeviceListClicked: () -> Unit,
    onAddDeviceClicked: () -> Unit,
    onConnectDeviceClicked: (ListFlySightDeviceDisplayData) -> Unit,
    onUploadConfigToSystemClicked: (ListFlySightDeviceDisplayData) -> Unit,
    onUpdateSystemConfigClicked: (ListFlySightDeviceDisplayData) -> Unit,
    onPushConfigToDeviceClicked: (FlySightDevice) -> Unit,
    onChangeDeviceConfigurationClicked: (ListFlySightDeviceDisplayData) -> Unit,
    onUploadRecordToSystem: (ListFlySightDeviceDisplayData) -> Unit,
    onPreventDialogForFirmwareVersion: (ListFlySightDeviceDisplayData) -> Unit,
    onUpdateFirmwareClicked: (ListFlySightDeviceDisplayData) -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (!state.hasBluetoothPermission) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.list_device_missing_ble_permission))
                Spacer(modifier = Modifier.padding(8.dp))
                Button(
                    onClick = {
                        onRequestBluetoothPermissionClicked()
                    }
                ) {
                    Text(text = stringResource(R.string.misc_grant_permission))
                }
            }
        } else if (state.bluetoothState != BluetoothService.BluetoothState.Available) {
            if (state.bluetoothState == BluetoothService.BluetoothState.NotAvailable) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.list_device_ble_not_available))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(R.string.list_device_ble_disabled))
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            onEnableBluetoothClicked()
                        }
                    ) {
                        Text(text = stringResource(R.string.misc_enable_bluetooth))
                    }
                }
            }
        } else {
            val refreshingDeviceList = state.refreshingDeviceList
            if (refreshingDeviceList is LoadingState.Loading && state.devices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .padding(bottom = 24.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            CircularProgressIndicator()
                        }
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            val textResource = when (refreshingDeviceList.increment) {
                                1 -> {
                                    R.string.list_device_refresh_info_2
                                }

                                2 -> {
                                    R.string.list_device_refresh_info_3
                                }

                                else -> {
                                    R.string.list_device_refresh_info_1
                                }
                            }
                            Text(
                                text = stringResource(textResource),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            TextButton(
                                onClick = {
                                    onCancelScanClicked()
                                }
                            ) {
                                Text(text = stringResource(R.string.misc_cancel))
                            }
                        }
                    }

                }
            } else if (state.devices.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FText(
                        text = stringResource(R.string.list_devices_no_device_found),
                        configuration = TextConfiguration.PlainScreenTextLarge
                    )
                    Spacer(modifier = Modifier.padding(16.dp))
                    Button(
                        onClick = {
                            refreshBluetoothDeviceListClicked()
                        }
                    ) {
                        FText(
                            text = stringResource(R.string.list_devices_refresh_list),
                            configuration = TextConfiguration.PlainScreenButtonText
                        )
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            onAddDeviceClicked()
                        }
                    ) {
                        FText(
                            text = stringResource(R.string.list_device_add_new),
                            configuration = TextConfiguration.PlainScreenButtonText
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.devices) { device ->
                        FlySightDeviceItem(
                            device = device,
                            firmwareCompatibilityMatrix = state.compatibilityMatrix,
                            unitSystem = state.unitSystem,
                            updatingConfiguration = state.updatingConfiguration == device.volatileUuid,
                            onConnectionClicked = {
                                onConnectDeviceClicked(device)
                            },
                            onDeviceClicked = {
                                onDeviceSelected(device)
                            },
                            onUploadConfigToSystem = {
                                onUploadConfigToSystemClicked(device)
                            },
                            onUpdateSystemConfClicked = {
                                onUpdateSystemConfigClicked(device)
                            },
                            onPushConfigToDeviceClicked = {
                                onPushConfigToDeviceClicked(device)
                            },
                            onChangeDeviceConfigurationClicked = {
                                onChangeDeviceConfigurationClicked(device)
                            },
                            onUploadRecordToSystem = {
                                onUploadRecordToSystem(device)
                            },
                            onPreventDialogForFirmwareVersion = {
                                onPreventDialogForFirmwareVersion(device)
                            },
                            onUpdateFirmwareClicked = {
                                onUpdateFirmwareClicked(device)
                            }
                        )
                    }
                }
                if (state.uploadingRecord != null) {
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
                                    text = state.uploadingRecord,
                                    configuration = FlySightTheme.typography.plainScreenTextLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun FlySightDeviceItem(
    device: ListFlySightDeviceDisplayData,
    firmwareCompatibilityMatrix: FirmwareCompatibilityMatrix,
    unitSystem: UnitSystem,
    updatingConfiguration: Boolean,
    onConnectionClicked: () -> Unit,
    onDeviceClicked: () -> Unit,
    onUploadConfigToSystem: () -> Unit,
    onUpdateSystemConfClicked: () -> Unit,
    onPushConfigToDeviceClicked: () -> Unit,
    onChangeDeviceConfigurationClicked: () -> Unit,
    onUploadRecordToSystem: () -> Unit,
    onPreventDialogForFirmwareVersion: () -> Unit,
    onUpdateFirmwareClicked: () -> Unit
) {
    var firmwareUpdateDialogOpened by remember { mutableStateOf(false) }
    Card {
        val connectionState by device.connectionState.collectAsState()

        val mode by device.deviceMode.collectAsState()

        Timber.d("Composing FlySightDeviceItem with device state : $connectionState")

        val resultFilesState by device.records.collectAsState()

        val clickableModifier =
            if (connectionState == DeviceConnectionState.Connected && mode == DeviceMode.Sleep) {
                if (device.hasFirmwareUpdate && device.canShowFirmwareWarning) {
                    Modifier.clickable {
                        firmwareUpdateDialogOpened = true
                    }
                } else if (resultFilesState is LoadingState.Loaded) {
                    Modifier.clickable {
                        onDeviceClicked()
                    }
                } else {
                    Modifier
                }
            } else {
                Modifier
            }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 192.dp)
                .then(clickableModifier)
        ) {
            when (connectionState) {
                DeviceConnectionState.Connected -> {

                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (device.isBle) {
                                Spacer(modifier = Modifier.requiredWidth(8.dp))
                                ConnectionIndicator(
                                    connectionState = connectionState,
                                    flySightDevice = device
                                )
                                Spacer(modifier = Modifier.requiredWidth(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "This device is connected through Bluetooth"
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Usb,
                                    contentDescription = "This device is connected through Usb"
                                )
                            }
                            if (device.hasFirmwareUpdate && device.canShowFirmwareWarning) {
                                Spacer(modifier = Modifier.requiredWidth(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "",
                                    tint = CustomColors.Orange
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = onConnectionClicked
                            ) {
                                Text(connectionText(connectionState))
                            }
                        }
                        when (mode) {
                            DeviceMode.Active -> DeviceItemModeActive(device = device)
                            DeviceMode.Sleep -> {
                                val configFileState by device.configFile.collectAsState()
                                FlySightDeviceItemConfigBody(
                                    device = device,
                                    updatingConfiguration = updatingConfiguration,
                                    configFileState = configFileState,
                                    unitSystem = unitSystem,
                                    onUploadConfigToSystem = onUploadConfigToSystem,
                                    onUpdateSystemConfClicked = onUpdateSystemConfClicked,
                                    onPushConfigToDeviceClicked = onPushConfigToDeviceClicked,
                                    onChangeDeviceConfigurationClicked = onChangeDeviceConfigurationClicked,
                                    onUploadRecordToSystem = onUploadRecordToSystem
                                )
                            }
//                        DeviceMode.Config -> TODO()
//                        DeviceMode.Usb -> TODO()
//                        DeviceMode.Pairing -> TODO()
//                        DeviceMode.Start -> TODO()
                            else -> {}
                        }
                    }
                }

                DeviceConnectionState.Connecting -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 192.dp)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.requiredWidth(8.dp))
                            if (device.isBle) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "This device is connected through Bluetooth"
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Usb,
                                    contentDescription = "This device is connected through Usb"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.requiredHeight(32.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.requiredWidth(8.dp))
                            Text(connectionText(connectionState))
                        }
                    }
                }

                DeviceConnectionState.ConnectionError -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 192.dp)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.requiredWidth(8.dp))
                            if (device.isBle) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "This device is connected through Bluetooth"
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Usb,
                                    contentDescription = "This device is connected through Usb"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.requiredHeight(32.dp))
                        Text(
                            text = connectionText(connectionState),
                            color = Color.Red
                        )
                    }
                }

                DeviceConnectionState.Disconnected -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 192.dp)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.requiredWidth(8.dp))
                            if (device.isBle) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "This device is connected through Bluetooth"
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Usb,
                                    contentDescription = "This device is connected through Usb"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.requiredHeight(32.dp))
                        Button(
                            onClick = onConnectionClicked
                        ) {
                            Text(connectionText(connectionState))
                        }
                    }
                }
            }
        }
    }

    if (firmwareUpdateDialogOpened) {
        FirmwareUpdateDialog(
            device = device,
            firmwareCompatibilityMatrix = firmwareCompatibilityMatrix,
            onDismissRequest = {
                firmwareUpdateDialogOpened = false
                if (it) {
                    onPreventDialogForFirmwareVersion()
                }
            },
            onValidate = {
                onUpdateFirmwareClicked()
                firmwareUpdateDialogOpened = false
            }
        )
    }
}

@Composable
private fun DeviceItemModeActive(
    device: ListFlySightDeviceDisplayData
) {
    var gpsData: GnssData? by remember { mutableStateOf(null) }
    LaunchedEffect(device) {
        device.gnssFeed.collect {
            gpsData = it
        }
    }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(gpsData) {
        val data = gpsData
        if (data != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(data.lat, data.lon),
                        10f
                    )
                )
            )
        }
    }

    Surface(
        modifier = Modifier.requiredHeight(160.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL,
                isBuildingEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false,
                indoorLevelPickerEnabled = false,
                myLocationButtonEnabled = false,
                rotationGesturesEnabled = false,
                scrollGesturesEnabled = false,
                scrollGesturesEnabledDuringRotateOrZoom = false,
                tiltGesturesEnabled = false,
                zoomGesturesEnabled = false
            )
        ) {
            gpsData?.let {
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            it.lat,
                            it.lon
                        )
                    ),
                    title = "Current Position"
                )
            }
        }
    }
}

@Composable
fun FirmwareUpdateDialog(
    device: ListFlySightDeviceDisplayData,
    firmwareCompatibilityMatrix: FirmwareCompatibilityMatrix,
    onDismissRequest: (Boolean) -> Unit,
    onValidate: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismissRequest(false) }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                FText(
                    modifier = Modifier.fillMaxWidth(),
                    text = device.name,
                    configuration = FlySightTheme.typography.cardTitle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.requiredHeight(32.dp))
                Text(
                    text = "You can update the firmware of your device to version ${firmwareCompatibilityMatrix.firmwares.first().name}",
                    color = CustomColors.Orange
                )
                Spacer(modifier = Modifier.requiredHeight(32.dp))
                var donnotShowAgain by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.clickable { donnotShowAgain = !donnotShowAgain },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = donnotShowAgain,
                        onCheckedChange = {
                            donnotShowAgain = !donnotShowAgain
                        }
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text(
                        text = "Don't show this message again for this version"
                    )
                }
                SimpleDialogActionBar(
                    onCancel = { onDismissRequest(donnotShowAgain) },
                    onValidate = onValidate,
                    validateButtonText = "Update".uppercase()
                )
            }
        }
    }
}

@Composable
fun DeviceRecordsContainer(
    modifier: Modifier = Modifier,
    device: ListFlySightDeviceDisplayData,
    onUploadRecordToSystem: () -> Unit
) {
    val locales = LocalConfiguration.current.locales
    val locale = if (locales.isEmpty) Locale.ROOT else locales[0]
    val dateTimeFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
    }

    val resultFiles by device.records.collectAsState()
    val warning = !device.isLastRecordUploaded
    var warningDialogOpened by remember { mutableStateOf(false) }
    val mod = if (warning) {
        Modifier
            .clickable {
                warningDialogOpened = true
            }
            .padding(8.dp)
    } else {
        Modifier.padding(8.dp)
    }
    Column(
        modifier = modifier
            .then(mod)
    ) {
        if (warning) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.list_device_records_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier.requiredSize(24.dp),
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.list_device_item_record_warning_content_description),
                    tint = CustomColors.Orange
                )
            }
        } else {
            Text(
                text = stringResource(R.string.list_device_records_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        when (val files = resultFiles) {
            is LoadingState.Error<List<RecordFile>> -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.list_device_error_loading_records),
                    )
                }
            }

            is LoadingState.Loaded<List<RecordFile>> -> {
                val mostRecentFile = files.value.maxByOrNull { it.dateTime }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.list_device_record_count),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    FText(
                        text = "${files.value.size}",
                        configuration = FlySightTheme.typography.captionText
                    )
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                    Text(
                        text = stringResource(R.string.list_device_record_most_recent),
                        style = MaterialTheme.typography.titleSmall
                    )
                    FText(
                        text = "${mostRecentFile?.dateTime?.format(dateTimeFormatter)}",
                        configuration = FlySightTheme.typography.captionText
                    )
                }
            }

            LoadingState.Idle,
            is LoadingState.Loading<List<RecordFile>> -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.list_device_loading_records))
                }
            }
        }
    }
    if (warningDialogOpened) {
        DeviceLastRecordNotSavedDialog(
            device = device,
            onValidate = {
                onUploadRecordToSystem()
                warningDialogOpened = false
            },
            onDismissRequest = { warningDialogOpened = false }
        )
    }
}

@Composable
private fun DeviceLastRecordNotSavedDialog(
    device: ListFlySightDeviceDisplayData,
    onValidate: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = device.name,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                Text(
                    text = stringResource(R.string.list_device_dialog_upload_most_recent_record),
                    color = CustomColors.Orange
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                SimpleDialogActionBar(
                    onCancel = onDismissRequest,
                    onValidate = onValidate,
                    validateButtonText = stringResource(R.string.misc_upload).uppercase()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlySightDeviceItemConfigBody(
    device: ListFlySightDeviceDisplayData,
    updatingConfiguration: Boolean,
    configFileState: LoadingState<ConfigFile>,
    unitSystem: UnitSystem,
    onUploadConfigToSystem: () -> Unit,
    onUpdateSystemConfClicked: () -> Unit,
    onPushConfigToDeviceClicked: () -> Unit,
    onChangeDeviceConfigurationClicked: () -> Unit,
    onUploadRecordToSystem: () -> Unit
) {
    val rows = 2
    FlowRow(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = rows
    ) {
        val itemModifier = Modifier
            .padding(8.dp)
            .height(220.dp)
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))

        Surface(
            modifier = itemModifier
        ) {
            DeviceConfigurationContainer(
                configFileState = configFileState,
                updatingConfiguration = updatingConfiguration,
                device = device,
                onUploadConfigToSystem = onUploadConfigToSystem,
                onUpdateSystemConfClicked = onUpdateSystemConfClicked,
                onPushConfigToDeviceClicked = onPushConfigToDeviceClicked,
                onChangeDeviceConfigurationClicked = onChangeDeviceConfigurationClicked,
                unitSystem = unitSystem
            )
        }
//        Surface(
//            modifier = itemModifier
//        ) {
//            DeviceRecordsContainer(
//                device = device,
//                onUploadRecordToSystem = onUploadRecordToSystem
//            )
//        }
    }
}

@Composable
private fun DeviceConfigurationContainer(
    modifier: Modifier = Modifier,
    configFileState: LoadingState<ConfigFile>,
    updatingConfiguration: Boolean,
    device: ListFlySightDeviceDisplayData,
    onUploadConfigToSystem: () -> Unit,
    onUpdateSystemConfClicked: () -> Unit,
    onPushConfigToDeviceClicked: () -> Unit,
    onChangeDeviceConfigurationClicked: () -> Unit,
    unitSystem: UnitSystem
) {

    val warning = !device.isConfigFromSystem || device.hasConfigContentChanged
    var warningDialogOpened by remember { mutableStateOf(false) }
    var menuOpened by remember { mutableStateOf(false) }
    val mod = if (warning && configFileState is LoadingState.Loaded) {
        Modifier
            .clickable {
                warningDialogOpened = true
            }
            .padding(8.dp)
    } else {
        Modifier.padding(8.dp)
    }
    Column(
        modifier = modifier
            .then(mod)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.list_device_configuration_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            if (!updatingConfiguration) {
//                Box(
//                    modifier = Modifier.requiredSize(24.dp)
//                ) {
                IconButton(
                    modifier = Modifier.requiredSize(24.dp),
                    onClick = {
                        menuOpened = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(
                            R.string.list_device_item_configuration_menu_content_description
                        )
                    )
                }
                DropdownMenu(
                    expanded = menuOpened,
                    onDismissRequest = { menuOpened = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(
                                    R.string.list_device_item_configuration_menu_change
                                ),
                                textAlign = TextAlign.Center
                            )
                        },
                        onClick = {
                            menuOpened = false
                            onChangeDeviceConfigurationClicked()
                        }
                    )
                }
            }
//            }
        }
        if (updatingConfiguration) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(stringResource(R.string.list_device_item_configuration_updating))
            }
            return
        }
        when (configFileState) {
            is LoadingState.Error -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.list_device_item_load_configuration_error))
                }
            }

            is LoadingState.Loading -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.list_device_item_loading_configuration))
                }
            }

            LoadingState.Idle -> {}
            is LoadingState.Loaded -> {
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                if (warning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = configFileState.value.name.ifBlank { stringResource(R.string.list_device_item_configuration_no_name) })
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            modifier = Modifier.requiredSize(24.dp),
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.list_device_item_configuration_warning_content_description),
                            tint = CustomColors.Orange
                        )
                    }
                } else {
                    Text(text = configFileState.value.name.ifBlank { stringResource(R.string.list_device_item_configuration_no_name) })
                }
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                FText(
                    text = stringResource(
                        R.string.list_config_file_dz_elev_info,
                        configFileState.value.dzElev,
                        stringResource(unitSystem.distanceTextResource)
                    ),
                    configuration = FlySightTheme.typography.plainScreenTextLarge
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                Text(
                    text = stringResource(
                        R.string.list_config_file_speech_count,
                        configFileState.value.speeches.size
                    ),
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                Text(
                    text = stringResource(
                        R.string.list_config_file_alarm_count,
                        configFileState.value.alarms.size
                    ),
                )
                Spacer(modifier = Modifier.requiredHeight(8.dp))
                Text(
                    text = stringResource(
                        R.string.list_config_file_silence_window_count,
                        configFileState.value.silenceWindows.size
                    ),
                )
            }
        }
    }
    if (warningDialogOpened) {
        DeviceConfigurationMisMatchDialog(
            configFileState = configFileState,
            device = device,
            onDismissRequest = { warningDialogOpened = false },
            onUploadConfigToSystem = {
                warningDialogOpened = false
                onUploadConfigToSystem()
            },
            onUpdateSystemConfClicked = {
                warningDialogOpened = false
                onUpdateSystemConfClicked()
            },
            onPushConfigToDeviceClicked = {
                warningDialogOpened = false
                onPushConfigToDeviceClicked()
            }
        )
    }
}

@Composable
internal fun DeviceConfigurationMisMatchDialog(
    configFileState: LoadingState<ConfigFile>,
    device: ListFlySightDeviceDisplayData,
    onDismissRequest: () -> Unit,
    onUploadConfigToSystem: () -> Unit,
    onUpdateSystemConfClicked: () -> Unit,
    onPushConfigToDeviceClicked: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = device.name,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                if (!device.isConfigFromSystem) {
                    Text(
                        text = stringResource(R.string.list_device_dialog_upload_config),
                        color = CustomColors.Orange
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    SimpleDialogActionBar(
                        onCancel = onDismissRequest,
                        onValidate = onUploadConfigToSystem,
                        validateButtonText = stringResource(R.string.misc_upload).uppercase()
                    )
                } else if (device.hasConfigContentChanged) {
                    Text(
                        text = stringResource(
                            R.string.list_device_dialog_config_content_changed,
                            configFileState.content?.name ?: stringResource(R.string.misc_unknown)
                        ),
                        color = CustomColors.Orange
                    )
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                    SimpleVerticalDialogActionBar(
                        onDismissRequest = onDismissRequest,
                        onNeutral = onUpdateSystemConfClicked,
                        onValidate = onPushConfigToDeviceClicked,
                        validateButtonText = stringResource(R.string.list_device_dialog_update_device_conf).uppercase(),
                        neutralButtonText = stringResource(R.string.list_device_dialog_update_local_conf).uppercase(),
                        cancelButtonText = stringResource(R.string.list_device_dialog_do_nothing).uppercase()
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ConnectionIndicator(
    connectionState: DeviceConnectionState,
    flySightDevice: FlySightDevice
) {
    BoxWithConstraints(
        modifier = Modifier.requiredSize(8.dp)
    ) {
        if (connectionState == DeviceConnectionState.Connected) {
            var shouldAnimate by remember { mutableStateOf(false) }
            LaunchedEffect(connectionState, flySightDevice) {
                flySightDevice.ping
                    .collect {
                        shouldAnimate = true
//                        delay(1_000)
//                        shouldAnimate = false
                    }
            }
            val density = LocalDensity.current.density
            var circleStrokeWidth by remember { mutableFloatStateOf(0f) }
            var radius by remember { mutableFloatStateOf((minHeight.value * density) / 4) }
            var animatedAlpha by remember { mutableFloatStateOf(1f) }
            with(LocalDensity.current) {
                LaunchedEffect(shouldAnimate) {
                    if (shouldAnimate) {
                        launch {
                            animate(
                                initialValue = 0.dp.toPx(),
                                targetValue = 2.dp.toPx(),
                                animationSpec = tween(durationMillis = 1_000),
                            ) { value, _ ->
                                circleStrokeWidth = value
                            }
                        }
                        launch {
                            animate(
                                initialValue = minHeight.toPx() / 4,
                                targetValue = minHeight.toPx(),
                                animationSpec = tween(durationMillis = 1_000),
                            ) { value, _ ->
                                radius = value
                            }
                        }
                        launch {
                            animate(
                                initialValue = 1f,
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 1_000),
                            ) { value, _ ->
                                animatedAlpha = value
                            }
                        }
                        delay(1_000)
                        circleStrokeWidth = 0f
                        radius = minHeight.toPx() / 4
                        animatedAlpha = 1f

                        shouldAnimate = false
                    }
                }
            }
            if (shouldAnimate) {
                Box(
                    modifier = Modifier.requiredSize(8.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .requiredSize(8.dp)
                    ) {
                        drawCircle(
                            color = connectionState.connectionColor.copy(alpha = animatedAlpha),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = radius,
                            style = Stroke(
                                width = circleStrokeWidth
                            )
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .requiredSize(8.dp)
                .background(
                    color = connectionState.connectionColor,
                    shape = CircleShape
                )
        )
    }
}

private val DeviceConnectionState.connectionColor: Color
    get() = when (this) {
        DeviceConnectionState.Connected -> Color.Green
        DeviceConnectionState.Disconnected -> Color.Gray
        DeviceConnectionState.Connecting -> Color.Yellow
        DeviceConnectionState.ConnectionError -> Color.Red
    }

@Composable
fun connectionText(connectionState: DeviceConnectionState): String = when (connectionState) {
    DeviceConnectionState.Connected -> {
        stringResource(R.string.device_connection_button_disconnect)
    }

    DeviceConnectionState.Disconnected -> {
        stringResource(R.string.device_connection_button_connect)
    }

    DeviceConnectionState.Connecting -> {
        stringResource(R.string.device_connection_button_connecting)
    }

    DeviceConnectionState.ConnectionError -> {
        stringResource(R.string.misc_error)
    }
}
