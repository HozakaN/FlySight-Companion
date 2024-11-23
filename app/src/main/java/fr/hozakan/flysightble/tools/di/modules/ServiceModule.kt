package fr.hozakan.flysightble.tools.di.modules

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import fr.hozakan.flysightble.BaseApplication
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.bluetoothmodule.DefaultBluetoothService
import fr.hozakan.flysightble.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigFileService
import fr.hozakan.flysightble.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightble.framework.service.async.ActivityOperationsService
import fr.hozakan.flysightble.fsdevicemodule.business.DefaultFsDeviceService
import fr.hozakan.flysightble.fsdevicemodule.business.FsDeviceService
import kotlinx.coroutines.InternalCoroutinesApi
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton

@InternalCoroutinesApi
@Module
class ServiceModule {

    @Singleton
    @Provides
    @Named("serviceExecutor")
    fun provideLibExecutor(): ExecutorService = Executors.newSingleThreadExecutor()

    @Singleton
    @Provides
    fun provideGson(): Gson = Gson()

    @Singleton
    @Provides
    fun provideBluetoothService(
        baseApplication: BaseApplication,
        activityOperationsService: ActivityOperationsService,
        activityLifecycleService: ActivityLifecycleService
    ): BluetoothService = DefaultBluetoothService(
        baseApplication.applicationContext,
        activityOperationsService,
        activityLifecycleService
    )

    @Singleton
    @Provides
    fun provideFsDeviceService(
        baseApplication: BaseApplication,
        bluetoothService: BluetoothService
    ): FsDeviceService = DefaultFsDeviceService(
        baseApplication.applicationContext,
        bluetoothService
    )

    @Singleton
    @Provides
    fun provideConfigFileService(
        baseApplication: BaseApplication
    ): ConfigFileService = DefaultConfigFileService(baseApplication.applicationContext)

}
