package fr.hozakan.flysightcompanion.tools.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fr.hozakan.flysightcompanion.configfilesmodule.ui.config_detail.ConfigDetailViewModel
import fr.hozakan.flysightcompanion.configfilesmodule.ui.list_files.ListConfigFilesViewModel
import fr.hozakan.flysightcompanion.framework.dagger.ViewModelKey
import fr.hozakan.flysightcompanion.framework.viewmodel.ViewModelFactory
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_config.DeviceConfigurationViewModel
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.device_detail.DeviceDetailViewModel
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.file.FileScreenViewModel
import fr.hozakan.flysightcompanion.fsdevicemodule.ui.list_fs.ListFlySightDevicesViewModel
import fr.hozakan.flysightcompanion.recordsmodule.ui.detail.RecordDetailViewModel
import fr.hozakan.flysightcompanion.recordsmodule.ui.list.ListRecordsViewModel
import fr.hozakan.flysightcompanion.recordsmodule.ui.plot.PlotSettingsViewModel
import fr.hozakan.flysightcompanion.sessionmodule.ui.config.SessionConfigViewModel
import fr.hozakan.flysightcompanion.sessionmodule.ui.pick_config.PickConfigViewModel
import fr.hozakan.flysightcompanion.sessionmodule.ui.play.SessionPlayerViewModel
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
    @ViewModelKey(PickConfigViewModel::class)
    abstract fun bindPickConfigViewModel(pickConfigViewModel: PickConfigViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(SessionConfigViewModel::class)
    abstract fun bindSessionConfigViewModel(sessionConfigViewModel: SessionConfigViewModel): ViewModel

    @InternalCoroutinesApi
    @Binds
    @IntoMap
    @ViewModelKey(SessionPlayerViewModel::class)
    abstract fun bindSessionPlayViewModel(sessionPlayerViewModel: SessionPlayerViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
