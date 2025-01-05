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

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
