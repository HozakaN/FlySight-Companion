package fr.hozakan.flysightble.framework.service.loading


/**
 * Represents an instantaneous loading state with no memory and that may NOT be inactive (no `Idle` state)
 * @param <T>
 */
sealed class LoadingState<out R> {

    /**
     * Loading state: the operation is actively executing.
     *
     */
    data class Loading<out T>(val currentLoad: T? = null) : LoadingState<T>()

    /**
     * Loaded state: the operation succeeded, holding its value
     *
     * @param value: the loaded value
     */
    data class Loaded<out T>(val value: T) : LoadingState<T>()

    /**
     * Error state: the operation failed, holding the cause error
     *
     * @param error: the error throwable
     */
    data class Error<out T>(val error: Throwable) : LoadingState<T>()

}
