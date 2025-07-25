package fr.hozakan.flysightcompanion.tools.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail.ConfigDetailViewModel
import fr.hozakan.flysightcompanion.configfilesmodule.ui.list_files.ListConfigFilesViewModel
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.firmware.FirmwareScreenViewModel
import fr.hozakan.flysightcompanion.framework.dagger.ViewModelKey
import fr.hozakan.flysightcompanion.framework.viewmodel.ViewModelFactory
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_config.DeviceConfigurationViewModel
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail.DeviceDetailActionsViewModel
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail.DeviceDetailViewModel
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.file.FileScreenViewModel
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs.ListFlySightDevicesViewModel
import fr.hozakan.flysightcompanion.recordsmodule.ui.detail.RecordDetailViewModel
import fr.hozakan.flysightcompanion.recordsmodule.ui.list.ListRecordsViewModel
import fr.hozakan.flysightcompanion.recordsmodule.ui.plot.PlotSettingsViewModel
import fr.hozakan.flysightcompanion.sessionmodule.ui.profile.SessionProfileViewModel
import fr.hozakan.flysightcompanion.sessionmodule.ui.prepare_session.PrepareSessionViewModel
import fr.hozakan.flysightcompanion.sessionmodule.ui.player.SessionPlayerViewModel
import fr.hozakan.flysightcompanion.sessionmodule.ui.reference.ReferencePointListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@Module
internal abstract class ViewModelBindings {

    @OptIn(ExperimentalCoroutinesApi::class)
    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(ListFlySightDevicesViewModel::class)
    abstract fun bindListFlySightDevicesViewModel(listFlySightDevicesViewModel: ListFlySightDevicesViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(DeviceDetailViewModel::class)
    abstract fun bindDeviceDetailViewModel(deviceDetailViewModel: DeviceDetailViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(DeviceDetailActionsViewModel::class)
    abstract fun bindDeviceDetailActionsViewModel(deviceDetailActionsViewModel: DeviceDetailActionsViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(ListConfigFilesViewModel::class)
    abstract fun bindListConfigFilesViewModel(listConfigFilesViewModel: ListConfigFilesViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(ConfigDetailViewModel::class)
    abstract fun bindConfigDetailViewModel(configDetailViewModel: ConfigDetailViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(FileScreenViewModel::class)
    abstract fun bindFileScreenViewModel(fileScreenViewModel: FileScreenViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(DeviceConfigurationViewModel::class)
    abstract fun bindDeviceConfigurationViewModel(deviceConfigurationViewModel: DeviceConfigurationViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(ListRecordsViewModel::class)
    abstract fun bindListRecordsViewModel(listRecordsViewModel: ListRecordsViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(RecordDetailViewModel::class)
    abstract fun bindRecordDetailViewModel(recordDetailViewModel: RecordDetailViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(PlotSettingsViewModel::class)
    abstract fun bindPlotSettingsViewModel(plotSettingsViewModel: PlotSettingsViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(PrepareSessionViewModel::class)
    abstract fun bindPickConfigViewModel(prepareSessionViewModel: PrepareSessionViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(SessionProfileViewModel::class)
    abstract fun bindSessionConfigViewModel(sessionProfileViewModel: SessionProfileViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(SessionPlayerViewModel::class)
    abstract fun bindSessionPlayViewModel(sessionPlayerViewModel: SessionPlayerViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(ReferencePointListViewModel::class)
    abstract fun bindReferencePointListViewModel(referencePointListViewModel: ReferencePointListViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(FirmwareScreenViewModel::class)
    abstract fun bindFirmwareScreenViewModel(firmwareScreenViewModel: FirmwareScreenViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
