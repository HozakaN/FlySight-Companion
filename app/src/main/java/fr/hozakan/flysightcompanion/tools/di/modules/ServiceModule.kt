package fr.hozakan.flysightcompanion.tools.di.modules

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import fr.hozakan.flysightcompanion.dialogmodule.DefaultDialogService
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.MutableDialogService
import fr.hozakan.flysightcompanion.userpreferencesmodule.DatastoreUserPrefService
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
import fr.hozakan.flysightcompanion.BaseApplication
import fr.hozakan.flysightcompanion.BuildConfig
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.bluetoothmodule.DefaultBluetoothService
import fr.hozakan.flysightcompanion.capabilitiesmodule.CapabilitiesService
import fr.hozakan.flysightcompanion.capabilitiesmodule.DefaultCapabilitiesService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.configfilesmodule.business.DefaultConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.DefaultConfigFileService
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightcompanion.framework.service.async.ActivityOperationsService
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.framework.service.versionning.DefaultAppVersionService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.DefaultFsDeviceService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.loggermodule.DefaultLoggerService
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.networkmodule.KTorNetworkService
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import fr.hozakan.flysightcompanion.recordsmodule.business.FileBasedRecordService
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.sessionmodule.business.DefaultSessionConfigurationsService
import fr.hozakan.flysightcompanion.sessionmodule.business.DefaultSessionPlayerService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionConfigurationsService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionPlayerService
import fr.hozakan.flysightcompanion.usbmodule.DefaultUsbService
import fr.hozakan.flysightcompanion.usbmodule.UsbService
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
        configEncoder: ConfigEncoder,
        configFileService: ConfigFileService,
        recordService: RecordService,
        networkService: NetworkService,
        usbService: UsbService,
        loggerService: LoggerService,
        dialogService: DialogService,
        appVersionService: AppVersionService,
        userPrefService: UserPrefService
    ): FsDeviceService = DefaultFsDeviceService(
        baseApplication.applicationContext,
        bluetoothService,
        configEncoder,
        configFileService,
        recordService,
        networkService,
        usbService,
        loggerService,
        dialogService,
        appVersionService,
        userPrefService
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
    fun provideMutableDialogService(): MutableDialogService =
        DefaultDialogService()

    @Singleton
    @Provides
    fun provideConfigEncoder(): ConfigEncoder = DefaultConfigEncoder()

    @Singleton
    @Provides
    fun provideAppVersionService(): AppVersionService = DefaultAppVersionService(
        BuildConfig.VERSION_NAME,
        BuildConfig.VERSION_CODE
    )

    @Singleton
    @Provides
    fun provideRecordService(
        baseApplication: BaseApplication
    ): RecordService = FileBasedRecordService(
        baseApplication.applicationContext
    )

    @Singleton
    @Provides
    fun provideNetworkService(
        application: BaseApplication
    ): NetworkService = KTorNetworkService(application.applicationContext)

    @Singleton
    @Provides
    fun provideFsUsbService(
        application: BaseApplication,
        activityLifecycleService: ActivityLifecycleService
    ): UsbService {
        return DefaultUsbService(application.applicationContext, activityLifecycleService)
    }

    @Singleton
    @Provides
    fun provideLoggerService(): LoggerService = DefaultLoggerService()

    @Singleton
    @Provides
    fun provideCapabilitiesService(): CapabilitiesService = DefaultCapabilitiesService()

    @Singleton
    @Provides
    fun provideSessionConfigurationsService(
        application: BaseApplication,
        dialogService: DialogService
    ): SessionConfigurationsService =
        DefaultSessionConfigurationsService(
            context = application.applicationContext,
            dialogService = dialogService
        )

    @Singleton
    @Provides
    fun provideSessionPlayerService(
        application: BaseApplication,
        fsDeviceService: FsDeviceService
    ) : SessionPlayerService = DefaultSessionPlayerService(
        context = application.applicationContext,
        fsDeviceService = fsDeviceService
    )

}
