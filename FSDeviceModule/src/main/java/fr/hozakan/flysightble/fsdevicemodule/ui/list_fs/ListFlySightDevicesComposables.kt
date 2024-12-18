package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.composablecommons.ExpandableColumn
import fr.hozakan.flysightble.framework.compose.CustomColors
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.ConfigFileState
import fr.hozakan.flysightble.model.DeviceConnectionState
import fr.hozakan.flysightble.model.FileInfo
import fr.hozakan.flysightble.model.FileState
import fr.hozakan.flysightble.model.config.UnitSystem
import fr.hozakan.flysightble.model.defaultConfigFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
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
            if (state.refreshingDeviceList && state.devices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.requiredHeight(24.dp))
                        Text(
                            text = "Refreshing device list...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                }
            } else if (state.devices.isEmpty()) {
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
                            unitSystem = state.unitSystem,
                            onConnectionClicked = {
                                viewModel.connectDevice(device)
                            },
                            onDeviceClicked = {
                                onDeviceSelected(device.device)
                            },
                            onUploadConfigToSystem = {
                                viewModel.uploadConfigToSystem(device)
                            },
                            onUpdateSystemConfClicked = {
                                viewModel.updateSystemConfig(device)
                            },
                            onPushConfigToDeviceClicked = {
                                viewModel.pushConfigToDevice(device)
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
    device: ListFlySightDeviceDisplayData,
    unitSystem: UnitSystem,
    onConnectionClicked: () -> Unit,
    onDeviceClicked: () -> Unit,
    onUploadConfigToSystem: () -> Unit,
    onUpdateSystemConfClicked: () -> Unit,
    onPushConfigToDeviceClicked: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface
        )
    ) {
        val connectionState by device.connectionState.collectAsState()

        val clickableModifier = if (connectionState == DeviceConnectionState.Connected) {
            Modifier.clickable { onDeviceClicked() }
        } else {
            Modifier
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 192.dp)
//                .requiredHeight(192.dp)
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
                            Spacer(modifier = Modifier.requiredWidth(8.dp))
                            ConnectionIndicator(
                                connectionState = connectionState,
                                flySightDevice = device
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = onConnectionClicked
                            ) {
                                Text(connectionText(connectionState))
                            }
                        }
                        val configFileState by device.configFile.collectAsState()
                        FlySightDeviceItemConfigBody(
                            device = device,
                            configFileState = configFileState,
                            unitSystem = unitSystem,
                            onUploadConfigToSystem = onUploadConfigToSystem,
                            onUpdateSystemConfClicked = onUpdateSystemConfClicked,
                            onPushConfigToDeviceClicked = onPushConfigToDeviceClicked
                        )
                    }
                }

                DeviceConnectionState.Connecting -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleLarge
                        )
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
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleLarge
                        )
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
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleLarge
                        )
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
}

@Composable
fun FlySightDeviceItemConfigBody(
    device: ListFlySightDeviceDisplayData,
    configFileState: ConfigFileState,
    unitSystem: UnitSystem,
    onUploadConfigToSystem: () -> Unit,
    onUpdateSystemConfClicked: () -> Unit,
    onPushConfigToDeviceClicked: () -> Unit
) {
    ExpandableColumn(
        headerComposable = {
            when (configFileState) {
                is ConfigFileState.Error -> {
                    Text("Error loading device configuration")
                }

                ConfigFileState.Loading -> {
                    Text("Configuration loading...")
                }

                ConfigFileState.Nothing -> {}
                is ConfigFileState.Success -> {
                    val warning = /*!device.isConfigFromSystem ||*/ device.hasConfigContentChanged
                    Text("Config name: ${configFileState.config.name.ifBlank { "No name" }}")
                    if (warning) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "There is issues with the configuration",
                            tint = CustomColors.Orange
                        )
                    }
                }
            }
        },
        isExpandable = configFileState is ConfigFileState.Success
    ) {
        if (!device.isConfigFromSystem) {
            Button(
                onClick = onUploadConfigToSystem
            ) {
                Text("Upload to my config files")
            }
        } else if (device.hasConfigContentChanged) {
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Text(
                text = "The FlySight configuration differs from your local config file named ${configFileState.conf?.name}",
                color = CustomColors.Orange
            )
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Row {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onUpdateSystemConfClicked
                ) {
                    Text("Update local config")
                }
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onPushConfigToDeviceClicked
                ) {
                    Text("Update FlySight config")
                }
            }
        } else {
            when (configFileState) {
                is ConfigFileState.Error -> {}
                ConfigFileState.Loading -> {}
                ConfigFileState.Nothing -> {}
                is ConfigFileState.Success -> {
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Text(
                        "Dropzone altitude : ${configFileState.config.dzElev} ${unitSystem.distanceText}",
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Text(
                        "${configFileState.config.speeches.size} speeches",
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Text(
                        "${configFileState.config.alarms.size} alarms",
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    Text(
                        "${configFileState.config.silenceWindows.size} silence windows",
                    )
                }
            }
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
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Disconnected
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = ConfigFileState.Nothing
        ),
        unitSystem = UnitSystem.Metric,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectingPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connecting
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = ConfigFileState.Nothing
        ),
        unitSystem = UnitSystem.Metric,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndNominalConfigFilePreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                configFileName = "Distance Le Puy"

            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = ConfigFileState.Nothing
        ),
        unitSystem = UnitSystem.Metric,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileUnknownPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected
            ),
            isConfigFromSystem = false,
            hasConfigContentChanged = false,
            deviceConfig = ConfigFileState.Nothing
        ),
        unitSystem = UnitSystem.Metric,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileDiffersPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                configFileName = "Speed Corbas"
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = true,
            deviceConfig = ConfigFileState.Nothing
        ),
        unitSystem = UnitSystem.Metric,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemErrorPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.ConnectionError
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = ConfigFileState.Nothing
        ),
        unitSystem = UnitSystem.Metric,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {}
    )
}

private class FakeDeviceImpl(
    initialConnectionState: DeviceConnectionState = DeviceConnectionState.Disconnected,
    private val configFileName: String = "",
    override val name: String = "Fake device"
) : FlySightDevice {
    override val uuid: String
        get() = "uuid"
    override val address: String
        get() = "address"
    override val connectionState: StateFlow<DeviceConnectionState> =
        MutableStateFlow(initialConnectionState)
    override val configFile: StateFlow<ConfigFileState>
        get() = MutableStateFlow(ConfigFileState.Success(defaultConfigFile().copy(name = configFileName)))
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
    override fun updateConfigFile(configFile: ConfigFile) {}

}
