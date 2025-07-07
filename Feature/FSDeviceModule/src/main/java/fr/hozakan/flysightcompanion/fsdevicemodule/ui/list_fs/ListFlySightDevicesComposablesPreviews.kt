package fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightCompanionTheme
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.fsdevicemodule.business.MutableFlySightDevice
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.defaultConfigFile
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.model.records.RecordFile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

@Preview
@Composable
fun ListFlySightDevicesScreenInternalRefreshingWithoutDevicePreview() {
    ListFlySightDevicesScreenInternal(
        state = ListFlySightDevicesState(
            hasBluetoothPermission = true,
            bluetoothState = BluetoothService.BluetoothState.Available,
            devices = emptyList(),
            refreshingDeviceList = LoadingState.Loading(Unit),
            unitSystem = UnitSystem.Metric,
            updatingConfiguration = null,
        ),
        onDeviceSelected = {},
        onRequestBluetoothPermissionClicked = {},
        onEnableBluetoothClicked = {},
        onCancelScanClicked = {},
        refreshBluetoothDeviceListClicked = {},
        onAddDeviceClicked = {},
        onConnectDeviceClicked = {},
        onUploadConfigToSystemClicked = {},
        onUpdateSystemConfigClicked = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun ListFlySightDevicesScreenInternalRefreshingWithoutDeviceStep2Preview() {
    ListFlySightDevicesScreenInternal(
        state = ListFlySightDevicesState(
            hasBluetoothPermission = true,
            bluetoothState = BluetoothService.BluetoothState.Available,
            devices = emptyList(),
            refreshingDeviceList = LoadingState.Loading(increment = 1),
            unitSystem = UnitSystem.Metric,
            updatingConfiguration = null
        ),
        onDeviceSelected = {},
        onRequestBluetoothPermissionClicked = {},
        onEnableBluetoothClicked = {},
        onCancelScanClicked = {},
        refreshBluetoothDeviceListClicked = {},
        onAddDeviceClicked = {},
        onConnectDeviceClicked = {},
        onUploadConfigToSystemClicked = {},
        onUpdateSystemConfigClicked = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun ListFlySightDevicesScreenInternalRefreshingWithoutDeviceStep3Preview() {
    ListFlySightDevicesScreenInternal(
        state = ListFlySightDevicesState(
            hasBluetoothPermission = true,
            bluetoothState = BluetoothService.BluetoothState.Available,
            devices = emptyList(),
            refreshingDeviceList = LoadingState.Loading(increment = 2),
            unitSystem = UnitSystem.Metric,
            updatingConfiguration = null
        ),
        onDeviceSelected = {},
        onRequestBluetoothPermissionClicked = {},
        onEnableBluetoothClicked = {},
        onCancelScanClicked = {},
        refreshBluetoothDeviceListClicked = {},
        onAddDeviceClicked = {},
        onConnectDeviceClicked = {},
        onUploadConfigToSystemClicked = {},
        onUpdateSystemConfigClicked = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun ListFlySightDevicesScreenInternalNoDevicePreview() {
    ListFlySightDevicesScreenInternal(
        state = ListFlySightDevicesState(
            hasBluetoothPermission = true,
            bluetoothState = BluetoothService.BluetoothState.Available,
            devices = emptyList(),
            refreshingDeviceList = LoadingState.Idle,
            unitSystem = UnitSystem.Metric,
            updatingConfiguration = null
        ),
        onDeviceSelected = {},
        onRequestBluetoothPermissionClicked = {},
        onEnableBluetoothClicked = {},
        onCancelScanClicked = {},
        refreshBluetoothDeviceListClicked = {},
        onAddDeviceClicked = {},
        onConnectDeviceClicked = {},
        onUploadConfigToSystemClicked = {},
        onUpdateSystemConfigClicked = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview(device = "id:4.65in 720p (Galaxy Nexus)", showSystemUi = true)
@Preview(device = "id:Nexus One", showSystemUi = true)
@Preview(device = "id:pixel_9_pro", showSystemUi = true)
@Preview(device = "id:pixel_9_pro_fold", showSystemUi = true)
@Preview(device = "id:pixel_tablet", showSystemUi = true)
@Composable
fun ListFlySightDevicesScreenInternalWithDevicePreview() {
    ListFlySightDevicesScreenInternal(
        state = ListFlySightDevicesState(
            hasBluetoothPermission = true,
            bluetoothState = BluetoothService.BluetoothState.Available,
            devices = listOf(
                ListFlySightDeviceDisplayData(
                    device =
                        FakeDeviceDelegateImpl(
                            initialConnectionState = DeviceConnectionState.Disconnected,
                            initialRecordFileState = LoadingState.Loaded(
                                emptyList()
                            ),
                            initialConfigFileState = LoadingState.Idle,
                            configFileName = "Speed Corbas",
                            name = "Fake device 1"
                        ),
                    deviceConfig = LoadingState.Idle,
                    isConfigFromSystem = false,
                    hasConfigContentChanged = false
                )
            ),
            refreshingDeviceList = LoadingState.Idle,
            unitSystem = UnitSystem.Metric,
            updatingConfiguration = null
        ),
        onDeviceSelected = {},
        onRequestBluetoothPermissionClicked = {},
        onEnableBluetoothClicked = {},
        onCancelScanClicked = {},
        refreshBluetoothDeviceListClicked = {},
        onAddDeviceClicked = {},
        onConnectDeviceClicked = {},
        onUploadConfigToSystemClicked = {},
        onUpdateSystemConfigClicked = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview(device = "id:4.65in 720p (Galaxy Nexus)", showSystemUi = true)
@Preview(device = "id:Nexus One", showSystemUi = true)
@Preview(device = "id:pixel_9_pro", showSystemUi = true)
@Preview(device = "id:pixel_9_pro_fold", showSystemUi = true)
@Preview(device = "id:pixel_tablet", showSystemUi = true)
@Composable
fun ListFlySightDevicesScreenInternalWithDeviceConnectedPreview() {
    ListFlySightDevicesScreenInternal(
        state = ListFlySightDevicesState(
            hasBluetoothPermission = true,
            bluetoothState = BluetoothService.BluetoothState.Available,
            devices = listOf(
                ListFlySightDeviceDisplayData(
                    device =
                        FakeDeviceDelegateImpl(
                            initialConnectionState = DeviceConnectionState.Connected,
                            initialRecordFileState = LoadingState.Loaded(
                                emptyList()
                            ),
                            initialConfigFileState = LoadingState.Loaded(
                                defaultConfigFile().copy(name = "Speed Corbas")
                            ),
                            configFileName = "Speed Corbas",
                            name = "Fake device 1"
                        ),
                    deviceConfig = LoadingState.Loaded(
                        defaultConfigFile().copy(name = "Speed Corbas")
                    ),
                    isConfigFromSystem = true,
                    hasConfigContentChanged = false
                )
            ),
            refreshingDeviceList = LoadingState.Idle,
            unitSystem = UnitSystem.Metric,
            updatingConfiguration = null
        ),
        onDeviceSelected = {},
        onRequestBluetoothPermissionClicked = {},
        onEnableBluetoothClicked = {},
        onCancelScanClicked = {},
        refreshBluetoothDeviceListClicked = {},
        onAddDeviceClicked = {},
        onConnectDeviceClicked = {},
        onUploadConfigToSystemClicked = {},
        onUpdateSystemConfigClicked = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemDisconnectedPreview() {
    FlySightDeviceItem(
        firmwareCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.Disconnected
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = LoadingState.Idle
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectingPreview() {
    FlySightDeviceItem(
        firmwareCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.Connecting
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = LoadingState.Idle
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndNominalConfigFilePreview() {
    FlySightDeviceItem(
        firmwareCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                configFileName = "Distance Le Puy",
                initialConfigFileState = LoadingState.Loaded(defaultConfigFile().copy(name = "Distance Le Puy"))
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = LoadingState.Idle
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileUnknownPreview() {
    FlySightDeviceItem(
        firmwareCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialConfigFileState = LoadingState.Loaded(defaultConfigFile().copy(name = "Distance Le Puy"))
            ),
            isConfigFromSystem = false,
            hasConfigContentChanged = false,
            deviceConfig = LoadingState.Idle
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileDiffersPreview() {
    FlySightDeviceItem(
        firmwareCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialRecordFileState = LoadingState.Loaded(
                    listOf(
                        RecordFile(
                            LocalDateTime.now()
                        )
                    )
                ),
                configFileName = "Speed Corbas",
                initialConfigFileState = LoadingState.Loaded(defaultConfigFile().copy(name = "Speed Corbas"))
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = true,
            deviceConfig = LoadingState.Loaded(defaultConfigFile().copy(name = "Speed Corbas"))
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileLoadingPreview() {
    FlySightDeviceItem(
        firmwareCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialRecordFileState = LoadingState.Loaded(
                    listOf(
                        RecordFile(
                            LocalDateTime.now()
                        )
                    )
                ),
                initialConfigFileState = LoadingState.Loading<ConfigFile>(),
                configFileName = "Speed Corbas"
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = true,
            deviceConfig = LoadingState.Loading<ConfigFile>()
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndUpdatingConfigurationPreview() {
    FlySightCompanionTheme(
        darkTheme = true
    ) {
        FlySightDeviceItem(
            firmwareCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
            device = ListFlySightDeviceDisplayData(
                device = FakeDeviceDelegateImpl(
                    initialConnectionState = DeviceConnectionState.Connected,
                    initialRecordFileState = LoadingState.Loaded(
                        listOf(
                            RecordFile(
                                LocalDateTime.now()
                            )
                        )
                    ),
                    initialConfigFileState = LoadingState.Loading<ConfigFile>(),
                    configFileName = "Speed Corbas"
                ),
                isConfigFromSystem = true,
                hasConfigContentChanged = true,
                deviceConfig = LoadingState.Loading<ConfigFile>()
            ),
            unitSystem = UnitSystem.Metric,
            updatingConfiguration = true,
            onConnectionClicked = {},
            onDeviceClicked = {},
            onUpdateSystemConfClicked = {},
            onUploadConfigToSystem = {},
            onPushConfigToDeviceClicked = {},
            onChangeDeviceConfigurationClicked = {},
            onUploadRecordToSystem = {},
            onPreventDialogForFirmwareVersion = {},
            onUpdateFirmwareClicked = {}
        )
    }
}

@Preview
@Composable
fun FlySightDeviceItemErrorPreview() {
    FlySightDeviceItem(
        firmwareCompatibilityMatrix = FirmwareCompatibilityMatrix.placeholder,
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.ConnectionError
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = false,
            deviceConfig = LoadingState.Idle
        ),
        unitSystem = UnitSystem.Metric,
        updatingConfiguration = false,
        onConnectionClicked = {},
        onDeviceClicked = {},
        onUpdateSystemConfClicked = {},
        onUploadConfigToSystem = {},
        onPushConfigToDeviceClicked = {},
        onChangeDeviceConfigurationClicked = {},
        onUploadRecordToSystem = {},
        onPreventDialogForFirmwareVersion = {},
        onUpdateFirmwareClicked = {}
    )
}

@Preview
@Composable
fun DeviceConfigurationMisMatchDialogWithConfigContentChangedPreview() {
    DeviceConfigurationMisMatchDialog(
        configFileState = LoadingState.Loaded(defaultConfigFile().copy(name = "Speed Corbas")),
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialRecordFileState = LoadingState.Loaded(
                    listOf(
                        RecordFile(
                            LocalDateTime.now()
                        )
                    )
                ),
                configFileName = "Speed Corbas"
            ),
            isConfigFromSystem = true,
            hasConfigContentChanged = true,
            deviceConfig = LoadingState.Loaded(defaultConfigFile().copy(name = "Speed Corbas"))
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
        configFileState = LoadingState.Loaded(defaultConfigFile().copy(name = "Speed Corbas")),
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceDelegateImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                configFileName = "Speed Corbas"
            ),
            isConfigFromSystem = false,
            hasConfigContentChanged = false,
            deviceConfig = LoadingState.Loaded(defaultConfigFile().copy(name = "Speed Corbas"))
        ),
        onDismissRequest = {},
        onUploadConfigToSystem = {},
        onUpdateSystemConfClicked = {},
        onPushConfigToDeviceClicked = {}
    )
}

private class FakeDeviceDelegateImpl(
    initialConnectionState: DeviceConnectionState = DeviceConnectionState.Disconnected,
    initialRecordFileState: LoadingState<List<RecordFile>> = LoadingState.Idle,
    initialConfigFileState: LoadingState<ConfigFile> = LoadingState.Idle,
    private val configFileName: String = "",
    override val name: String = "Fake device"
) : MutableFlySightDevice {
    override val volatileUuid: String
        get() = "uuid"
    override val connectionState: StateFlow<DeviceConnectionState> =
        MutableStateFlow(initialConnectionState)
    override val deviceMode: StateFlow<DeviceMode> =
        MutableStateFlow(DeviceMode.Sleep)
    override val batteryLevel: StateFlow<Int> = MutableStateFlow(0)
    override val configFile: StateFlow<LoadingState<ConfigFile>> =
        MutableStateFlow(initialConfigFileState)
    override val rawConfigFile: StateFlow<FileState>
        get() = MutableStateFlow(FileState.Nothing)
    override val flySightFile: StateFlow<FileState>
        get() = MutableStateFlow(FileState.Nothing)
    override val records: StateFlow<LoadingState<List<RecordFile>>> =
        MutableStateFlow(initialRecordFileState).asStateFlow()
    override val logs: StateFlow<List<String>>
        get() = MutableStateFlow(emptyList())
    override val fileReceived: SharedFlow<FileState>
        get() = MutableSharedFlow()
    override val ping: SharedFlow<Boolean>
        get() = MutableSharedFlow()
    override val firmwareVersion: StateFlow<String?>
        get() = MutableStateFlow("")
    override val publicKeys: StateFlow<Pair<String, String>?>
        get() = MutableStateFlow(null)
    override val gnssFeed: SharedFlow<GnssData> = MutableSharedFlow()
    override val isBle: Boolean = true

    override suspend fun connect(): Boolean = true

    override suspend fun disconnect(): Boolean = true

    override fun flowDirectory(directoryPath: List<String>): StateFlow<List<FileInfo>> =
        MutableStateFlow(emptyList())

    override suspend fun readFile(fileName: String) {}
    override suspend fun readFileSynchronously(fileName: String): FileState = FileState.Nothing

    override suspend fun updateConfigFile(configFile: ConfigFile) {}
    override suspend fun writeBinaryFile(
        filePath: String,
        fileContent: ByteArray,
        callback: (Int) -> Unit
    ): Boolean = true

    override suspend fun startGNSSFeed() {
        TODO("Not yet implemented")
    }

    override suspend fun stopGNSSFeed() {
        TODO("Not yet implemented")
    }

}
