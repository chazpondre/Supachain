/*
░░░░░░░░░░░░░░░░░░░░░       ░░░        ░░       ░░░        ░░░      ░░░        ░░░      ░░░       ░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓       ▓▓▓      ▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████████████  ████  █████  █████  ███  ███  ████████  ████  █████  █████  ████  ██  ███  ██████████████████████
█████████████████████       ███        ██  ████  ██        ███      ██████  ██████      ███  ████  █████████████████████
*/

package dev.supachain.robot.director

import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.director.directive.Directive
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.tool.ToolMap
import dev.supachain.utilities.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

internal interface DirectorCore {
    val defaultProvider: Provider<*>
    val directives: MutableMap<String, Directive>
    val toolMap: ToolMap
    val messenger: Messenger
    var defaultSeed: Int?
}

/**
 * Core class for orchestrating AI interactions and managing tool execution.
 *
 * This class serves as the central hub for communication between the user, the AI provider,
 * and the available tools. It handles the following key responsibilities:
 *
 * - **Directive Management:** Stores a collection of `Directive` objects that define the possible
 *    actions or operations the AI can perform based on user requests.
 * - **Tool Integration:** Maintains a `ToolMap` that associates tool names with their
 *    configurations (`ToolConfig`), allowing for dynamic tool execution.
 * - **Message Handling:** Utilizes a `Messenger` to store and manage the conversation history
 *   between the user and the AI, including system instructions, user prompts, AI responses, and tool outputs.
 * - **Provider Interaction:**  Communicates with the AI provider using the `defaultProvider`,
 *   sending requests and receiving responses.
 * - **Tool Execution:**  Provides a `toolProxy` object to facilitate the execution of tool functions
 *   when requested by the AI.
 *
 * @param P The type of the AI provider used for generating responses and potentially executing tool calls.
 * @param API The interface defining the methods available for interacting with the AI and its tools.
 * @param ToolType The type of the tool class containing the available tool functions.
 *
 * @property defaultProvider The default AI provider for handling requests.
 * @property toolMap A map of tool names to their configurations.
 * @property directives A map of directive names to their corresponding `Directive` objects.
 * @property defaultSeed An optional seed value for controlling the randomness of AI-generated responses.
 * @property messenger An instance of `Messenger` for managing conversation history.
 * @property toolProxy A proxy object for executing tool functions.
 * @property toolProxyObject A lazy-initialized function that creates an instance of the `ToolType` class
 *                           containing the tool functions.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
data class Director<P : Provider<*>, API : Any, ToolType : Any>(
    override var defaultProvider: P,
    override val toolMap: ToolMap = mutableMapOf(),
    override val directives: MutableMap<String, Directive> = mutableMapOf(),
    override var defaultSeed: Int? = null,
    override val job: CompletableJob = SupervisorJob(),
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job,
    override val messenger: Messenger = Messenger(),
) : DirectorCore, RunsInBackground