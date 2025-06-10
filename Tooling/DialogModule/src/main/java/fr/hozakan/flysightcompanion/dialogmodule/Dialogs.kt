package fr.hozakan.flysightcompanion.dialogmodule

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import fr.hozakan.flysightcompanion.model.DisplayableConfig
import fr.hozakan.flysightcompanion.model.firmware.FirmwareUpdateStatus
import fr.hozakan.flysightcompanion.model.session.profile.Coordinate
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class ConfigFileNameDialogResult(val name: String) : DialogResult
data class PickConfigurationDialogResult(val configFile: DisplayableConfig) : DialogResult
data class CreateReferencePointDialogResult(val referencePoint: ReferencePoint) : DialogResult

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
                            FirmwareUpdateStatus.Pushing -> "Pushing firmware to the FlySight..."
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
                            }

                            is FirmwareUpdateStatus.PushingWithAmount -> {
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
                        }
                    }
                    when (firmwareUpdateState) {
                        FirmwareUpdateStatus.Downloading,
                        FirmwareUpdateStatus.Pushing,
                        is FirmwareUpdateStatus.PushingWithAmount,
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