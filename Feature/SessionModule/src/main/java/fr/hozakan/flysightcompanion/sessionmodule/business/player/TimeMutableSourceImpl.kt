package fr.hozakan.flysightcompanion.sessionmodule.business.player

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
        _userInteracting.value = false
        val continuations = ArrayList(userInteractionContinuations)
        userInteractionContinuations.clear()
        continuations.forEach { continuation ->
            if (continuation.isActive) {
                continuation.resume(true)
            }
        }
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

    fun setCurrentTime(time: Float) {
        _currentValue.value = time
    }
}