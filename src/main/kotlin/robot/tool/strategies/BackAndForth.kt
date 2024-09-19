package dev.supachain.robot.tool.strategies


import dev.supachain.robot.director.*
import dev.supachain.robot.messenger.ToolResultAction
import dev.supachain.robot.messenger.ToolResultMessage
import dev.supachain.robot.messenger.asSystemMessage
import dev.supachain.robot.provider.models.CommonResponse
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.tool.ToolConfig

/**
 * Represents a tool use strategy where the AI interacts with tools iteratively until a final answer is reached.
 *
 * This data object implements the `ToolUseStrategy` interface and defines the behavior for handling function calls
 * in a back-and-forth manner. It processes responses from the AI provider, executes requested function calls,
 * manages call history, and intervenes when potential loops are detected.
 *
 * @since 0.1.0-alpha

 */
data object BackAndForth : ToolUseStrategy {
    internal class Result(
        override var action: ToolResultAction = ToolResultAction.Complete,
        override val messages: MutableList<Message> = mutableListOf(),
        val callHistory: MutableMap<String, String> = mutableMapOf()
    ) : ToolResultMessage

    /**
     * Invokes the back-and-forth tool use strategy.
     *
     * This function handles the core logic of the `BackAndForth` strategy. It processes the AI provider's response,
     * executes requested function calls, and manages the conversation flow.
     *
     * @param director The `Director` instance responsible for orchestrating the AI interaction.
     * @param response The `CommonResponse` received from the AI provider.
     * @param directive The `Directive` object containing instructions and restrictions for the AI's actions.
     * @param name The name of the current function being executed (for logging).
     * @param args The arguments passed to the function.
     * @param callHistory A mutable map to track the history of function calls and their results.
     */
    internal operator fun invoke(
        director: RobotCore<*, *, *>,
        lastUserMessage: Message,
        response: CommonResponse,
        provider: Provider<*, *>, // TODO Decouple Provider
        result: Result = Result()
    ): ToolResultMessage = with(director) {
        if (response.requestedFunctions.isNotEmpty()) {

            var callStatus: CallStatus = Success
            for ((index, call) in response.requestedFunctions.withIndex()) {
                try {
                    callStatus = call(result.callHistory)
                    when (callStatus) {
                        Success -> {
                            if (
                                index == response.requestedFunctions.lastIndex &&
                                provider.includeSeekCompletionMessage
                            ) result.messages.add(completionMessage)

                            val callResult = result.callHistory.asIterable().last().value
                            result.messages.add(provider.toolResultMessage(callResult))
                            result.action = ToolResultAction.Update
                        }

                        Recalled -> {
                            result.messages.add(toolRecallMessage)
                            result.messages.add(interventionMessage(lastUserMessage, result.callHistory))
                            result.action = ToolResultAction.Retry
                            break
                        }

                        is Error -> {
                            result.messages.add((callStatus.exception.asSystemMessage()))
                            result.action = ToolResultAction.Retry
                            break
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Illegal call: $call in ${response.requestedFunctions}")
                    throw e
                }
            }
        }
        result
    }

    /**
     * Intervenes in the AI's response generation by providing context about previous function calls.
     *
     * This function is triggered when the AI seems to be stuck in a loop during function calls. It takes the following steps:
     *
     * 1. **Fetches Last User Message:** Retrieves the last message sent by the user in the current conversation.
     * 2. **Adds Contextual Information:** Appends a note to the user's message summarizing the results of all previous
     *    function calls made so far. This helps the AI break out of potential loops and reconsider its approach.
     * 3. **Starts New Conversation:** Initiates a new conversation thread within the `Messenger`. This effectively
     *    resets the context and prevents the AI from fixating on the previous loop.
     * 4. **Sends Messages:** Adds the predefined `completionMessageWhenToolFailed` (which likely instructs the AI to
     *    re-evaluate) and the modified user message to the new conversation. This prompts the AI to process the
     *    information again with the added context.
     *
     * @param callMap A map containing the history of function calls and their results.
     *
     * @since 0.1.0-alpha

     */
    private fun interventionMessage(lastUserMessage: Message, callMap: MutableMap<String, String>) =
        lastUserMessage.copy().apply {
            content += " \nNote the following may contain the answer." +
                    "[${callMap.map { "${it.key} has result ${it.value}." }}]. " +
                    "If you see the answer, say it in the desired format."
        }

    private val completionMessage
        get() = ("If you know the final answer after reading the result content from a function call, respond " +
                "with ONLY the answer in the require format")
            .asSystemMessage()

    private val toolRecallMessage
        get() = "You must find the answer in the user message and format it to the required format.".asSystemMessage()

    override fun onRequestMessage(toolSet: List<ToolConfig>): Message? = null
    override fun getTools(tools: List<ToolConfig>): List<ToolConfig> = tools
}