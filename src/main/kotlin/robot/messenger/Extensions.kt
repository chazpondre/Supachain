package dev.supachain.robot.messenger

import dev.supachain.robot.messenger.messaging.Message


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
internal fun Any.asSystemMessage(name: String? = null): Message = Message(Role.SYSTEM, toString(), name)

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
internal fun Any.asAssistantMessage(name: String? = null): Message = Message(Role.ASSISTANT, toString(), name)

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
internal fun Any.asUserMessage(name: String? = null): Message = Message(Role.USER, toString(), name)