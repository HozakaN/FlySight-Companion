package fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightCompanionTheme
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.config.UnitSystem
import fr.hozakan.flysightcompanion.model.defaultConfigFile
import fr.hozakan.flysightcompanion.model.records.Record
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
        onUploadRecordToSystem = {}
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
        onUploadRecordToSystem = {}
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
        onUploadRecordToSystem = {}
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
        onUploadRecordToSystem = {}
    )
}

@Preview
@Composable
fun ListFlySightDevicesScreenInternalWithDevicePreview() {
    ListFlySightDevicesScreenInternal(
        state = ListFlySightDevicesState(
            hasBluetoothPermission = true,
            bluetoothState = BluetoothService.BluetoothState.Available,
            devices = listOf(
                ListFlySightDeviceDisplayData(
                    device =
                        FakeDeviceImpl(
                            initialConnectionState = DeviceConnectionState.Disconnected,
                            initialRecordState = LoadingState.Loaded(
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
        onUploadRecordToSystem = {}
    )
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
        onUploadRecordToSystem = {}
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
        onUploadRecordToSystem = {}
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
        onUploadRecordToSystem = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileUnknownPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
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
        onUploadRecordToSystem = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileDiffersPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialRecordState = LoadingState.Loaded(
                    listOf(
                        Record(
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
        onUploadRecordToSystem = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndConfigFileLoadingPreview() {
    FlySightDeviceItem(
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialRecordState = LoadingState.Loaded(
                    listOf(
                        Record(
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
        onUploadRecordToSystem = {}
    )
}

@Preview
@Composable
fun FlySightDeviceItemConnectedAndUpdatingConfigurationPreview() {
    FlySightCompanionTheme(
        darkTheme = true
    ) {
        FlySightDeviceItem(
            device = ListFlySightDeviceDisplayData(
                device = FakeDeviceImpl(
                    initialConnectionState = DeviceConnectionState.Connected,
                    initialRecordState = LoadingState.Loaded(
                        listOf(
                            Record(
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
            onUploadRecordToSystem = {}
        )
    }
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
        onUploadRecordToSystem = {}
    )
}

@Preview
@Composable
fun DeviceConfigurationMisMatchDialogWithConfigContentChangedPreview() {
    DeviceConfigurationMisMatchDialog(
        configFileState = LoadingState.Loaded(defaultConfigFile().copy(name = "Speed Corbas")),
        device = ListFlySightDeviceDisplayData(
            device = FakeDeviceImpl(
                initialConnectionState = DeviceConnectionState.Connected,
                initialRecordState = LoadingState.Loaded(
                    listOf(
                        Record(
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
            device = FakeDeviceImpl(
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

private class FakeDeviceImpl(
    initialConnectionState: DeviceConnectionState = DeviceConnectionState.Disconnected,
    initialRecordState: LoadingState<List<Record>> = LoadingState.Idle,
    initialConfigFileState: LoadingState<ConfigFile> = LoadingState.Idle,
    private val configFileName: String = "",
    override val name: String = "Fake device"
) : FlySightDevice {
    override val uuid: String
        get() = "uuid"
    override val address: String
        get() = "address"
    override val connectionState: StateFlow<DeviceConnectionState> =
        MutableStateFlow(initialConnectionState)
    override val configFile: StateFlow<LoadingState<ConfigFile>> = MutableStateFlow(initialConfigFileState)
    override val rawConfigFile: StateFlow<FileState>
        get() = MutableStateFlow(FileState.Nothing)
    override val records: StateFlow<LoadingState<List<Record>>> =
        MutableStateFlow(initialRecordState).asStateFlow()
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
    override suspend fun readFileSynchronously(fileName: String): FileState = FileState.Nothing

    override suspend fun updateConfigFile(configFile: ConfigFile) {}

}
