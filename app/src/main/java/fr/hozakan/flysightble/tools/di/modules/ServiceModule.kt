package fr.hozakan.flysightble.tools.di.modules

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import fr.hozakan.flusightble.dialog.DefaultDialogService
import fr.hozakan.flusightble.dialog.DialogService
import fr.hozakan.flusightble.dialog.MutableDialogService
import fr.hozakan.flusightble.userpreferencesmodule.DatastoreUserPrefService
import fr.hozakan.flusightble.userpreferencesmodule.UserPrefService
import fr.hozakan.flysightble.BaseApplication
import fr.hozakan.flysightble.bluetoothmodule.BluetoothService
import fr.hozakan.flysightble.bluetoothmodule.DefaultBluetoothService
import fr.hozakan.flysightble.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightble.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightble.configfilesmodule.business.DefaultConfigEncoder
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
        bluetoothService: BluetoothService,
        configEncoder: ConfigEncoder
    ): FsDeviceService = DefaultFsDeviceService(
        baseApplication.applicationContext,
        bluetoothService,
        configEncoder
    )

    @Singleton
    @Provides
    fun provideConfigFileService(
        baseApplication: BaseApplication,
        dialogService: DialogService,
        configEncoder: ConfigEncoder
    ): ConfigFileService = DefaultConfigFileService(
        baseApplication.applicationContext,
        dialogService,
        configEncoder
    )

    @Singleton
    @Provides
    fun provideUserPrefService(
        baseApplication: BaseApplication
    ): UserPrefService = DatastoreUserPrefService(baseApplication.applicationContext)

    @Singleton
    @Provides
    fun provideDialogService(
        mutableDialogService: MutableDialogService
    ): DialogService = mutableDialogService

    @Singleton
    @Provides
    fun provideMutableDialogService(): MutableDialogService = DefaultDialogService()

    @Singleton
    @Provides
    fun provideConfigEncoder(): ConfigEncoder = DefaultConfigEncoder()

}
