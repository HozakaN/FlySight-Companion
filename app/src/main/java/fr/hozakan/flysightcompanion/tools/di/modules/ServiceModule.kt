package fr.hozakan.flysightcompanion.tools.di.modules

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import fr.hozakan.flysightcompanion.BaseApplication
import fr.hozakan.flysightcompanion.BuildConfig
import fr.hozakan.flysightcompanion.audiomodule.AudioService
import fr.hozakan.flysightcompanion.audiomodule.DefaultAudioService
import fr.hozakan.flysightcompanion.bluetoothmodule.BluetoothService
import fr.hozakan.flysightcompanion.bluetoothmodule.DefaultBluetoothService
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.ConfigFileService
import fr.hozakan.flysightcompanion.configfilesmodule.business.DefaultConfigEncoder
import fr.hozakan.flysightcompanion.configfilesmodule.business.DefaultConfigFileService
import fr.hozakan.flysightcompanion.dialogmodule.DefaultDialogService
import fr.hozakan.flysightcompanion.dialogmodule.DialogService
import fr.hozakan.flysightcompanion.dialogmodule.MutableDialogService
import fr.hozakan.flysightcompanion.externaldisplaymodule.DefaultDisplayService
import fr.hozakan.flysightcompanion.externaldisplaymodule.DisplayService
import fr.hozakan.flysightcompanion.firmwaremodule.business.DefaultFirmwareUpdateService
import fr.hozakan.flysightcompanion.firmwaremodule.business.FirmwareUpdateService
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightcompanion.framework.service.async.ActivityOperationsService
import fr.hozakan.flysightcompanion.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightcompanion.framework.service.versionning.AppVersionService
import fr.hozakan.flysightcompanion.framework.service.versionning.DefaultAppVersionService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.DefaultFsDeviceService
import fr.hozakan.flysightcompanion.fsdevicemodule.business.FsDeviceService
import fr.hozakan.flysightcompanion.locationmodule.GPlayLocationService
import fr.hozakan.flysightcompanion.locationmodule.LocationService
import fr.hozakan.flysightcompanion.loggermodule.DefaultLoggerService
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.networkmodule.KTorNetworkService
import fr.hozakan.flysightcompanion.networkmodule.NetworkService
import fr.hozakan.flysightcompanion.recordsmodule.business.FileBasedRecordService
import fr.hozakan.flysightcompanion.recordsmodule.business.RecordService
import fr.hozakan.flysightcompanion.sessionmodule.business.DefaultReferencePointsService
import fr.hozakan.flysightcompanion.sessionmodule.business.DefaultSessionControllerService
import fr.hozakan.flysightcompanion.sessionmodule.business.DefaultSessionProfilesService
import fr.hozakan.flysightcompanion.sessionmodule.business.ReferencePointsService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionControllerService
import fr.hozakan.flysightcompanion.sessionmodule.business.SessionProfilesService
import fr.hozakan.flysightcompanion.usbmodule.DefaultUsbService
import fr.hozakan.flysightcompanion.usbmodule.UsbService
import fr.hozakan.flysightcompanion.userpreferencesmodule.DatastoreUserPrefService
import fr.hozakan.flysightcompanion.userpreferencesmodule.UserPrefService
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
        activityOperationsService: ActivityOperationsService
    ): BluetoothService = DefaultBluetoothService(
        baseApplication.applicationContext,
        activityOperationsService
    )

    @Singleton
    @Provides
    fun provideFirmwareUpdateService(
        baseApplication: BaseApplication,
        gson: Gson,
        networkService: NetworkService
    ): FirmwareUpdateService = DefaultFirmwareUpdateService(
        context = baseApplication.applicationContext,
        gson = gson,
        networkService = networkService
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
        userPrefService: UserPrefService,
        firmwareUpdateService: FirmwareUpdateService,
        activityLifecycleService: ActivityLifecycleService
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
        userPrefService,
        firmwareUpdateService,
        activityLifecycleService
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
    fun provideSessionConfigurationsService(
        application: BaseApplication,
        dialogService: DialogService
    ): SessionProfilesService =
        DefaultSessionProfilesService(
            context = application.applicationContext,
            dialogService = dialogService
        )

    @Singleton
    @Provides
    fun provideSessionPlayerService(
        application: BaseApplication,
        fsDeviceService: FsDeviceService,
        audioService: AudioService,
        recordService: RecordService,
        displayService: DisplayService,
        locationService: LocationService,
        userPrefService: UserPrefService,
        appVersionService: AppVersionService,
        dialogService: DialogService
    ): SessionControllerService = DefaultSessionControllerService(
        context = application.applicationContext,
        fsDeviceService = fsDeviceService,
        audioService = audioService,
        recordService = recordService,
        displayService = displayService,
        locationService = locationService,
        userPrefService = userPrefService,
        appVersionService = appVersionService,
        dialogService = dialogService
    )

    @Singleton
    @Provides
    fun provideAudioService(
        application: BaseApplication
    ): AudioService {
        return DefaultAudioService(
            application.applicationContext
        )
    }

    @Singleton
    @Provides
    fun provideExternalDisplayService(
        activityLifecycleService: ActivityLifecycleService
    ): DisplayService {
        return DefaultDisplayService(
            activityLifecycleService
        )
    }

    @Singleton
    @Provides
    fun provideReferencePointsService(
        application: BaseApplication,
        dialogService: DialogService
    ): ReferencePointsService {
        return DefaultReferencePointsService(
            application.applicationContext,
            dialogService
        )
    }


    @Provides
    @Singleton
    fun provideLocationService(
        context: Context,
        activityLifecycleService: ActivityLifecycleService,
        androidPermissionsService: AndroidPermissionsService
    ): LocationService {
        return GPlayLocationService(
            context,
            activityLifecycleService,
            androidPermissionsService
        )
    }

}
