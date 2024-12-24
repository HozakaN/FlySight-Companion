package fr.hozakan.flysightcompanion.framework.service.applifecycle

import android.app.Activity
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import fr.hozakan.flysightcompanion.framework.service.ListenableService
import fr.hozakan.flysightcompanion.framework.service.MutableListenableService
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

class DefaultActivityLifecycleService(
    private val delegate: MutableListenableService<ComponentActivity?>,
    app: Application
) :
    ActivityLifecycleService,
    ListenableService<ComponentActivity?> by delegate {

    private val awaitingCoroutines = mutableListOf<CancellableContinuation<ComponentActivity>>()
    private val awaitingNextResumeCoroutines = mutableListOf<CancellableContinuation<Unit>>()

    private val callback = object : SimpleActivityLifecycleCallbacks() {
        override fun onActivityPaused(activity: Activity) {
            if (activity == currentActivity) {
                currentActivity = null
                notifyActivityChanged()
            }
        }

        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity as? ComponentActivity
            notifyActivityChanged()
            freeAwaitingNextResumeCoroutines()
        }
    }

    override var currentActivity: ComponentActivity? = null

    private val _appInForeground = MutableStateFlow(false)
    override val appInForeground: Flow<Boolean>
        get() = _appInForeground

    init {
        app.registerActivityLifecycleCallbacks(callback)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                Timber.i("App state changed : ${event.name}")
                _appInForeground.value = source.lifecycle.currentState == Lifecycle.State.RESUMED || event == Lifecycle.Event.ON_START
            }
        })
    }


    override suspend fun awaitActivity(): ComponentActivity {
        return currentActivity ?: suspendCancellableCoroutine { continuation ->
            awaitingCoroutines += continuation
            continuation.invokeOnCancellation {
                awaitingCoroutines -= continuation
            }
        }
    }

    override suspend fun awaitNextResume() = suspendCancellableCoroutine<Unit> { continuation ->
        awaitingNextResumeCoroutines += continuation
        continuation.invokeOnCancellation {
            awaitingNextResumeCoroutines -= continuation
        }
    }

    private fun notifyActivityChanged() {
        currentActivity?.let { activity ->
            val listeners = ArrayList(awaitingCoroutines)
            awaitingCoroutines.clear()
            listeners.forEach {
                if (it.isActive) {
                    it.resume(activity)
                }
            }
        }
        delegate(currentActivity)
    }

    private fun freeAwaitingNextResumeCoroutines() {
        currentActivity?.let { _ ->
            val listeners = ArrayList(awaitingNextResumeCoroutines)
            awaitingNextResumeCoroutines.clear()
            listeners.forEach {
                if (it.isActive) {
                    it.resume(Unit)
                }
            }
        }
    }

}