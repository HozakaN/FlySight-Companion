package fr.hozakan.flysightcompanion.tools.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import fr.hozakan.flysightcompanion.BaseApplication
import fr.hozakan.flysightcompanion.framework.service.BaseMonitorableService
import fr.hozakan.flysightcompanion.framework.service.StickyMonitorableService
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightcompanion.framework.service.applifecycle.DefaultActivityLifecycleService
import fr.hozakan.flysightcompanion.framework.service.async.ActivityOperationsService
import fr.hozakan.flysightcompanion.framework.service.async.FrameworkActivityOperationsService
import fr.hozakan.flysightcompanion.framework.service.permission.AndroidPermissionsService
import fr.hozakan.flysightcompanion.framework.service.permission.FrameworkAndroidPermissionsService
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
