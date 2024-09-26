package fr.hozakan.flysightble.framework.service.async

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import fr.hozakan.flysightble.framework.service.applifecycle.ActivityLifecycleService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ACTIVITY_OPERATIONS_FRAGMENT_TAG = "activityOperationsFragment"

class FrameworkActivityOperationsService(private val activityLifecycleService: ActivityLifecycleService) :
    ActivityOperationsService {

    private var currentFragment: AsyncOperationsFragment? = null

    private val listener: (Activity?) -> Unit = { activity: Activity? ->
        obtainAsyncOperationsFragment(activity)
    }

    init {
        obtainAsyncOperationsFragment(activityLifecycleService.currentActivity)
        activityLifecycleService += listener
    }

    override fun usePermission(permission: String, job: (Boolean) -> Unit) {
        GlobalScope.launch {
            while (currentFragment == null || currentFragment?.isAdded != true) {
                delay(200)
            }
            currentFragment?.usePermissions(arrayListOf(permission), job)
        }
    }

    override fun usePermissions(permissions: List<String>, job: (Boolean) -> Unit) {
        GlobalScope.launch {
            while (currentFragment == null || currentFragment?.isAdded != true) {
                delay(200)
            }
            currentFragment?.usePermissions(ArrayList(permissions), job)
        }
    }

    override fun usePermissions(vararg permissions: String, job: (Boolean) -> Unit) {
        GlobalScope.launch {
            while (currentFragment == null || currentFragment?.isAdded != true) {
                delay(200)
            }
            val permissions1 = ArrayList(permissions.toList())
            currentFragment?.usePermissions(permissions1, job)
        }
    }

    override suspend fun requestPermission(permission: String): Boolean {
        while (currentFragment == null || currentFragment?.isAdded != true) {
            delay(200)
        }
        return currentFragment!!.requestPermission(permission)
    }

    override suspend fun requestPermissions(vararg permissions: String): Boolean {
        while (currentFragment == null || currentFragment?.isAdded != true) {
            delay(200)
        }
        return currentFragment!!.requestPermissions(*permissions)
    }

    override suspend fun requestActivityResult(intent: Intent): Pair<Int, Intent?> {
        while (currentFragment == null || currentFragment?.isAdded != true) {
            delay(200)
        }
        return currentFragment!!.requestActivityResult(intent)
//        val activity = activityLifecycleService.awaitActivity()
//        return suspendCancellableCoroutine { continuation ->
//            val acLauncher =
//                activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//                    continuation.resume(it.resultCode to it.data)
//                }
//            acLauncher.launch(intent)
//            continuation.invokeOnCancellation {
//                acLauncher.unregister()
//            }
//        }
    }

    private fun obtainAsyncOperationsFragment(activity: Activity?) {
        currentFragment = null
        val act = activity as? FragmentActivity
        if (act != null) {
            var fragment =
                act.supportFragmentManager.findFragmentByTag(ACTIVITY_OPERATIONS_FRAGMENT_TAG) as? AsyncOperationsFragment
            if (fragment == null) {
                fragment = AsyncOperationsFragment()
                act.supportFragmentManager.beginTransaction()
                    .add(fragment, ACTIVITY_OPERATIONS_FRAGMENT_TAG).commit()
            }
            currentFragment = fragment
        }
    }

}