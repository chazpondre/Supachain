package dev.supachain.robot.director

import dev.supachain.robot.director.directive.Directive
import dev.supachain.utilities.Debug
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The Robot Core's interface for handling directives
 */
interface DirectiveHandling<T> : FunctionHandling<T> {
    override val toolProxy: T
    private val logger: Logger get() = LoggerFactory.getLogger(DirectiveHandling::class.java)

    /**
     * Processes a directive request, handles the provider's response, and returns the final message content.
     *
     * This function is responsible for the core logic of handling directives within the `RobotCore`.
     * It performs the following key steps:
     *
     * 1. **Directive Retrieval:** Fetches the `RobotDirective` associated with the given proxied `name` from the `directives` map.
     *    If the directive is not found, an `IllegalStateException` is thrown.
     * 2. **Provider Interaction:** Sends the provided arguments to the default provider's `messenger`, along with the directive.
     * 3. **Result Extraction:** Retrieves the final response from the `messenger`.
     * 4. **Error Handling:** Catches any exceptions during processing, logs an error message with context,
     *    and rethrows the exception for further handling by the caller.
     *
     * @param parent The name of the parent or context initiating this directive request (used for logging purposes).
     * @param name The name of the directive to be executed.
     * @param args An array of arguments to be passed to the underlying function associated with the directive.
     * @return The content of the final message in the `Messenger`, representing the response or result.
     * @throws IllegalStateException If the specified directive is not found in the `directives` map.
     * @throws Exception If any other error occurs during the processing of the directive.
     *
     * @since 0.1.0-alpha
     */
    suspend
    fun RobotCore<*, *, *>.handleDirectiveRequest(parent: String, name: String, args: Array<Any?>): String {
        try {
            // Get Corresponding Directive
            val directive = directives[name] ?: throw IllegalStateException("Method $name not found in methodLookUp")

            // Debug
            logReceiveDirectiveRequest(parent, name, args, directive)

            // Get Provider
            val provider = defaultProvider
            return with(provider) {
                messenger.send(args, this@handleDirectiveRequest, messageFilter, directive)
                messenger.finalResponse()
            }

        } catch (e: Exception) {
            logger.error("Error handling method $name of $parent", e)
            throw e
        }
    }

    fun logReceiveDirectiveRequest(parent: String, name: String, args: Array<Any?>, directive: Directive) {
        logger.debug(
            Debug("Director"),
            "[Request/Directive]\nRequest To Robot {\n   parent: {},\n   name: {},\n   args: {}\n}" +
                    "\n\n↓↓↓↓↓↓↓  Interpreted As  ↓↓↓↓↓↓↓\n\n" +
                    "\n\n-- method --\n{}",
            parent, name, args, directive
        )
    }


}