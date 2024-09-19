/*
░░░░░░░░░░░░░░░░  ░░░░  ░░        ░░░      ░░░░      ░░░        ░░   ░░░  ░░░      ░░░        ░░       ░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒   ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓        ▓▓      ▓▓▓▓▓      ▓▓▓▓      ▓▓▓      ▓▓▓▓  ▓  ▓  ▓▓  ▓▓▓   ▓▓      ▓▓▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
████████████████  █  █  ██  ██████████████  ████████  ██  ████████  ██    ██  ████  ██  ████████  ███  █████████████████
████████████████  ████  ██        ███      ████      ███        ██  ███   ███      ███        ██  ████  ████████████████
*/
@file:Suppress("unused")

package dev.supachain.robot.messenger

import dev.supachain.robot.director.RobotCore
import dev.supachain.robot.director.RobotInterface
import dev.supachain.robot.director.directive.Directive
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.provider.Feature
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.provider.models.CommonResponse
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.BackAndForth
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.utilities.Debug
import dev.supachain.utilities.toJson
import org.slf4j.LoggerFactory

typealias Conversation = MutableList<Message>
typealias ConversationHistory = MutableList<Conversation>
typealias ConversationKey = Int

enum class MessageFilter {
    OnlyUserMessages,
    OnlySystemMessages,
    None
}

enum class ToolResultAction {
    Retry,
    Update,
    Complete,
    ReplaceAndComplete
}

interface ToolResultMessage {
    var action: ToolResultAction
    val messages: MutableList<Message>
}

/**
 * Manages and stores chat messages for multiple conversations.
 *
 * This class provides a convenient way to store and retrieve messages in the context of multiple ongoing
 * conversations. It maintains a list of conversations, each represented as a list of `Message` objects.
 * You can add messages, switch between conversations, and access messages for a specific conversation.
 *
 * @property conversations A list of conversation histories, each being a list of `Message` objects.
 *
 * @since 0.1.0-alpha

 */
class Messenger<ResponseType : CommonResponse> internal constructor
    (private val provider: Provider<ResponseType, *>, maxMessages: Int = 10) {

    private val responses: MutableList<ResponseType> = mutableListOf()
    private val conversations: ConversationHistory = mutableListOf(mutableListOf())
    private val logger = LoggerFactory.getLogger(Messenger::class.qualifiedName)

    private var currentConversationIndex: ConversationKey = 0
    private val currentConversation get() = conversations[currentConversationIndex]
    private fun Message.store() = this.also { currentConversation.add(this) }.log()
    private fun Message.log() = this.also { log(this) }

    private fun log(message: Message) =
        logger.debug(Debug("Messenger"), "[Messenger]" + message.toJson())

    fun messages(): List<Message> = currentConversation

    internal suspend
    fun send(
        args: Array<Any?>,
        director: RobotCore<*, *, *>,
        filter: MessageFilter,
        directive: Directive,
    ): Message {
        setupMessages(args, directive, director, filter)
        return getResult(director, directive.feature)
    }

    private fun setupMessages(
        args: Array<Any?>,
        directive: Directive,
        director: RobotInterface,
        filter: MessageFilter
    ) {
        // Get All Messages
        val messageList = mutableListOf<Message>().apply {
            // Add Config Messages
            addAll(directive.rankedConfigMessages)
            // Formatting Message
            if (provider.useFormatMessage) add(directive.formattingMessage())
            // Tool Strategy Messages
            provider.toolStrategy.onRequestMessage(director.allTools)?.also { add(it) }
            // Argument Messages
            addAll(directive.argumentMessages(args, provider.userMessagePrimer))
        }

        // Filter Messages & Store
        messageList.filter {
            when (filter) {
                MessageFilter.OnlyUserMessages -> it.role == Role.USER
                MessageFilter.OnlySystemMessages -> it.role == Role.SYSTEM
                MessageFilter.None -> true
            }
        }.forEach { it.store() }
    }

    private tailrec suspend
    fun getResult(director: RobotCore<*, *, *>, feature: Feature, toolResult: ToolResultMessage? = null): Message {
        // Handle Response
        val response = handleResponse(feature, director.allTools)
        // Handle Tool Calls
        val result: ToolResultMessage = handleToolCalling(director, response, toolResult)
        // Store Messages
        result.messages.forEach { it.store() }

        // Handle Action
        return when (result.action) {
            ToolResultAction.Retry -> getResult(director, feature, result)
            ToolResultAction.Update -> getResult(director, feature, result)
            ToolResultAction.Complete -> lastMessage()
            ToolResultAction.ReplaceAndComplete -> lastMessage().apply {
                currentConversation.removeAt(currentConversation.lastIndex - 1)
            }
        }
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

     */
    private suspend fun handleResponse(feature: Feature, tools: List<ToolConfig>): ResponseType {
        val response = provider.request(feature, tools)
        responses.add(response)
        response.topMessage.data.store()
        logger.debug(Debug("Messenger"), "Response: \n{}", response)
        return response
    }

    private fun Messenger<ResponseType>.handleToolCalling(
        director: RobotCore<*, *, *>,
        response: ResponseType,
        toolResult: ToolResultMessage?
    ): ToolResultMessage = when (provider.toolStrategy) {
        is BackAndForth -> BackAndForth(director, lastUserMessage(), response, provider, toolResult as BackAndForth.Result)
        is FillInTheBlank -> FillInTheBlank(director, response)
    }

    fun finalResponse() = lastMessage().content ?: ""

    /**
     * Adds a single message to the current conversation.
     *
     * @param message The message to add.
     * @return This `Messenger` instance for chaining.
     */
    operator fun invoke(message: Message) = this.apply { message.store() }

    /**
     * Retrieves the last message in the current conversation.
     *
     * @return The last message, or an `IllegalStateException` if the conversation is empty.
     * @throws IllegalStateException If the current conversation is empty.
     */
    private fun lastMessage() = currentConversation.lastOrNull()
        ?: throw IllegalAccessError("Last message is not available")

    /**
     * Finds the last user message in the current conversation.
     *
     * @return The last user message, or throws `NoSuchElementException` if no user message is found.
     * @throws NoSuchElementException If no user message is found in the current conversation.
     */
    private fun lastUserMessage(): Message = currentConversation.findLast { it.role == Role.USER }!!

    /**
     * Creates a new conversation and switches to it.
     *
     * @return This `Messenger` instance for chaining.
     */
    fun newConversation() = this.apply {
        conversations.add(mutableListOf())
        currentConversationIndex = conversations.size - 1
    }

    /**
     * Switches to the conversation at the specified index.
     *
     * @param index The index of the conversation to switch to.
     * @return This `Messenger` instance for chaining.
     * @throws IllegalArgumentException If the index is invalid (out of bounds).
     */
    fun switchConversation(index: Int) = this.apply {
        require(index >= 0 && index < conversations.size) { "Invalid conversation index" } // Validate index
        currentConversationIndex = index //
    }
}
