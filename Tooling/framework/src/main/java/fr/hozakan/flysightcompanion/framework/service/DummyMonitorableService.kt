package fr.hozakan.flysightcompanion.framework.service


class DummyMonitorableService<T> : MonitorableService<T> {

    override fun plusAssign(listener: (data: T) -> Unit) {}

    override fun minusAssign(listener: (data: T) -> Unit) {}

    override fun plusAssign(monitor: MonitorableService.MonitorableServiceListener) {}

    override fun minusAssign(monitor: MonitorableService.MonitorableServiceListener) {}

    override fun invoke(data: T) {}

}