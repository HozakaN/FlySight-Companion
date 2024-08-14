package fr.hozakan.flysightble.tools.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import fr.hozakan.flysightble.BaseApplication
import fr.hozakan.flysightble.framework.service.BaseMonitorableService
import fr.hozakan.flysightble.framework.service.StickyMonitorableService
import fr.hozakan.flysightble.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightble.framework.service.applifecycle.DefaultActivityLifecycleService
import fr.hozakan.flysightble.framework.service.async.ActivityOperationsService
import fr.hozakan.flysightble.framework.service.async.FrameworkActivityOperationsService
import fr.hozakan.flysightble.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightble.framework.service.permission.FrameworkAndroidPermissionsService
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Singleton

@InternalCoroutinesApi
@Module(includes = [ViewModelBindings::class])
class BaseModule {

    @Singleton
    @Provides
    fun provideAppContext(app: BaseApplication): Context = app

    @Singleton
    @Provides
    fun provideActivityLifecycleService(application: BaseApplication): ActivityLifecycleService =
        DefaultActivityLifecycleService(StickyMonitorableService(), application)

    @Singleton
    @Provides
    fun provideActivityOperationService(activityLifecycleService: ActivityLifecycleService): ActivityOperationsService =
        FrameworkActivityOperationsService(activityLifecycleService)

    @Singleton
    @Provides
    fun provideAndroidPermissionsService(
        application: BaseApplication,
        activityOperationsService: ActivityOperationsService
    ): AndroidPermissionsService =
        FrameworkAndroidPermissionsService(
            BaseMonitorableService(),
            application,
            activityOperationsService
        )
}
