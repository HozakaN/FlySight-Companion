package fr.hozakan.flysightble.tools.di

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import fr.hozakan.flysightble.BaseApplication
import fr.hozakan.flysightble.framework.dagger.Injectable
import fr.hozakan.flysightble.framework.service.applifecycle.SimpleActivityLifecycleCallbacks
import kotlinx.coroutines.InternalCoroutinesApi

@OptIn(InternalCoroutinesApi::class)
private var baseComponent: BaseComponent? = null

@InternalCoroutinesApi
fun injectApp(app: BaseApplication) {
    val component = DaggerBaseComponent
        .builder()
        .application(app)
        .build()
    baseComponent = component
    component.inject(app)

    app.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            handleActivity(activity)
        }
    })
}

private fun handleActivity(activity: Activity) {
    if (activity is Injectable || activity is HasAndroidInjector) {
        AndroidInjection.inject(activity)
    }
    (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                if (f is Injectable) {
                    AndroidSupportInjection.inject(f)
                }
            }
        },
        true
    )
}
