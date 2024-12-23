package fr.hozakan.flysightble.fsdevicemodule.ui.list_fs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.designsystem.theme.FlySightBLETheme
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
        onChangeDeviceConfigurationClicked = {}
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
        onChangeDeviceConfigurationClicked = {}
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
        onChangeDeviceConfigurationClicked = {}
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
        onChangeDeviceConfigurationClicked = {}
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
                            initialResultFileState = LoadingState.Loaded(
                                emptyList()
                            ),
                            initialConfigFileState = ConfigFileState.Nothing,
                            configFileName = "Speed Corbas",
                            name = "Fake device 1"
                        ),
                    deviceConfig = ConfigFileState.Nothing,
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
        onChangeDeviceConfigurationClicked = {}
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
    FlySightBLETheme(
        darkTheme = true
    ) {
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
