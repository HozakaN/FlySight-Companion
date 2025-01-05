package fr.hozakan.flysightcompanion.framework.service

import java.util.*

open class BaseMonitorableService<T> : MonitorableService<T> {

    private val listeners: MutableList<(data: T) -> Unit> =
        Collections.synchronizedList(mutableListOf<(data: T) -> Unit>())
    private val monitors: MutableList<MonitorableService.MonitorableServiceListener> =
        Collections.synchronizedList(mutableListOf<MonitorableService.MonitorableServiceListener>())
    

    override operator fun invoke(data: T) {
        val list = ArrayList(listeners)
        list.forEach {
            it(data)
        }
    }

    override operator fun plusAssign(listener: (data: T) -> Unit) {
        listeners += listener
        triggerMonitors(listeners.size - 1, listeners.size)
    }

    override operator fun minusAssign(listener: (data: T) -> Unit) {
        listeners -= listener
        triggerMonitors(listeners.size + 1, listeners.size)
    }

    override fun plusAssign(monitor: MonitorableService.MonitorableServiceListener) {
        monitors += monitor
    }

    override fun minusAssign(monitor: MonitorableService.MonitorableServiceListener) {
        monitors -= monitor
    }

    private fun triggerMonitors(oldCount: Int, listenerCount: Int) {
        val list = ArrayList(monitors)
        list.forEach {
            it.onListenerCountChanged(oldCount, listenerCount)
        }
    }
}