/*
░░░░░░░░░░░░░░░░░░░░░       ░░░        ░░░      ░░░       ░░░░      ░░░   ░░░  ░░░      ░░░        ░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓       ▓▓▓      ▓▓▓▓▓      ▓▓▓       ▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████████████  ███  ███  ██████████████  ██  ████████  ████  ██  ██    ████████  ██  ███████████████████████████
█████████████████████  ████  ██        ███      ███  █████████      ███  ███   ███      ███        █████████████████████
 */

@file:Suppress("unused")

package dev.supachain.robot.provider.models

import dev.supachain.robot.messenger.Role
import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.utilities.toJson
import kotlinx.serialization.Serializable


/**
 * Sealed interface representing a common response from an AI provider.
 *
 * This interface serves as a base for different types of responses that different AI providers
 * might provide. It encapsulates the core elements of a response.
 *
 * @property functions Returns a list of FunctionCall objects requested by the provider.
 * @property role Returns the role of the message sender.
 * @property text Returns the text content of the message.
 * @property contents Returns a list of contents in the message.
 *
 * @since 0.1.0
 */
@Serializable
sealed interface Message {
    fun functions(): List<FunctionCall> = contents().filterIsInstance<FunctionCall>()
    fun role(): Role = throw NotImplementedError()
    fun text(): TextContent = throw NotImplementedError()
    fun contents(): List<Content> = throw NotImplementedError()

    /**
     * Define a sealed interface for message content.
     */
    sealed interface Content
}

/** Represents a text content in a message from an AI provider */
@Serializable
@JvmInline
value class TextContent(val value: String) : Message.Content {
    override fun toString() = value
}

/** Represents a function call in a message from an AI provider */
@Serializable
@JvmInline
value class FunctionCallContent(val function: FunctionCall) : Message.Content {
    override fun toString() = "fun ${function.name}"
}

/** Represents Image in a message from an AI provider */
@Serializable
data class Base64ImageContent(val base64: String, val mediaType: String) : Message.Content

/** Represents Document in a message from an AI provider */
@Serializable
@JvmInline
value class DocumentContent(val location: String) : Message.Content

/**
 * Represents a text message exchanged within a conversation.
 *
 * This class encapsulates essential information about a text message.
 *
 * @property role The role of the message sender.
 * @property content The text content of the message.
 *
 * @since 0.1.0
 */
@Serializable
data class TextMessage(
    var role: Role,
    var content: String?,
) : Message {
    override fun toString() = this.toJson()
    override fun functions(): List<FunctionCall> = emptyList()
    override fun role(): Role = this.role
    override fun text() = TextContent(content ?: "")
    override fun contents(): List<Message.Content> = listOf(text())
}

/** Converts any object into a system message for an AI conversation. */
internal fun Any.asSystemMessage(): TextMessage = TextMessage(Role.SYSTEM, toString())

/**Converts any object into an assistant message for an AI conversation. */
internal fun Any.asAssistantMessage(): TextMessage = TextMessage(Role.ASSISTANT, toString())

/** Converts any object into a user message for an AI conversation. */
internal fun Any.asUserMessage(): TextMessage = TextMessage(Role.USER, toString())