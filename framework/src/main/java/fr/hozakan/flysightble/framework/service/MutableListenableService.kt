package fr.hozakan.flysightble.framework.service

interface MutableListenableService<T> : ListenableService<T> {
    operator fun invoke(data: T)
}