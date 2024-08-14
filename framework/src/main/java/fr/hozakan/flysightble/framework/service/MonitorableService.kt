package fr.hozakan.flysightble.framework.service

interface MonitorableService<T> : MutableListenableService<T> {
    operator fun plusAssign(monitor: MonitorableServiceListener)
    operator fun minusAssign(monitor: MonitorableServiceListener)

    interface MonitorableServiceListener {
        fun onListenerCountChanged(oldCount: Int, count: Int)
    }
}