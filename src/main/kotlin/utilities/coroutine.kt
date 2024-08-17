package utilities

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Interface providing background coroutine scope management.
 *
 * This interface defines properties and methods for managing a background coroutine scope using a
 * `CompletableJob` and a `CoroutineContext`. It ensures that the job and coroutine scope can be
 * controlled and cancelled when needed.
 *
 * @property job The `CompletableJob` used to manage the lifecycle of the coroutine scope.
 * @property coroutineContext The `CoroutineContext` used to define the dispatcher and job for the coroutine scope.
 * @property scope The coroutine scope created with the provided `backgroundContext`.
 *
 * @since 0.1.0-alpha
 */
internal interface RunsInBackground {
    val job: CompletableJob
    val coroutineContext: CoroutineContext
    val scope get() = CoroutineScope(coroutineContext)

    /**
     * Cancels the job associated with the coroutine scope, terminating all running coroutines.
     *
     * This function cancels the `CompletableJob` and all associated coroutines, ensuring that
     * resources are properly released.
     *
     * @since 0.1.0-alpha
     */
    @Suppress("unused")
    fun cancel() {
        job.cancel()
    }
}