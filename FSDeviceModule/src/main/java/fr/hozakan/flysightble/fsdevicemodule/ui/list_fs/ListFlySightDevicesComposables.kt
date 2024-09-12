package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattService
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
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
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightCharacteristic
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.FileInfo
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListFlySightDevicesScreen() {
    val factory = LocalViewModelFactory.current

    val viewModel: ListFlySightDevicesViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visible devices") },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    titleContentColor = MaterialTheme.colorScheme.inverseSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.inverseSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.inverseSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                if (state.bluetoothState == BluetoothService.BluetoothState.NotAvailable)  {
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
                        contentPadding = innerPadding,
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
                                onDeviceClicked = {
                                    viewModel.selectDevice(device)
                                },
                                onRefreshDirectoryContentClicked = {
                                    viewModel.refreshDirectoryContent(device)
                                }
                            )
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
    device: FlySightDevice,
    onDeviceClicked: () -> Unit,
    onRefreshDirectoryContentClicked: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp).clickable {
                onDeviceClicked()
            }
        ) {
            Text("Device: ${device.bluetoothDevice.name}")
            var state by remember { mutableStateOf<FlySightDevice.State>(FlySightDevice.State.Disconnected) }
            var services by remember { mutableStateOf<List<BluetoothGattService>>(emptyList()) }
            var directoryContent by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
            var logs by remember { mutableStateOf<List<String>>(emptyList()) }
            LaunchedEffect(device) {
                device.state.collect {
                    state = it
                }
            }
            LaunchedEffect(device) {
                device.services.collect {
                    services = it
                }
            }
            LaunchedEffect(device) {
                device.directory.collect {
                    directoryContent = it
                }
            }
            LaunchedEffect(device) {
                device.logs.collect {
                    logs = it
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Text("State: $state")
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Services:")
            Spacer(modifier = Modifier.padding(8.dp))
            services.forEach { service ->
                Text("Service: ${service.uuid}")
                service.characteristics.forEach { characteristic ->
                    Text("Characteristic: ${FlySightCharacteristic.fromUuid(characteristic.uuid)?.name}")
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Row {
                Text("Directory content:")
                Spacer(modifier = Modifier.padding(32.dp))
                Button(
                    onClick = {
                        onRefreshDirectoryContentClicked()
                    }
                ) {
                    Text("Refresh")
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            directoryContent.forEach { entry ->
                Text("Entry: ${entry.fileName}")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Logs:")
            Spacer(modifier = Modifier.padding(8.dp))
            logs.forEachIndexed { index, log ->
                Text("($index) $log")
            }
        }
    }
}