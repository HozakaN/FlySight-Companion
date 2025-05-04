package fr.hozakan.flysightcompanion.externaldisplaymodule

import android.content.Context
import android.hardware.display.DisplayManager
import fr.hozakan.flysightcompanion.framework.service.applifecycle.ActivityLifecycleService
import fr.hozakan.flysightcompanion.loggermodule.LoggerService
import fr.hozakan.flysightcompanion.model.display.Display
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultDisplayService(
    context: Context,
    private val activityLifecycleService: ActivityLifecycleService,
    private val loggerService: LoggerService
) : DisplayService {

    private val _displays = MutableStateFlow(emptyList<Display>())
    override val displays: StateFlow<List<Display>> = _displays.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("DisplayService"))

    private val displayManager = context.getSystemService(DisplayManager::class.java)

    private val displayManagerListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            refreshDisplays()
        }

        override fun onDisplayRemoved(displayId: Int) {
            refreshDisplays()
        }

        override fun onDisplayChanged(displayId: Int) {}

    }

    init {
        refreshDisplays()
        displayManager.registerDisplayListener(displayManagerListener, null)
    }

    override fun lockDisplay(lock: Boolean) {
        scope.launch {
            (activityLifecycleService.awaitActivity() as? ScreenExtensions)?.lock(lock)
        }
    }

    private fun refreshDisplays() {
        val displays1 = displayManager.displays
        loggerService.log("available displays : ${displays1.size} : ${
            displays1.joinToString(
                separator = "; "
            ) { it.name }
        }")
        _displays.value = displays1.filter { it.displayId != android.view.Display.DEFAULT_DISPLAY }.map { display ->
            Display(
                id = display.displayId,
                name = display.name,
                internal = display
            )
        }
    }
}