package fr.hozakan.flysightcompanion.framework.service

interface ListenableService<T> : Service {
    operator fun plusAssign(listener: (data: T) -> Unit)
    operator fun minusAssign(listener: (data: T) -> Unit)
}