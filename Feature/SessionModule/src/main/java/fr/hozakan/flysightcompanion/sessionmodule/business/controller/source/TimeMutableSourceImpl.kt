package fr.hozakan.flysightcompanion.sessionmodule.business.controller.source

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TimeMutableSourceImpl() : TimeMutableSource {
    private val scope = CoroutineScope(SupervisorJob())
    private var _startValue = MutableStateFlow(0f)
    private var _endValue = MutableStateFlow(0f)
    private var _currentValue = MutableStateFlow(0f)
    override val startValue: StateFlow<Float> = _startValue.asStateFlow()
    override val endValue: StateFlow<Float> = _endValue.asStateFlow()
    override val currentTime: StateFlow<Float> = _currentValue.asStateFlow()

    private val _userInteracting = MutableStateFlow(false)
    override val userInteracting: StateFlow<Boolean> = _userInteracting.asStateFlow()

    private val _userInteractionEvent = MutableSharedFlow<Boolean>()
    override val userInteractionEvent: SharedFlow<Boolean> = _userInteractionEvent.asSharedFlow()

    private val userInteractionContinuations = mutableListOf<CancellableContinuation<Boolean>>()
    private val pauseContinuations = mutableListOf<CancellableContinuation<Unit>>()

    // New event to notify when user stops interacting
    private val _userInteractionEndEvent = MutableSharedFlow<Float>()
    override val userInteractionEndEvent: SharedFlow<Float> = _userInteractionEndEvent.asSharedFlow()

    private val _paused = MutableStateFlow(false)
    override val paused: StateFlow<Boolean> = _paused.asStateFlow()

    fun setStartValue(value: Float) {
        _startValue.value = value
    }

    fun setEndValue(value: Float) {
        _endValue.value = value
    }

    override fun moveTo(value: Float) {
        if (!_userInteracting.value) {
            scope.launch {
                _userInteractionEvent.emit(true)
            }
        }
        _userInteracting.value = true
        _currentValue.value = value
    }

    override fun start() {
        _paused.value = false
        _userInteracting.value = false
        val currentTime = _currentValue.value
        scope.launch {
            _userInteractionEndEvent.emit(currentTime)
        }

        val continuations = ArrayList(userInteractionContinuations)
        userInteractionContinuations.clear()
        continuations.forEach { continuation ->
            if (continuation.isActive) {
                continuation.resume(true)
            }
        }

        val pauses = ArrayList(pauseContinuations)
        pauseContinuations.clear()
        pauses.forEach { continuation ->
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
        }
    }

    override fun pause() {
        _paused.value = true
    }

    suspend fun awaitUserInteractionEnd(): Boolean {
        if (_userInteracting.value) {
            return suspendCancellableCoroutine { continuation ->
                userInteractionContinuations += continuation
                continuation.invokeOnCancellation {
                    userInteractionContinuations -= continuation
                }
            }
        }
        return false
    }

    override suspend fun awaitPauseEnd() {
        if (_paused.value) {
            return suspendCancellableCoroutine { continuation ->
                pauseContinuations += continuation
                continuation.invokeOnCancellation {
                    pauseContinuations -= continuation
                }
            }
        }
    }

    fun setCurrentTime(time: Float) {
        _currentValue.value = time
    }
}
