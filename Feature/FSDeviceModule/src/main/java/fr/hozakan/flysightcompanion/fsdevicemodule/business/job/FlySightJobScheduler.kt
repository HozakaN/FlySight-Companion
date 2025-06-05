package fr.hozakan.flysightcompanion.fsdevicemodule.business.job

import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber

/**
 * Schedule jobs so that they are executed one after another.
 */
class FlySightJobScheduler {

    private var isRunning = false
    private val requestQueue = mutableListOf<Request>()

    private var counter = 0
    /**
     * @param priority the highest the soonest executed
     */
    suspend fun <T> schedule(
        priority: Int = 0,
        labelProvider: (() -> String)? = null,
        block: suspend (jobId: Int) -> T
    ): T {
        val deferred = CompletableDeferred<Unit>()
        synchronized(this)  {
            if (!isRunning) {
                isRunning = true
                deferred.complete(Unit)
            } else {
                val request = Request(deferred, priority)
                val position = requestQueue.indexOfFirst { it.priority < priority }
                    .let { if (it == -1) requestQueue.size else it }
                requestQueue.add(position, request)
            }
        }
        val label = labelProvider?.invoke()
        val tag = label?.let { "$it [$counter]" }
        if (label != null) {
            Timber.i("Job $tag scheduled")
        }
        deferred.await()
        if (label != null)  {
            Timber.i("Job $tag started")
        }
        val result = try {
            block(counter)
        } finally {
            onJobFinished()
        }
        if (label != null)  {
            Timber.i("Job $tag finished")
        }
        return result
    }

    private fun onJobFinished() {
        counter++
        synchronized(this) {
            if (requestQueue.isNotEmpty()) {
                val request = requestQueue.removeAt(0)
                request.deferred.complete(Unit)
            } else {
                isRunning = false
            }
        }
    }

    private inner class Request(
        val deferred: CompletableDeferred<Unit>,
        val priority: Int
    )
}
