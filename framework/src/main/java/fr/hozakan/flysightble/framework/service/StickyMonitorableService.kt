package fr.hozakan.flysightble.framework.service

class StickyMonitorableService<T> : BaseMonitorableService<T>() {

    private var data: T? = null

    override fun plusAssign(listener: (data: T) -> Unit) {
        super.plusAssign(listener)
        data?.let {
            listener(it)
        }
    }

    override fun invoke(data: T) {
        this.data = data
        super.invoke(data)
    }
}