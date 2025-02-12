package fr.hozakan.flysightcompanion.dialogmodule

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.model.firmware.FirmwareUpdateStatus
import kotlinx.coroutines.flow.StateFlow

data class ConfigFileName(val name: String) : DialogResult
data class PickConfigurationDialogResult(val configFile: ConfigFile) : DialogResult

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
                            onResult(ConfigFileName(configFileName))
                        }
                    )
                }
            }
        }

    }
}

data class PickConfigurationDialog(
    val configProvider: () -> List<ConfigFile>
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
            defaultConfigFile().copy(name = "Config 1"),
            defaultConfigFile().copy(name = "Config 2"),
            defaultConfigFile().copy(name = "Config 3"),
            defaultConfigFile().copy(name = "Config 4"),
        )
    }.Content {}
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
                            FirmwareUpdateStatus.Error -> "An error occurred during the update"
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
                                Icon(
                                    painter = painterResource(R.drawable.usb_flysight_to_phone),
                                    contentDescription = text,
                                )
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
                        FirmwareUpdateStatus.Error -> {
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