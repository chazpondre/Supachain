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
import dev.supachain.robot.director.directive.Objective
import dev.supachain.robot.provider.Feature
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.provider.models.Message
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.BackAndForth
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.utilities.Debug
import org.slf4j.LoggerFactory

typealias Conversation = MutableList<Message>
typealias ConversationHistory = MutableList<Conversation>
typealias ConversationKey = Int

/**
 * Enum representing filters that can be applied to messages in a conversation.
 *
 * This filter helps determine which messages to include when processing or displaying conversations.
 * It can filter for only user messages, only system messages, or include all messages.
 *
 * - `OnlyUserMessages`: Includes only messages sent by the user.
 * - `OnlySystemMessages`: Includes only messages sent by the system.
 * - `None`: Includes all messages without filtering.
 *
 * @since 0.1.0
 */
enum class MessageFilter {
    OnlyUserMessages,
    OnlySystemMessages,
    None
}

/**
 * Enum representing the possible actions that can be taken after processing a tool result.
 *
 * The actions dictate how the conversation should proceed or be updated after the tool has completed its operation.
 * - `Retry`: Indicates that the tool should be retried with the same or updated input.
 * - `Update`: Indicates that the current result should be updated and reprocessed.
 * - `Complete`: Indicates that the current result is final and no further processing is needed.
 * - `ReplaceAndComplete`: Indicates that the last result should be replaced and marked as final.
 *
 * @since 0.1.0
 */
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
 * @since 0.1.0
 */
class Messenger internal constructor
    (private val provider: Provider<*>, maxMessages: Int = 10) {

    private val conversations: ConversationHistory = mutableListOf(mutableListOf())
    private val logger = LoggerFactory.getLogger(Messenger::class.qualifiedName)

    private var currentConversationIndex: ConversationKey = 0
    private val currentConversation get() = conversations[currentConversationIndex]
    private fun Message.store() = this.also { currentConversation.add(this) }.log()
    private fun Message.log() = this.also { log(this) }
    private fun<T: List<Message>> T.filtered() = this.filter {
        when (provider.messageFilter) {
            MessageFilter.OnlyUserMessages -> it.role() == Role.USER
            MessageFilter.OnlySystemMessages -> it.role() == Role.SYSTEM
            MessageFilter.None -> true
        }
    }

    private fun log(message: Message) =
        logger.debug(Debug("Messenger"), "[Messenger]{}", message)

    fun messages(): List<Message> = currentConversation

    /**
     * Sends a message to a Provider using the provided objective.
     *
     * This function initiates the message exchange by setting up the appropriate
     * messages (including configurations, formatting, and strategies) and then obtaining
     * the response through the provider's tool strategies. The response is processed and filtered.
     *
     * @param robot The `RobotCore` instance that can do requested tasks.
     * @param objective The objective containing the directives and configuration for the message.
     * @return The final response `Message` obtained after processing.
     * @since 0.1.0
     */
    internal suspend
    fun send(robot: RobotCore<*, *, *>, objective: Objective): Message {
        setupMessages(robot, objective)
        return getResult(robot, objective.feature)
    }

    /**
     * Adds a single message to the current conversation.
     *
     * @param message The message to add.
     * @return This `Messenger` instance for chaining.
     */
    internal fun send(message: Message) = message.store()

    /**
     * Prepares and sets up all the messages to be sent based on the given objective and robot context.
     *
     * This method collects and organizes various types of messages, including configuration, formatting, tool strategy,
     * and argument messages, before sending them for further processing. The messages are filtered before being passed
     * to the provider for additional handling.
     *
     * @param robot The `RobotInterface` instance containing tools and configurations used to handle the request.
     * @param objective The `Objective` representing the goal or task, which contains relevant messages and settings.
     *
     * @since 0.1.0
     */
    private fun setupMessages(
        robot: RobotInterface,
        objective: Objective
    ) {
        // Get All Messages
        val messageList = mutableListOf<Message>().apply {
            // Add Config Messages
            objective.rankedConfigMessages.also { addAll(it) }
            // Formatting Message
            if (provider.useFormatMessage) objective.formattingMessage().also { add(it) }
            // Tool Strategy Messages
            provider.toolStrategy.onRequestMessage(robot.allTools)?.also { add(it) }
            // Argument Messages
            objective.argumentMessages(provider.userMessagePrimer).also { addAll(it) }
        }

        // Filter Messages & Store
        messageList.filtered().forEach { provider.onReceiveMessage(it) }
    }

    /**
     * Recursively processes and retrieves the final message result for a given objective, feature, and tool result.
     *
     * This function manages the flow of conversation by:
     * 1. Handling responses from the provider.
     * 2. Invoking tools and processing their results through a defined tool strategy.
     * 3. Filtering messages and sending them to the provider for further handling.
     * 4. Recursively determining the appropriate action to take based on the result's action (Retry, Update, Complete, etc.).
     *
     * The function operates recursively when a `Retry` or `Update` action is returned until a final message is reached.
     *
     * @param robot The `RobotCore` instance that contains the tools and context for processing the request.
     * @param feature The `Feature` representing the current task or functionality being executed.
     * @param toolResult Optional. A previous `ToolResultMessage` that may influence the current state of the conversation.
     * @return The final `Message` after processing all relevant actions.
     *
     * @since 0.1.0
     */
    private tailrec suspend
    fun getResult(robot: RobotCore<*, *, *>, feature: Feature, toolResult: ToolResultMessage? = null): Message {
        // Handle Response
        val response = handleResponse(feature, robot.allTools)
        // Handle Tool Calls
        val result: ToolResultMessage = handleToolCalling(robot, response, toolResult)
        // Send filtered messages to Provider
        result.messages.filtered().forEach { provider.onReceiveMessage(it) }
        // Clear
        result.messages.clear()

        // Handle Action
        return when (result.action) {
            ToolResultAction.Retry -> getResult(robot, feature, result)
            ToolResultAction.Update -> getResult(robot, feature, result)
            ToolResultAction.Complete -> lastMessage()
            ToolResultAction.ReplaceAndComplete -> lastMessage().apply {
                currentConversation.removeAt(currentConversation.lastIndex - 1)
            }
        }
    }

    /**
     * Handles the response from the default language model provider and delegates further processing based on the tool strategy.
     *
     * This function processes the response received from the default LLM provider.

     * @param feature The AI feature to be executed (e.g., `Feature.Chat`, `Feature.Embedding`).
     * @param tools A list of `ToolConfig` that may be used by the Provider during the request.
     *
     * @return A `Message` object containing the response from the provider.
     *
     * @since 0.1.0-alpha
     */
    private suspend fun handleResponse(feature: Feature, tools: List<ToolConfig>): Message {
        val response = provider.request(feature, tools)
        response.store()
        logger.debug(Debug("Messenger"), "Response: \n{}", response)
        return response
    }

    /**
     * Handles tool interaction logic and processes the results.
     *
     * Based on the current tool strategy (e.g., `BackAndForth` or `FillInTheBlank`), this method either
     * handles the tool calls and retries, or processes the response directly.
     *
     * @param robot The `RobotCore` instance managing the tools.
     * @param response The message response from the provider.
     * @param toolResult Optionally, the result of the previous tool interaction.
     * @return A `ToolResultMessage` with the processed result.
     * @since 0.1.0
     */
    private fun Messenger.handleToolCalling(
        robot: RobotCore<*, *, *>,
        response: Message,
        toolResult: ToolResultMessage?
    ): ToolResultMessage = when (provider.toolStrategy) {
        is BackAndForth -> BackAndForth(
            robot,
            lastUserMessage(),
            response,
            provider,
            toolResult as BackAndForth.Result?
        )

        is FillInTheBlank -> FillInTheBlank(robot, response)
    }


    /**
     * Retrieves the final response text of the current conversation.
     *
     * This method returns the final message text in the current conversation.
     *
     * @return The text of the final response message.
     * @since 0.1.0
     */
    fun finalResponse() = lastMessage().text()

    /**
     * Retrieves the last message in the current conversation.
     *
     * @return The last message, or an `IllegalStateException` if the conversation is empty.
     * @throws IllegalStateException If the current conversation is empty.
     */
    internal fun lastMessage() = currentConversation.lastOrNull()
        ?: throw IllegalAccessError("Last message is not available")

    /**
     * Finds the last user message in the current conversation.
     *
     * @return The last user message, or throws `NoSuchElementException` if no user message is found.
     * @throws NoSuchElementException If no user message is found in the current conversation.
     */
    private fun lastUserMessage(): Message = currentConversation.findLast { it.role() == Role.USER }!!

    /**
     * Creates a new conversation and switches to it.
     *
     * @return This `Messenger` instance for chaining.
     */
    internal fun newConversation() = this.apply {
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
    internal fun switchConversation(index: Int) = this.apply {
        require(index >= 0 && index < conversations.size) { "Invalid conversation index" } // Validate index
        currentConversationIndex = index //
    }
}
