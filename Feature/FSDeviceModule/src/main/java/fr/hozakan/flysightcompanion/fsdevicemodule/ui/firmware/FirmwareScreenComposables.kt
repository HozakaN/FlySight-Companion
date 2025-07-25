package fr.hozakan.flysightcompanion.fsdevicemodule.ui.firmware

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.fsdevicemodule.business.DeviceId
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.firmware.FirmwareInfo

@Composable
fun FirmwareScreen(
    deviceId: DeviceId
) {
    val factory = LocalViewModelFactory.current
    val viewModel: FirmwareScreenViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(deviceId) {
        viewModel.initializeWith(deviceId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CurrentVersionsCard(
                    currentAppVersion = state.currentAppVersion,
                    currentFirmwareVersion = state.currentFirmwareVersion,
                    currentStackVersion = state.currentStackVersion
                )
            }

            item {
                CompatibilityMatrixTable(
                    matrix = state.compatibilityMatrix,
                    currentAppVersion = state.currentAppVersion,
                    currentFirmwareVersion = state.currentFirmwareVersion,
                    onUpdateFirmwareClicked = { firmwareInfo ->
                        viewModel.onUpdateFirmwareClicked(firmwareInfo)
                    }
                )
            }
        }
    }
}

@Composable
fun CurrentVersionsCard(
    currentAppVersion: String,
    currentFirmwareVersion: String?,
    currentStackVersion: String?
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FText(
                text = stringResource(R.string.firmware_current_versions),
                configuration = FlySightTheme.typography.cardTitle
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.firmware_app_version))
                Text(
                    text = currentAppVersion,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.firmware_version))
                Text(
                    text = currentFirmwareVersion ?: "Loading",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.stack_version))
                Text(
                    text = currentStackVersion ?: "Loading",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CompatibilityMatrixTable(
    matrix: FirmwareCompatibilityMatrix,
    currentAppVersion: String,
    currentFirmwareVersion: String?,
    onUpdateFirmwareClicked: (FirmwareInfo) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FText(
                    text = stringResource(R.string.firmware_available_firmwares),
                    configuration = FlySightTheme.typography.cardTitle
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.firmware_version_column),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.firmware_compatible_apps_column),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Table Rows
            matrix.firmwares.forEach { firmware ->
                FirmwareCompatibilityRow(
                    firmwareInfo = firmware,
                    currentAppVersion = currentAppVersion,
                    currentFirmwareVersion = currentFirmwareVersion,
                    onUpdateFirmwareClicked = {
                        onUpdateFirmwareClicked(firmware)
                    }
                )
            }
        }
    }
}

@Composable
fun FirmwareCompatibilityRow(
    firmwareInfo: FirmwareInfo,
    currentAppVersion: String,
    currentFirmwareVersion: String?,
    onUpdateFirmwareClicked: () -> Unit
) {
    val isCurrentFirmware = firmwareInfo.name == currentFirmwareVersion

    val borderColor = when {
        isCurrentFirmware -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrentFirmware) 2.dp else 1.dp,
                color = borderColor
            )
            .clickable {
                onUpdateFirmwareClicked()
            }
            .padding(8.dp)
    ) {
        Text(
            text = firmwareInfo.name,
            modifier = Modifier.weight(1f),
            color = if (firmwareInfo.name == currentFirmwareVersion) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (isCurrentFirmware) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(8.dp))
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            firmwareInfo.appCompatibility.forEach { appVersion ->
                Text(
                    text = appVersion,
                    modifier = Modifier.weight(1f),
                    color = if (appVersion == currentAppVersion)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (appVersion == currentAppVersion) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}