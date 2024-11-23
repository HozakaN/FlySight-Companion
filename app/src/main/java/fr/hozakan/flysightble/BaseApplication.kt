package fr.hozakan.flysightble

import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import fr.hozakan.flysightble.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightble.tools.di.injectApp
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
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}