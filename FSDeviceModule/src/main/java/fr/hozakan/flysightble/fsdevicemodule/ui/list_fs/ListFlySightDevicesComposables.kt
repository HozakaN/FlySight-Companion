package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.ConfigFileState
import fr.hozakan.flysightble.model.DeviceConnectionState
import fr.hozakan.flysightble.model.FileInfo
import fr.hozakan.flysightble.model.FileState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
        val clickableModifier = if (connectionState == DeviceConnectionState.Connected) {
            Modifier.clickable { onDeviceClicked() }
        } else {
            Modifier
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(192.dp)
                .then(clickableModifier)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(device.name)
                    if (connectionState != DeviceConnectionState.Connecting) {
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        ConnectionIndicator(
                            connectionState = connectionState,
                            flySightDevice = device
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    when (connectionState) {
                        DeviceConnectionState.Connected -> {
                            TextButton(
                                onClick = onConnectionClicked
                            ) {
                                Text(connectionText(connectionState))
                            }
                        }

                        DeviceConnectionState.Disconnected -> {
                            Button(
                                onClick = onConnectionClicked
                            ) {
                                Text(connectionText(connectionState))
                            }
                        }

                        DeviceConnectionState.Connecting -> {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.requiredWidth(8.dp))
                            Text(connectionText(connectionState))
                        }

                        DeviceConnectionState.ConnectionError -> {
                            Text(
                                text = connectionText(connectionState),
                                color = Color.Red
                            )
                        }
                    }
                }
            }
            if (connectionState == DeviceConnectionState.Connected) {
                Spacer(modifier = Modifier.padding(8.dp))

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
//            LaunchedEffect(device) {
//                device.logs.collect {
//                    logs = it
//                }
//            }
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
//            ExpandableColumn(
//                headerComposable = { expanded ->
//                    Text("Logs")
//                },
//                contentComposable = {
//                    Column(
//                        modifier = Modifier.padding(8.dp)
//                    ) {
//                        logs.forEachIndexed { index, log ->
//                            Text("($index) $log")
//                        }
//                    }
//                }
//            )
        }
    }
}

@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
private fun ConnectionIndicator(
    connectionState: DeviceConnectionState,
    flySightDevice: FlySightDevice
) {
    BoxWithConstraints(
        modifier = Modifier.requiredSize(8.dp)
    ) {
        if (connectionState == DeviceConnectionState.Connected) {
            Timber.d(
                "Hoz2 ping ${
                    LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
                }, minHeight = ${minHeight.value}"
            )
            var shouldAnimate by remember { mutableStateOf(false) }
            LaunchedEffect(connectionState, flySightDevice) {
                flySightDevice.ping
                    .collect {
                        shouldAnimate = true
                        delay(1_000)
                        shouldAnimate = false
                    }
            }
            if (shouldAnimate) {
                val infiniteTransition = rememberInfiniteTransition("my_infinite_transition")
                val circleStrokeWidth by with(LocalDensity.current) {
                    infiniteTransition.animateFloat(
                        initialValue = 0.dp.toPx(),
                        targetValue = 2.dp.toPx(),
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1_000),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                }
                val radius by with(LocalDensity.current) {
                    infiniteTransition.animateFloat(
                        initialValue = minHeight.toPx() / 4,
                        targetValue = minHeight.toPx(),
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1_000),
                            repeatMode = RepeatMode.Restart
                        )
                    )
                }
//                animate(
//                    initialValue = 0f,
//                    targetValue = 1f,
//                    animationSpec = tween(durationMillis = 1_000),
//                ) { value, velocity ->
//                    Timber.d("Hoz2 value = $value")
//
//                }
                val animatedAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1_000),
                        repeatMode = RepeatMode.Restart
                    )
                )
                Box(
                    modifier = Modifier.requiredSize(8.dp)
                ) {
                    Canvas(modifier = Modifier.requiredSize(8.dp)) {
                        Timber.d("Hoz2 size.width = ${size.width}, size.height = ${size.height}")
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

fun connectionText(connectionState: DeviceConnectionState): String = when (connectionState) {
    DeviceConnectionState.Connected -> {
        "Disconnect"
    }

    DeviceConnectionState.Disconnected -> {
        "Connect"
    }

    DeviceConnectionState.Connecting -> {
        "Connecting..."
    }

    DeviceConnectionState.ConnectionError -> {
        "Error"
    }
}

@Preview
@Composable
fun FlySightDeviceItemDisconnectedPreview() {
    FlySightDeviceItem(
        device = FakeDeviceImpl(),
        onConnectionClicked = {},
        onDeviceClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectingPreview() {
    FlySightDeviceItem(
        device = FakeDeviceImpl(
            initialConnectionState = DeviceConnectionState.Connecting
        ),
        onConnectionClicked = {},
        onDeviceClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedPreview() {
    FlySightDeviceItem(
        device = FakeDeviceImpl(
            initialConnectionState = DeviceConnectionState.Connected
        ),
        onConnectionClicked = {},
        onDeviceClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemErrorPreview() {
    FlySightDeviceItem(
        device = FakeDeviceImpl(
            initialConnectionState = DeviceConnectionState.ConnectionError
        ),
        onConnectionClicked = {},
        onDeviceClicked = {}
    )
}

private class FakeDeviceImpl(
    initialConnectionState: DeviceConnectionState = DeviceConnectionState.Disconnected
) : FlySightDevice {
    override val uuid: String
        get() = "uuid"
    override val name: String
        get() = "name"
    override val address: String
        get() = "address"
    override val connectionState: StateFlow<DeviceConnectionState> =
        MutableStateFlow(initialConnectionState)
    override val configFile: StateFlow<ConfigFileState>
        get() = MutableStateFlow(ConfigFileState.Nothing)
    override val rawConfigFile: StateFlow<FileState>
        get() = MutableStateFlow(FileState.Nothing)
    override val logs: StateFlow<List<String>>
        get() = MutableStateFlow(emptyList())
    override val fileReceived: SharedFlow<FileState>
        get() = MutableSharedFlow()
    override val ping: SharedFlow<Boolean>
        get() = MutableSharedFlow()

    override suspend fun connectGatt(): Boolean = true

    override suspend fun disconnectGatt(): Boolean = true

    override fun loadDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> =
        MutableStateFlow(emptyList())

    override fun readFile(fileName: String) {}

}
