/*
░░░░░░░░░░░░░░░░░░░░░       ░░░        ░░░      ░░░       ░░░░      ░░░   ░░░  ░░░      ░░░        ░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓       ▓▓▓      ▓▓▓▓▓      ▓▓▓       ▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████████████  ███  ███  ██████████████  ██  ████████  ████  ██  ██    ████████  ██  ███████████████████████████
█████████████████████  ████  ██        ███      ███  █████████      ███  ███   ███      ███        █████████████████████
 */

@file:Suppress("unused")

package dev.supachain.robot.provider.responses

import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.messenger.messaging.Message

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
sealed interface CommonResponse {
    val rankMessages: List<Message.FromAssistant>
    val topMessage get() = rankMessages.first()
    val requestedFunctions: List<FunctionCall>
}

