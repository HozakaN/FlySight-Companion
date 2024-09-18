package fr.hozakan.flysightble.tools.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import fr.hozakan.flysightble.configfilesmodule.ui.config_detail.ConfigDetailViewModel
import fr.hozakan.flysightble.configfilesmodule.ui.list_files.ListConfigFilesViewModel
import fr.hozakan.flysightble.framework.dagger.ViewModelKey
import fr.hozakan.flysightble.framework.viewmodel.ViewModelFactory
import fr.hozakan.flysightble.fsdevicemodule.ui.device_detail.DeviceDetailViewModel
import fr.hozakan.flysightble.fsdevicemodule.ui.list_fs.ListFlySightDevicesViewModel
import kotlinx.coroutines.InternalCoroutinesApi

@Module
internal abstract class ViewModelBindings {

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

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
