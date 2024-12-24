package fr.hozakan.flysightcompanion.framework.service

interface MutableListenableService<T> : ListenableService<T> {
    operator fun invoke(data: T)
}