package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.composablecommons.ExpandableColumn
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.DeviceConnectionState

@Composable
fun ListFlySightDevicesScreen(
    onDeviceSelected: (FlySightDevice) -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: ListFlySightDevicesViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

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
                Text("No bluetooth permission")
                Spacer(modifier = Modifier.padding(8.dp))
                Button(
                    onClick = {
                        viewModel.requestBluetoothPermission()
                    }
                ) {
                    Text("Request permission")
                }
            }
        } else if (state.bluetoothState != BluetoothService.BluetoothState.Available) {
            if (state.bluetoothState == BluetoothService.BluetoothState.NotAvailable) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bluetooth is not available")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Bluetooth is disabled")
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            viewModel.enableBluetooth()
                        }
                    ) {
                        Text("Enable bluetooth")
                    }
                }
            }
        } else {
            if (state.devices.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No devices found")
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            viewModel.refreshBluetoothDeviceList()
                        }
                    ) {
                        Text("Refresh list")
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = {
                            viewModel.addDevice()
                        }
                    ) {
                        Text("Add device")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Button(
                            onClick = {
                                viewModel.addDevice()
                            }
                        ) {
                            Text("Add device")
                        }
                    }
                    items(state.devices) { device ->
                        FlySightDeviceItem(
                            device = device,
                            onConnectionClicked = {
                                viewModel.connectDevice(device)
                            },
                            onDeviceClicked = {
                                onDeviceSelected(device)
                            }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun FlySightDeviceItem(
    device: FlySightDevice,
    onConnectionClicked: () -> Unit,
    onDeviceClicked: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface
        )
    ) {
        var connectionState by remember {
            mutableStateOf<DeviceConnectionState>(
                DeviceConnectionState.Disconnected
            )
        }
        LaunchedEffect(device) {
            device.connectionState.collect {
                connectionState = it
            }
        }
        var modifier = Modifier.fillMaxSize()
        if (connectionState == DeviceConnectionState.Connected) {
            modifier = modifier.clickable {
                onDeviceClicked()
            }
        }
        Column(
            modifier = modifier
        ) {
            var logs by remember { mutableStateOf<List<String>>(emptyList()) }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(device.bluetoothDevice.name)
                Spacer(modifier = Modifier.padding(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("State: $connectionState")
                    Spacer(modifier = Modifier.weight(1f))
                    if (connectionState != DeviceConnectionState.Connecting) {
                        Button(
                            onClick = onConnectionClicked
                        ) {
                            Text(connectionText(connectionState))
                        }
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }
//            var services by remember { mutableStateOf<List<BluetoothGattService>>(emptyList()) }
//            var directoryContent by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
//            LaunchedEffect(device) {
//                device.services.collect {
//                    services = it
//                }
//            }
//            LaunchedEffect(device) {
//                device.directory.collect {
//                    directoryContent = it
//                }
//            }
            LaunchedEffect(device) {
                device.logs.collect {
                    logs = it
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
//            Text("Services:")
//            Spacer(modifier = Modifier.padding(8.dp))
//            services.forEach { service ->
//                Text("Service: ${service.uuid}")
//                service.characteristics.forEach { characteristic ->
//                    Text("Characteristic: ${FlySightCharacteristic.fromUuid(characteristic.uuid)?.name}")
//                }
//            }
//            Spacer(modifier = Modifier.padding(8.dp))
//            Row {
//                Text("Directory content:")
//                Spacer(modifier = Modifier.padding(32.dp))
//                Button(
//                    onClick = {
//                        onRefreshDirectoryContentClicked()
//                    }
//                ) {
//                    Text("Refresh")
//                }
//            }
//            Spacer(modifier = Modifier.padding(8.dp))
//            directoryContent.forEach { entry ->
//                Text("Entry: ${entry.fileName}")
//            }
//            Spacer(modifier = Modifier.padding(8.dp))
//            Text("Logs:")
//            Spacer(modifier = Modifier.padding(8.dp))
            ExpandableColumn(
                headerComposable = { expanded ->
                    Text("Logs")
                },
                contentComposable = {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        logs.forEachIndexed { index, log ->
                            Text("($index) $log")
                        }
                    }
                }
            )
        }
    }
}

fun connectionText(connectionState: DeviceConnectionState): String = when (connectionState) {
    DeviceConnectionState.Connected -> {
        "Disconnect"
    }

    DeviceConnectionState.Disconnected -> {
        "Connect"
    }

    else -> {
        ""
    }
}
