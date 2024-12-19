package fr.hozakan.flysightble.fsdevicemodule.business.job

import kotlinx.coroutines.CompletableDeferred

/**
 * Schedule jobs so that they are executed one after another.
 */
class FlySightJobScheduler {

    private var isRunning = false
    private val requestQueue = mutableListOf<Request>()

    /**
     * @param priority the highest the soonest executed
     */
    suspend fun <T> schedule(
        priority: Int = 0,
        block: suspend () -> T
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
        deferred.await()
        val result = block()
        onJobFinished()
        return result
    }

    private fun onJobFinished() {
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
