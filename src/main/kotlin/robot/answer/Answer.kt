/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░   ░░░  ░░░      ░░░  ░░░░  ░░        ░░       ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒  ▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓        ▓▓      ▓▓▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
███████████████████████████████        ██  ██    ████████  ██   ██   ██  ████████  ███  ████████████████████████████████
███████████████████████████████  ████  ██  ███   ███      ███  ████  ██        ██  ████  ███████████████████████████████
 */

package dev.supachain.robot.answer

import dev.supachain.utilities.castFormat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents an asynchronous answer or response that is expected to be available in the future.
 *
 * This class wraps a [Deferred] object, providing a convenient way to handle asynchronous results. It allows
 * for registering a callback function (`onAnswer`) that is invoked when the result is ready, or explicitly
 * waiting for the result using `await()`. Additionally, it supports error handling through the `onError` function.
 *
 * @param T The type of the value that the answer is expected to hold.
 *
 * @constructor Creates an instance of `Answer` wrapping a `Deferred` object representing the asynchronous result.
 * @param deferred The [Deferred] object representing the asynchronous result.
 *
 * @throws IllegalArgumentException If the provided type does not conform to the expected `Answer` return type.
 * @since 0.1.0
 */
@Suppress("unused")
class Answer<T>(answerType: KType, private val deferred: Deferred<String>) {
    private val returnType: KType
    private lateinit var deferredHandle: DisposableHandle
    private lateinit var handler: Answer<T>.(T) -> Unit
    private lateinit var errorHandler: (Throwable?) -> Unit
    private val logger: org.slf4j.Logger get() = LoggerFactory.getLogger(Answer::class.java)

    init {
        // Check if the return type is of type Answer
        if (answerType.jvmErasure != Answer::class) {
            throw IllegalArgumentException("Return must be an Answer of some type. But received $answerType")
        }

        // Check the generic type T
        returnType = answerType.arguments.firstOrNull()?.type
            ?: throw IllegalArgumentException("Answer return type must be specified")
    }

    /**
     * Registers a callback function to be invoked when the answer is available.
     *
     * The callback function is executed with the result of the asynchronous operation as its argument.
     * If an exception occurs during the operation, the callback will not be invoked.
     * This method also attaches a handler to the [Deferred] object for managing completion events.
     *
     * @param handler The callback function to execute when the answer is ready.
     * @return This `Answer` object, allowing for method chaining.
     *
     * @since 0.1.0
     */
    infix fun onAnswer(handler: Answer<T>.(T) -> Unit): Answer<T> {
        this.handler = handler
        deferredHandle = deferred.invokeOnCompletion { throwable ->
            if (throwable == null) handler(getFormattedAnswer())
            else {
                when (throwable) {
                    is CancellationException -> logger.warn(
                        "[Answer] Could not format answer, operation cancelled: {}",
                        throwable.message
                    )

                    is IllegalStateException -> logger.error("[Answer] Illegal state: {}", throwable.message)
                    // Handle robot errors here
                    // is RobotError -> handleRobotError(throwable)

                    else -> logger.error("Unexpected error in asynchronous operation", throwable)
                }
                errorHandler(throwable) // Always invoke the general error handler
            }
        }
        return this
    }

    /**
     * Registers a callback function to be invoked in case an error occurs during the asynchronous operation.
     *
     * If an exception or error is encountered, the provided error handler is invoked with the exception as
     * its argument.
     *
     * @param handler The error handler function to execute upon encountering an error.
     * @return This `Answer` object for method chaining.
     *
     * @since 0.1.0
     */
    infix fun onError(handler: (Throwable?) -> Unit) = this.apply { errorHandler = handler }

    /**
     * Waits for the result of the asynchronous operation and returns it.
     *
     * Suspends the current coroutine until the operation completes, returning the result of the operation.
     * This method also invokes the registered callback function, if available. In the event of a failure, an
     * exception is thrown.
     *
     * @return The result of the asynchronous operation.
     *
     * @throws IllegalStateException If the deferred operation cannot be completed.
     * @since 0.1.0
     */
    fun await(): T {
        if (::deferredHandle.isInitialized) deferredHandle.dispose()
        return runBlocking {
            val result = deferred.await().format()
            if (::handler.isInitialized) handler(result)
            result
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getFormattedAnswer() = deferred.getCompleted().format()


    /**
     * Formats the raw response based on the expected return type [T].
     *
     * This method converts the raw string result into the correct type, which could be a simple value,
     * an enum, a list, or a set, based on the specified return type. It throws an exception if the
     * return type is not supported or cannot be inferred correctly.
     *
     * @return The formatted response of type [T].
     *
     * @throws IllegalArgumentException If the response cannot be formatted to the specified type.
     * @since 0.1.0
     */
    @Suppress("UNCHECKED_CAST")
    private fun String.format(): T {
        // Extract the KClass representing the unwrapped return type
        val returnClass = returnType.classifier as? KClass<*>
            ?: throw IllegalArgumentException("Unexpected return type: ${returnType.jvmErasure.qualifiedName}")
        return when {
            // Groups
            returnClass.isSubclassOf(Enum::class) -> {
                val enumClass = returnClass as KClass<out Enum<*>>
                enumClass.java.enumConstants.first { it.name == this.trim().trimEnd() } as T
            }

            returnClass.isSubclassOf(List::class) -> {
                val listType = returnType.arguments.firstOrNull()?.type?.jvmErasure
                    ?: throw IllegalArgumentException("Expected a generic List type")
                this.split("\n").map { listType.castFormat(it) } as T
            }

            returnClass.isSubclassOf(Set::class) -> {
                val listType = returnType.arguments.firstOrNull()?.type?.jvmErasure
                    ?: throw IllegalArgumentException("Expected a generic Set type")
                this.split("\n").map { listType.castFormat(it) }.toSet() as T
            }

            // Source
            else -> returnClass.castFormat(this) as T
        }
    }
}




