package fr.hozakan.flysightcompanion.dialogmodule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DisplayableConfig
import fr.hozakan.flysightcompanion.model.firmware.FirmwareUpdateStatus
import fr.hozakan.flysightcompanion.model.session.profile.Coordinate
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class ConfigFileNameDialogResult(val name: String) : DialogResult
data class PickConfigurationDialogResult(val configFile: DisplayableConfig) : DialogResult
data class CreateReferencePointDialogResult(val referencePoint: ReferencePoint) : DialogResult

data class AddFlySightDialog(
    private val scanFlow: Flow<LoadingState<List<Pair<String, String>>>>,
    private val deviceClicked: (String) -> Unit,
    private val connectionStateFlow: StateFlow<DeviceConnectionState>
) : DialogItem {

    @Composable
    private fun DeviceItem(
        device: Pair<String, String>,
        isSelected: Boolean,
        connectionState: DeviceConnectionState,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .defaultMinSize(minWidth = 300.dp)
                .clickable {
                    onClick()
                }
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = when {
                connectionState is DeviceConnectionState.Connecting -> CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                )

                isSelected -> CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )

                else -> CardDefaults.cardColors()
            }
        ) {
            Row(
                modifier = Modifier
                    .defaultMinSize(minWidth = 200.dp)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth Device",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.requiredWidth(16.dp))

                Column {
                    Text(
                        text = device.first ?: "Unknown Device",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.requiredHeight(4.dp))
                    Text(
                        text = device.second ?: "Unknown Address",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {
        Dialog(
            onDismissRequest = {
                onResult(DialogResult.Dismiss)
            }
        ) {
            val deviceState by scanFlow.collectAsState(initial = LoadingState.Loading(emptyList()))
            var connectionState by remember {
                mutableStateOf<DeviceConnectionState>(
                    DeviceConnectionState.Disconnected
                )
            }
            var selectedItem by remember { mutableStateOf<Int?>(null) }

            LaunchedEffect(Unit) {
                connectionStateFlow.collect { state ->
                    connectionState = state
                    if (state is DeviceConnectionState.Connected) {
                        onResult(OkDialogResult)
                    } else if (state !is DeviceConnectionState.Connecting) {
                        selectedItem = null
                    }
                }
            }

            Card {
                Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 300.dp, minHeight = 400.dp)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FText(
                            text = "Select a FlySight",
                            configuration = FlySightTheme.typography.cardTitle
                        )
                        if (deviceState is LoadingState.Loading) {
                            Spacer(modifier = Modifier.requiredWidth(16.dp))
                            CircularProgressIndicator()
                        }
                    }
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    when (val state = deviceState) {
                        is LoadingState.Error -> {}
                        LoadingState.Idle -> {}
                        is LoadingState.Loading,
                        is LoadingState.Loaded -> {
                            val devices = state.content ?: return@Column
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(devices) { index, device ->
                                    DeviceItem(
                                        device = device,
                                        isSelected = selectedItem == index,
                                        connectionState = connectionState,
                                        onClick = {
                                            selectedItem = index
                                            deviceClicked(device.second)
                                        }
                                    )
                                }
                            }
                        }
                    }
//                    SimpleDialogActionBar(
//                        onCancel = {
//                            onResult(DialogResult.Dismiss)
//                        },
//                        showValidateButton = false
//                    )
                }
            }

//            LaunchedEffect(Unit) {
//                awaitMechanism()
//                onResult(OkDialogResult)
//            }
        }
    }
}

data class AwaitFlySightDeviceModeDialog(
    val awaitPowerOn: Boolean = true,
    private val awaitMechanism: suspend () -> Unit
) : DialogItem {
    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {
        Dialog(
            onDismissRequest = {
                onResult(DialogResult.Dismiss)
            }
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    FText(
                        "Please power ${
                            if (awaitPowerOn) {
                                "on"
                            } else {
                                "off"
                            }
                        } your FlySight"
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    SimpleDialogActionBar(
                        onCancel = {
                            onResult(DialogResult.Dismiss)
                        },
                        showValidateButton = false
                    )
                }
            }

            LaunchedEffect(Unit) {
                awaitMechanism()
                onResult(OkDialogResult)
            }
        }
    }
}

data class ConfigFileNameDialog(
    private val name: String? = null
) : DialogItem {

    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {
        Dialog(
            onDismissRequest = {
                onResult(DialogResult.Dismiss)
            }
        ) {
            var configFileName by remember { mutableStateOf(name ?: "") }
            var isDirty by remember { mutableStateOf(false) }
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = configFileName,
                        onValueChange = {
                            configFileName = it
                            isDirty = true
                        },
                        label = {
                            Text(text = "Config name")
                        },
                        isError = isDirty && configFileName.isBlank()
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    SimpleDialogActionBar(
                        onCancel = {
                            onResult(DialogResult.Dismiss)
                        },
                        validateEnabled = configFileName.isNotBlank(),
                        onValidate = {
                            onResult(ConfigFileNameDialogResult(configFileName))
                        }
                    )
                }
            }
        }
    }
}

data class PickConfigurationDialog(
    val configProvider: () -> List<DisplayableConfig>
) : DialogItem {
    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {
        Dialog(
            onDismissRequest = {
                onResult(DialogResult.Dismiss)
            }
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.dialog_pick_config_title),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(configProvider()) { configFile ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .requiredHeight(40.dp)
                                    .clickable { onResult(PickConfigurationDialogResult(configFile)) }
                                    .padding(start = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = configFile.name
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PickConfigurationDialogPreview() {
    PickConfigurationDialog {
        listOf(
            object : DisplayableConfig {
                override val name: String
                    get() = "Config 1"

            },
            object : DisplayableConfig {
                override val name: String
                    get() = "Config 2"

            },
            object : DisplayableConfig {
                override val name: String
                    get() = "Config 3"

            },
            object : DisplayableConfig {
                override val name: String
                    get() = "Config 4"

            }
        )
    }.Content {}
}

data object HudWarningDialog : DialogItem {
    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {
        Dialog(
            onDismissRequest = {
                onResult(DialogResult.Dismiss)
            }
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    FText(
                        modifier = Modifier.fillMaxWidth(),
                        text = "⚠\uFE0F WARNING ⚠\uFE0F",
                        configuration = FlySightTheme.typography.cardTitle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = """
                            Using HUD goggles while flying is DANGEROUS and can be life-threatening.
                            
                            • Goggles can obstruct your peripheral vision
                            • Screen glare may impair depth perception
                            • Electronic displays can malfunction or fail
                            • Distraction from instruments increases crash risk
                            
                            Use only with extreme caution and proper training.
                            Your safety is your responsibility.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                onResult(DialogResult.Dismiss)
                            }
                        ) {
                            FText(
                                text = "Don't run session",
                            )
                        }
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        TextButton(
                            onClick = {
                                onResult(OkDialogResult)
                            }
                        ) {
                            FText(
                                text = "I understand and still want to launch the session",
                            )
                        }
                    }
                }
            }
        }
    }
}

class CreateReferencePointDialog : DialogItem {

    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {
        Dialog(
            onDismissRequest = {
                onResult(DialogResult.Dismiss)
            }
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    var isDirty by remember { mutableStateOf(false) }
                    var name by remember { mutableStateOf("") }
                    var description by remember { mutableStateOf("") }
                    var latitudeText by remember { mutableStateOf("45.077200") }
                    var longitudeText by remember { mutableStateOf("3.761141") }

                    fun isValid(): Boolean {
                        return name.isNotBlank() &&
                                description.isNotBlank() &&
                                latitudeText.toDoubleOrNull() != null &&
                                longitudeText.toDoubleOrNull() != null
                    }

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name,
                        onValueChange = {
                            if (it.length <= 5) {
                                name = it
                                isDirty = true
                            }
                        },
                        label = {
                            Text(text = "Name")
                        },
                        isError = isDirty && name.isBlank()
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = description,
                        onValueChange = {
                            description = it
                            isDirty = true
                        },
                        label = {
                            Text(text = "Description")
                        },
                        isError = isDirty && description.isBlank()
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = latitudeText,
                        onValueChange = {
                            latitudeText = it
                            isDirty = true
                        },
                        label = {
                            Text(text = "Latitude")
                        },
//                        isError = false // isDirty && latitude.isBlank()
                        isError = isDirty && latitudeText.toDoubleOrNull() == null
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = longitudeText,
                        onValueChange = {
                            longitudeText = it
                            isDirty = true
                        },
                        label = {
                            Text(text = "Longitude")
                        },
//                        isError = false // isDirty && longitude.isBlank()
                        isError = isDirty && longitudeText.toDoubleOrNull() == null
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    SimpleDialogActionBar(
                        onCancel = {
                            onResult(DialogResult.Dismiss)
                        },
                        validateEnabled = isValid(),
                        onValidate = {
                            onResult(
                                CreateReferencePointDialogResult(
                                    ReferencePoint(
                                        id = UUID.randomUUID().toString(),
                                        name = name,
                                        description = description,
                                        coords = Coordinate(
                                            latitude = latitudeText.toDouble(),
                                            longitude = longitudeText.toDouble()
                                        )
                                    )
                                )
                            )
                        }
                    )
                }
            }
        }
    }

}

@Preview
@Composable
fun CreateReferencePointDialogPreview() {
    CreateReferencePointDialog().Content { }
}

data class UpdateFirmwareDialog(
    val firmwareUpdateFlow: StateFlow<FirmwareUpdateStatus>
) : DialogItem {
    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {

        val firmwareUpdateState by firmwareUpdateFlow.collectAsState()

        Dialog(
            onDismissRequest = {}
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val text = remember(firmwareUpdateState) {
                        when (val state = firmwareUpdateState) {
                            FirmwareUpdateStatus.Downloading -> "Downloading firmware from internet..."
                            FirmwareUpdateStatus.PushingFirmware -> "Pushing firmware to the FlySight..."
                            FirmwareUpdateStatus.DisconnectingFromBluetooth,
                            FirmwareUpdateStatus.AwaitingUsbConnection -> "Connect the FlySight to the phone through USB..."

                            FirmwareUpdateStatus.AwaitingButtonPush -> "Press the power button until the LED becomes orange"
                            FirmwareUpdateStatus.DisconnectingFromUsb -> "Disconnect the FlySight from the phone"

                            FirmwareUpdateStatus.AwaitingBluetoothReconnection -> "Reconnecting through Bluetooth..."
                            FirmwareUpdateStatus.FirmwareVersionCheck -> "Checking firmware version..."
                            FirmwareUpdateStatus.NoUpdate -> "No update available"
                            FirmwareUpdateStatus.Done -> "You FlySight has been updated!"
                            is FirmwareUpdateStatus.Error -> when (state.errorInfo) {
                                FirmwareUpdateStatus.ErrorInfo.CantReconnect -> "Can't reconnect to the FlySight"
                                FirmwareUpdateStatus.ErrorInfo.DownloadError -> "Error while downloading the firmware"
                                FirmwareUpdateStatus.ErrorInfo.FirmwareVersionCheckError -> "Error while checking the firmware version"
                                FirmwareUpdateStatus.ErrorInfo.FirmwareVersionCheckTimeOut -> """
                                    Timeout while checking the firmware version.
                                    Reconnect to check if it has been updated.
                                """.trimIndent()

                                FirmwareUpdateStatus.ErrorInfo.PushFirmwareError -> "Error while pushing the firmware"
                                FirmwareUpdateStatus.ErrorInfo.Unknown -> "An error occurred"
                                FirmwareUpdateStatus.ErrorInfo.IncompatibleAppVersion -> "This firmware is not compatible with this app version."
                                FirmwareUpdateStatus.ErrorInfo.AlreadyUpToDate -> "Already up to date"
                                FirmwareUpdateStatus.ErrorInfo.PushStackError -> "Error while pushing the stack"
                            }

                            is FirmwareUpdateStatus.PushingFirmwareWithAmount -> {
                                val factor = if (state.maxValue > 1_000_000) 1_000_000 else 1_000
                                val maxValueText = if (state.maxValue > 1_000_000) {
                                    "${state.maxValue / factor} MB"
                                } else {
                                    "${state.maxValue / factor} KB"
                                }
                                """
                                    Pushing firmware to the FlySight...
                                    ${state.currentValue / factor} / $maxValueText
                                """.trimIndent()
                            }

                            FirmwareUpdateStatus.PushingStack -> "Pushing the stack to the FlySight..."
                            is FirmwareUpdateStatus.PushingStackWithAmount -> {
                                val factor = if (state.maxValue > 1_000_000) 1_000_000 else 1_000
                                val maxValueText = if (state.maxValue > 1_000_000) {
                                    "${state.maxValue / factor} MB"
                                } else {
                                    "${state.maxValue / factor} KB"
                                }
                                """
                                    Pushing the stack to the FlySight...
                                    ${state.currentValue / factor} / $maxValueText
                                """.trimIndent()
                            }
                        }
                    }
                    when (firmwareUpdateState) {
                        FirmwareUpdateStatus.Downloading,
                        FirmwareUpdateStatus.PushingFirmware,
                        FirmwareUpdateStatus.PushingStack,
                        is FirmwareUpdateStatus.PushingFirmwareWithAmount,
                        is FirmwareUpdateStatus.PushingStackWithAmount,
                        FirmwareUpdateStatus.DisconnectingFromBluetooth -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.requiredWidth(8.dp))
                                FText(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = text,
                                    configuration = FlySightTheme.typography.cardTitle,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        FirmwareUpdateStatus.AwaitingUsbConnection,
                        FirmwareUpdateStatus.AwaitingButtonPush,
                        FirmwareUpdateStatus.DisconnectingFromUsb -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.usb_flysight_to_phone),
                                    contentDescription = text,
                                )
//                                Icon(
//                                    painter = painterResource(R.drawable.usb_flysight_to_phone),
//                                    contentDescription = text,
//                                )
                                Spacer(modifier = Modifier.requiredHeight(16.dp))
                                FText(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = text,
                                    configuration = FlySightTheme.typography.cardTitle,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        FirmwareUpdateStatus.AwaitingBluetoothReconnection,
                        FirmwareUpdateStatus.FirmwareVersionCheck -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.requiredWidth(8.dp))
                                FText(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = text,
                                    configuration = FlySightTheme.typography.cardTitle,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        FirmwareUpdateStatus.NoUpdate,
                        FirmwareUpdateStatus.Done,
                        is FirmwareUpdateStatus.Error -> {
                            FText(
                                text = text,
                                configuration = FlySightTheme.typography.cardTitle,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.requiredHeight(16.dp))
                            SimpleDialogActionBar(
                                showCancelButton = false,
                                validateButtonText = stringResource(R.string.misc_ok),
                                onValidate = {
                                    onResult(DialogResult.Dismiss)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}