package fr.hozakan.flysightcompanion.sessionmodule.business.controller

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface TimeMutableSource {
    val paused: StateFlow<Boolean>
    val startValue: StateFlow<Float>
    val endValue: StateFlow<Float>

    val currentTime: StateFlow<Float>

    val userInteracting: StateFlow<Boolean>
    val userInteractionEvent: SharedFlow<Boolean>
    val userInteractionEndEvent: SharedFlow<Float>

    fun moveTo(value: Float)
    fun start()
    fun pause()
    suspend fun awaitPauseEnd()
}