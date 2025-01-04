package fr.hozakan.flysightcompanion

import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightcompanion.tools.di.injectApp
import fr.hozakan.flysightcompanion.userpreferencesmodule.DataStoreService
import fr.hozakan.flysightcompanion.BuildConfig
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

class BaseApplication : Application() , HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var activityLifecycleService: ActivityLifecycleService

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreate() {
        injectApp(this)
        super.onCreate()
        initTimber()
        DataStoreService.init(applicationContext)

        Timber.d("Hoz2 ${BuildConfig.VERSION_NAME}")
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}