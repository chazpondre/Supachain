package dev.supachain.robot.director

import dev.supachain.robot.director.directive.Directive
import dev.supachain.robot.tool.stategies.BackAndForth
import dev.supachain.robot.tool.stategies.FillInTheBlank
import dev.supachain.utilities.Debug
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Interface extending [DirectorCore] and [Extensions] interfaces.
 *
 * Provides the `handleDirectiveRequest` function to process method calls intercepted by the directive proxy.
 * Provides the `handleProviderMessaging` function which directs provider communication after a directive is received.
 */
internal interface DirectiveHandling<T> : FunctionHandling<T> {
    override val toolProxy: T

    @Suppress("unused")
    private val logger: Logger get() = LoggerFactory.getLogger(DirectiveHandling::class.java)

    /**
     * Processes a directive request, handles the provider's response, and returns the final message content.
     *
     * This function is responsible for the core logic of handling directives within the `Director`.
     * It performs these key steps:
     *
     * 1. **Directive Retrieval:** Fetches the `RobotDirective` associated with the given proxied `name` from the `directives` map.
     *    Throws an `IllegalStateException` if the directive is not found.
     * 2. **Message Preparation:** Uses the directive to configure the messages for the Robot interaction,
     *    potentially including information from the function arguments and output formatting instructions.
     *    These configured messages are then added to the `Messenger`.
     * 3. **Logging:**  Logs a detailed debug message containing the request details, the interpreted messages,
     *    and the directive itself. This aids in understanding the request and its transformation.
     * 4. **Provider Interaction:**  Calls `handleProviderResponse` to send the prepared messages to the default provider
     *    and process the provider's response. This might involve executing tool calls or other actions
     *    based on the provider's strategy.
     * 5. **Result Extraction:**  Retrieves the content of the last message from the `Messenger`, which should contain
     *    the final answer or response from the Robot or the tool execution.
     * 6. **Error Handling:** Catches any exceptions during the process, logs an error message with context,
     *    and re-throws the exception for further handling.
     *
     * @param parent The name of the parent or context initiating this directive request (for logging).
     * @param name The name of the directive to be executed.
     * @param args An array of arguments to be passed to the underlying function associated with the directive.
     * @return The content of the last message in the `Messenger`, representing the final response or result.
     * @throws IllegalStateException If the specified directive is not found.
     * @throws Exception If any other error occurs during the processing of the directive.
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    suspend
    fun Director<*, *, *>.handleDirectiveRequest(parent: String, name: String, args: Array<Any?>): String {
        try {
            // Find the method in the method lookup
            val directive = directives[name] ?: throw IllegalStateException("Method $name not found in methodLookUp")

            // Add Messages to Messenger
            defaultProvider.toolStrategy.message(this)?.also { messenger(it) }
            messenger(directive.getDirectiveMessages(args))

            // Debug
            logReceiveDirectiveRequest(parent, name, args, directive)

            // Request
            handleProviderMessaging(directive, name, args)
            return messenger.lastMessage().content
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
                    "-- messages --\n{}\n\n-- method --\n{}",
            parent, name, args, messenger, directive
        )
    }

    /**
     * Handles the response from the default language model provider and delegates further processing based on the tool strategy.
     *
     * This function processes the response received from the default LLM provider. It performs the following steps:
     *
     * 1. **Request and Message Handling:** Sends a request to the default provider based on the `directive`'s feature
     *    and adds the received response messages to the `messenger`.
     * 2. **Tool Strategy Delegation:**
     *    - Determines the tool strategy configured for the default provider (`defaultProvider.toolStrategy`).
     *    - Based on the strategy, delegates further processing to either:
     *        - `BackAndForth`:  Handles function calls and potential loops, possibly making additional requests to the provider.
     *        - `FillInTheBlank`:  Processes the response directly without any tool interactions.
     * 3. **Implicit Return:** If no specific tool strategy is matched, the function implicitly returns without taking any
     *    further action.
     *
     * @param directive The `RobotDirective` object containing instructions and restrictions for the AI's actions.
     * @param name The name of the current function being executed (for logging purposes).
     * @param args The arguments passed to the function.
     * @param callHistory A mutable map to track the history of function calls and their results (used by the `BackAndForth` strategy).
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    suspend
    fun Director<*, *, *>.handleProviderMessaging(
        directive: Directive,
        name: String,
        args: Array<Any?>,
        callHistory: MutableMap<String, String> = mutableMapOf()
    ) {
        // Todo Should featureProvider
        val response = defaultProvider.request(directive.feature, this)
        messenger(response.message.data)
        logger.debug(Debug("Director"), "[Response/Provider]\n Response: {}", this)

        // Handle Tool Strategy
        when (defaultProvider.toolStrategy) {
            is BackAndForth -> BackAndForth(this, response, directive, name, args, callHistory)
            is FillInTheBlank -> FillInTheBlank(this, response)
        }
    }
}