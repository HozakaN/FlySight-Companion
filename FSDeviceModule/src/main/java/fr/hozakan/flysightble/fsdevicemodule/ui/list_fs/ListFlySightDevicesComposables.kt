package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import android.annotation.SuppressLint
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightble.composablecommons.SimpleDialogActionBar3Button
import fr.hozakan.flysightble.composablecommons.SimpleVerticalDialogActionBar
import fr.hozakan.flysightble.framework.compose.CustomColors
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.framework.service.loading.LoadingState
import fr.hozakan.flysightble.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightble.model.ConfigFile
import fr.hozakan.flysightble.model.ConfigFileState
import fr.hozakan.flysightble.model.DeviceConnectionState
import fr.hozakan.flysightble.model.FileInfo
import fr.hozakan.flysightble.model.FileState
import fr.hozakan.flysightble.model.config.UnitSystem
import fr.hozakan.flysightble.model.defaultConfigFile
import fr.hozakan.flysightble.model.result.ResultFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale

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
                            updatingConfiguration = state.updatingConfiguration == device.uuid,
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
                            },
                            onChangeDeviceConfigurationClicked = {
                                viewModel.changeDeviceConfiguration(device)
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
    updatingConfiguration: Boolean,
    onConnectionClicked: () -> Unit,
    onDeviceClicked: () -> Unit,
    onUploadConfigToSystem: () -> Unit,
    onUpdateSystemConfClicked: () -> Unit,
    onPushConfigToDeviceClicked: () -> Unit,
    onChangeDeviceConfigurationClicked: () -> Unit
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
                            updatingConfiguration = updatingConfiguration,
                            configFileState = configFileState,
                            unitSystem = unitSystem,
                            onUploadConfigToSystem = onUploadConfigToSystem,
                            onUpdateSystemConfClicked = onUpdateSystemConfClicked,
                            onPushConfigToDeviceClicked = onPushConfigToDeviceClicked,
                            onChangeDeviceConfigurationClicked = onChangeDeviceConfigurationClicked
                        )
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
                            .fillMaxWidth()
                            .requiredHeight(height = 192.dp)
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
                            .fillMaxWidth()
                            .requiredHeight(height = 192.dp)
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
fun DeviceResultFilesContainer(
    modifier: Modifier,
    device: ListFlySightDeviceDisplayData
) {
    val locales = LocalContext.current.resources.configuration.locales
    val locale = if (locales.isEmpty) Locale.ROOT else locales[0]
    val dateTimeFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
    }
    val resultFiles by device.resultFiles.collectAsState()
    Column(
        modifier = modifier.padding(8.dp)
    ) {
        Text(
            text = "Results",
            style = MaterialTheme.typography.titleMedium
        )
        when (val files = resultFiles) {
            is LoadingState.Error<List<ResultFile>> -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Error loading result files"
                    )
                }
            }

            is LoadingState.Loaded<List<ResultFile>> -> {
                val mostRecentFile = files.value.maxByOrNull { it.dateTime }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "result files",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(text = "${files.value.size}")
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                    Text(
                        text = "Most recent run",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(text = "${mostRecentFile?.dateTime?.format(dateTimeFormatter)}")
                }
            }

            LoadingState.Idle,
            is LoadingState.Loading<List<ResultFile>> -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Loading result files...")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlySightDeviceItemConfigBody(
    device: ListFlySightDeviceDisplayData,
    updatingConfiguration: Boolean,
    configFileState: ConfigFileState,
    unitSystem: UnitSystem,
    onUploadConfigToSystem: () -> Unit,
    onUpdateSystemConfClicked: () -> Unit,
    onPushConfigToDeviceClicked: () -> Unit,
    onChangeDeviceConfigurationClicked: () -> Unit
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
            .background(MaterialTheme.colorScheme.onSurface)
        DeviceConfigurationContainer(
            modifier = itemModifier,
            configFileState = configFileState,
            updatingConfiguration = updatingConfiguration,
            device = device,
            onUploadConfigToSystem = onUploadConfigToSystem,
            onUpdateSystemConfClicked = onUpdateSystemConfClicked,
            onPushConfigToDeviceClicked = onPushConfigToDeviceClicked,
            onChangeDeviceConfigurationClicked = onChangeDeviceConfigurationClicked,
            unitSystem = unitSystem
        )
        DeviceResultFilesContainer(
            modifier = itemModifier,
            device = device
        )
    }
}

@Composable
private fun DeviceConfigurationContainer(
    modifier: Modifier = Modifier,
    configFileState: ConfigFileState,
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
    val mod = if (warning && configFileState is ConfigFileState.Success)  {
        Modifier.clickable {
            warningDialogOpened = true
        }.padding(8.dp)
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
                text = "Configuration",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            if (!updatingConfiguration) {
                IconButton(
                    modifier = Modifier.requiredSize(24.dp),
                    onClick = {
                        menuOpened = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Device menu"
                    )
                }
                DropdownMenu(
                    expanded = menuOpened,
                    onDismissRequest = { menuOpened = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Change configuration") },
                        onClick = {
                            menuOpened = false
                            onChangeDeviceConfigurationClicked()
                        }
                    )
                }
            }
        }
        if (updatingConfiguration) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Updating configuration...")
            }
            return
        }
        when (configFileState) {
            is ConfigFileState.Error -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Error loading device configuration")
                }
            }

            ConfigFileState.Loading -> {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Configuration loading...")
                }
            }

            ConfigFileState.Nothing -> {}
            is ConfigFileState.Success -> {
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                if (warning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = configFileState.config.name.ifBlank { "No name" })
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            modifier = Modifier.requiredSize(24.dp),
                            imageVector = Icons.Default.Warning,
                            contentDescription = "There is issues with the configuration",
                            tint = CustomColors.Orange
                        )
                    }
                } else {
                    Text(text = configFileState.config.name.ifBlank { "No name" })
                }
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                Text(
                    "Elevation : ${configFileState.config.dzElev} ${unitSystem.distanceText}",
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
private fun DeviceConfigurationMisMatchDialog(
    configFileState: ConfigFileState,
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
                        text = "Would you like to upload this device configuration on your phone ?",
                        color = CustomColors.Orange
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    SimpleDialogActionBar(
                        onDismissRequest = onDismissRequest,
                        onValidate = onUploadConfigToSystem,
                        validateButtonText = "Upload".uppercase()
                    )
                } else if (device.hasConfigContentChanged) {
                    Text(
                        text = "The FlySight configuration differs from your local config file named ${configFileState.conf?.name}",
                        color = CustomColors.Orange
                    )
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                    SimpleVerticalDialogActionBar(
                        onDismissRequest = onDismissRequest,
                        onNeutral = onUpdateSystemConfClicked,
                        onValidate = onPushConfigToDeviceClicked,
                        validateButtonText = "Update FlySight conf".uppercase(),
                        neutralButtonText = "Update local conf".uppercase(),
                        cancelButtonText = "Don't do anything".uppercase()
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
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {}
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
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndNominalConfigFilePreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                configFileName = "Distance Le Puy",
                initialConfigFileState = ConfigFileState.Success(defaultConfigFile().copy(name = "Distance Le Puy"))
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = ConfigFileState.Nothing
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileUnknownPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialConfigFileState = ConfigFileState.Success(defaultConfigFile().copy(name = "Distance Le Puy"))
            ),
            isConfigFromSystem = false,
            hasConfigContentChanged = false,
            deviceConfig = ConfigFileState.Nothing
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileDiffersPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialResultFileState = LoadingState.Loaded(
                    listOf(
                        ResultFile(
                            "result1",
                            LocalDateTime.now()
                        )
                    )
                ),
                configFileName = "Speed Corbas",
                initialConfigFileState = ConfigFileState.Success(defaultConfigFile().copy(name = "Speed Corbas"))
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = true,
            deviceConfig = ConfigFileState.Success(defaultConfigFile().copy(name = "Speed Corbas"))
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileLoadingPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialResultFileState = LoadingState.Loaded(
                    listOf(
                        ResultFile(
                            "result1",
                            LocalDateTime.now()
                        )
                    )
                ),
                initialConfigFileState = ConfigFileState.Loading,
                configFileName = "Speed Corbas"
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = true,
            deviceConfig = ConfigFileState.Loading
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndUpdatingConfigurationPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialResultFileState = LoadingState.Loaded(
                    listOf(
                        ResultFile(
                            "result1",
                            LocalDateTime.now()
                        )
                    )
                ),
                initialConfigFileState = ConfigFileState.Loading,
                configFileName = "Speed Corbas"
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = true,
            deviceConfig = ConfigFileState.Loading
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = true,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {}
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
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {}
    )
}

@Preview
@Composable
fun DeviceConfigurationMisMatchDialogWithConfigContentChangedPreview() {
    DeviceConfigurationMisMatchDialog(
        configFileState = ConfigFileState.Success(defaultConfigFile().copy(name = "Speed Corbas")),
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialResultFileState = LoadingState.Loaded(
                    listOf(
                        ResultFile(
                            "result1",
                            LocalDateTime.now()
                        )
                    )
                ),
                configFileName = "Speed Corbas"
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = true,
            deviceConfig = ConfigFileState.Success(defaultConfigFile().copy(name = "Speed Corbas"))
        ),
        onDismissRequest = {},
        onUploadConfigToSystem = {},
        onUpdateSystemConfClicked = {},
        onPushConfigToDeviceClicked = {}
    )
}

@Preview
@Composable
fun DeviceConfigurationMisMatchDialogWithConfigNotFromSystemPreview() {
    DeviceConfigurationMisMatchDialog(
        configFileState = ConfigFileState.Success(defaultConfigFile().copy(name = "Speed Corbas")),
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                configFileName = "Speed Corbas"
            ),
            isConfigFromSystem = false,
            hasConfigContentChanged = false,
            deviceConfig = ConfigFileState.Success(defaultConfigFile().copy(name = "Speed Corbas"))
        ),
        onDismissRequest = {},
        onUploadConfigToSystem = {},
        onUpdateSystemConfClicked = {},
        onPushConfigToDeviceClicked = {}
    )
}

private class FakeDeviceImpl(
    initialConnectionState: DeviceConnectionState = DeviceConnectionState.Disconnected,
    initialResultFileState: LoadingState<List<ResultFile>> = LoadingState.Idle,
    initialConfigFileState: ConfigFileState = ConfigFileState.Nothing,
    private val configFileName: String = "",
    override val name: String = "Fake device"
) : FlySightDevice {
    override val uuid: String
        get() = "uuid"
    override val address: String
        get() = "address"
    override val connectionState: StateFlow<DeviceConnectionState> =
        MutableStateFlow(initialConnectionState)
    override val configFile: StateFlow<ConfigFileState> = MutableStateFlow(initialConfigFileState)
    override val rawConfigFile: StateFlow<FileState>
        get() = MutableStateFlow(FileState.Nothing)
    override val resultFiles: StateFlow<LoadingState<List<ResultFile>>> =
        MutableStateFlow(initialResultFileState).asStateFlow()
    override val logs: StateFlow<List<String>>
        get() = MutableStateFlow(emptyList())
    override val fileReceived: SharedFlow<FileState>
        get() = MutableSharedFlow()
    override val ping: SharedFlow<Boolean>
        get() = MutableSharedFlow()

    override suspend fun connectGatt(): Boolean = true

    override suspend fun disconnectGatt(): Boolean = true

    override fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> =
        MutableStateFlow(emptyList())

    override suspend fun loadDirectory(directoryPath: List<String>): List<FileInfo> {
        return emptyList()
    }

    override suspend fun readFile(fileName: String) {}
    override suspend fun updateConfigFile(configFile: ConfigFile) {}

}
