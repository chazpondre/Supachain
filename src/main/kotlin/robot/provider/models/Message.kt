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
 * might provide. It encapsulates the core elements of a response, including:
 *
 * - `message`: The primary message returned by the provider, typically representing the models response or output.
 * - `requestedFunctions`: A list of `FunctionCall` objects, indicating any functions or tools that the
 *    provider is requesting to be executed to fulfill the user's request.
 *
 * Specific implementations of this interface can provide additional details or structure tailored to the capabilities
 * of different AI providers.
 *
 * @since 0.1.0-alpha
 */
@Serializable
sealed interface Message {
    fun functions(): List<FunctionCall> = contents().filterIsInstance<FunctionCall>()
    fun role(): Role = throw NotImplementedError()
    fun text(): TextContent = throw NotImplementedError()
    fun contents(): List<Content> = throw NotImplementedError()

    sealed interface Content
}

@Serializable
@JvmInline
value class TextContent(val value: String) : Message.Content {
    override fun toString() = value
}

@Serializable
@JvmInline
value class FunctionCallContent(val function: FunctionCall) : Message.Content {
    override fun toString() = "fun ${function.name}"
}

@Serializable
data class Base64ImageContent(val base64: String, val mediaType: String) : Message.Content

@Serializable
@JvmInline
value class DocumentContent(val location: String) : Message.Content

/**
 * Represents a message exchanged within a conversation.
 *
 * This class encapsulates essential information about a message, including its:
 *
 * - **Role:** Indicates the origin or purpose of the message, such as `SYSTEM` (instructions),
 *   `USER` (input), `ASSISTANT` (AI-generated response), or `FUNCTION` (function call or result).
 * - **Content:** The actual textual content of the message.
 * - **Name:** (Optional) An identifier for the message sender (e.g., a user's name).
 * - **Call ID:** (Optional) An ID associated with a function call related to this message.
 * - **Tool Calls:** (Optional) A list of tool calls initiated by this message (if the message is from the assistant).
 * - **Function Call:** (Optional) Details of a function call associated with this message.
 * - **Type:**  Indicates the type of content within the message (defaulting to 'Text'). Could be extended to support
 *   other types like 'Image', 'Audio', etc. (Note: this property is marked as `@Transient` for serialization purposes)
 *
 * @property role The role of the message sender.
 * @property text The text content of the message.
 * @property name (Optional) An identifier for the message sender.
 * @property callId (Optional) An ID associated with a function call.
 * @property toolCalls (Optional) A list of tool calls.
 * @property functionCall (Optional) Details of a function call.
 * @property type The type of message content (currently not serialized).
 *
 * @since 0.1.0-alpha
 * @version 1.0.0
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

/**
 * Converts any object into a system message for an AI conversation.
 *
 * This extension function simplifies the creation of system messages by automatically converting
 * any object into a `Message` with the role `Role.SYSTEM`. It uses the `toString()` representation
 * of the object as the message content.
 *
 * @receiver The object to be converted into a system message.
 * @param name An optional name or identifier for the system (defaults to null).
 * @return A `Message` object representing the system message.
 *
 * @since 0.1.0-alpha
 */
internal fun Any.asSystemMessage(): TextMessage = TextMessage(Role.SYSTEM, toString())

/**
 * Converts any object into an assistant message for an AI conversation.
 * This extension function simplifies the creation of assistant messages by automatically converting
 * any object into a Message with the role `Role.ASSISTANT`. It uses the toString() representation
 * of the object as the message content.
 *
 * @receiver The object to be converted into an assistant message.
 * @param name An optional name or identifier for the assistant (defaults to null).
 * @return A Message object representing the assistant message.
 *
 * @since 0.1.0-alpha
 */
internal fun Any.asAssistantMessage(): TextMessage = TextMessage(Role.ASSISTANT, toString())

/**
 * Converts any object into a user message for an AI conversation.
 *
 * This extension function facilitates the creation of user messages by automatically converting
 * any object into a `Message` with the role `Role.USER`. It utilizes the `toString()` representation
 * of the object as the message content.
 *
 * @receiver The object to be transformed into a user message.
 * @param name An optional name or identifier for the user (defaults to null).
 * @return A `Message` object representing the user message.
 *
 * @since 0.1.0-alpha
 */
internal fun Any.asUserMessage(): TextMessage = TextMessage(Role.USER, toString())