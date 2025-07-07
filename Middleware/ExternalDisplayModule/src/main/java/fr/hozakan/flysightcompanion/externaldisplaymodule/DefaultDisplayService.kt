package fr.hozakan.flysightcompanion.externaldisplaymodule

import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DefaultDisplayService(
    private val activityLifecycleService: ActivityLifecycleService
) : DisplayService {

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("DisplayService"))

    override fun lockDisplay(lock: Boolean) {
        scope.launch {
            (activityLifecycleService.awaitActivity() as? ScreenExtensions)?.lock(lock)
        }
    }
}