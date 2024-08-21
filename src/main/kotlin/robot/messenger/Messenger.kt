/*
░░░░░░░░░░░░░░░░  ░░░░  ░░        ░░░      ░░░░      ░░░        ░░   ░░░  ░░░      ░░░        ░░       ░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒   ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓        ▓▓      ▓▓▓▓▓      ▓▓▓▓      ▓▓▓      ▓▓▓▓  ▓  ▓  ▓▓  ▓▓▓   ▓▓      ▓▓▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
████████████████  █  █  ██  ██████████████  ████████  ██  ████████  ██    ██  ████  ██  ████████  ███  █████████████████
████████████████  ████  ██        ███      ████      ███        ██  ███   ███      ███        ██  ████  ████████████████
*/
package dev.supachain.robot.messenger

import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.utilities.Debug
import dev.supachain.utilities.toJson
import org.slf4j.LoggerFactory

typealias Conversation = MutableList<Message>
typealias ConversationHistory = MutableList<Conversation>
typealias ConversationKey = Int

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
class Messenger(private val conversations: ConversationHistory = mutableListOf(mutableListOf())) {
    private var currentConversationIndex: ConversationKey = 0
    private val currentConversation get() = conversations[currentConversationIndex]
    private val logger = LoggerFactory.getLogger(Messenger::class.qualifiedName)

    private fun Message.store() = currentConversation.add(this).also { log(this) }

    /**
     * Adds a single message to the current conversation.
     *
     * @param message The message to add.
     * @return This `Messenger` instance for chaining.
     */
    operator fun invoke(message: Message) = this.apply { message.store() }

    /**
     * Adds a list of messages to the current conversation.
     *
     * @param messages The list of messages to add.
     * @return This `Messenger` instance for chaining.
     */
    operator fun invoke(messages: List<Message>) = this.apply { messages.forEach { it.store() } }

    /**
     * Adds multiple messages (using varargs) to the current conversation.
     *
     * @param messages The messages to add.
     * @return This `Messenger` instance for chaining.
     */
    operator fun invoke(vararg messages: Message) = this.apply { messages.forEach { it.store() } }

    override fun toString(): String = currentConversation.joinToString("\n")

    private fun log(message: Message) {
        logger.debug(Debug("Messenger"), "[Messenger]" + message.toJson())
    }

    /**
     * Retrieves the last message in the current conversation.
     *
     * @return The last message, or an `IllegalStateException` if the conversation is empty.
     * @throws IllegalStateException If the current conversation is empty.
     */
    fun lastMessage() = currentConversation.lastOrNull()
        ?: throw IllegalAccessError("Last message is not available")

    fun messages(): List<Message> = currentConversation

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

    /**
     * Finds the last user message in the current conversation.
     *
     * @return The last user message, or throws `NoSuchElementException` if no user message is found.
     * @throws NoSuchElementException If no user message is found in the current conversation.
     */
    fun lastUserMessage(): Message = messages().findLast { it.role == Role.USER }!!
}

