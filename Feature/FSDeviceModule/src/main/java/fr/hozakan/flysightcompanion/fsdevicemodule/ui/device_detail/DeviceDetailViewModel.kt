package fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.hozakan.flysightcompanion.framework.coroutine.flow.asFlowEvent
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.dialogmodule.AwaitFlySightDeviceModeDialog
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.OkDialogResult
import fr.hozakan.flysightcompanion.firmwaremodule.business.FirmwareUpdateService
import fr.hozakan.flysightcompanion.framework.service.loading.LoadingState
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FlySightDevice
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.FileInfo
import fr.hozakan.flysightcompanion.model.FileState
import fr.hozakan.flysightcompanion.model.firmware.FirmwareCompatibilityMatrix
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class DeviceDetailViewModel @Inject constructor(
    private val context: Context,
    private val fsDeviceService: FsDeviceService,
    private val recordService: RecordService,
    private val networkService: NetworkService,
    private val dialogService: DialogService,
    private val firmwareUpdateService: FirmwareUpdateService
) : DeviceDetailViewBase(
    context,
    fsDeviceService,
    recordService,
    networkService,
    dialogService,
    firmwareUpdateService
)