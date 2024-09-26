/*
░░░░░░░░░░░░░░░░░░░░░       ░░░        ░░       ░░░        ░░░      ░░░        ░░░      ░░░       ░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓       ▓▓▓      ▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████████████  ████  █████  █████  ███  ███  ████████  ████  █████  █████  ████  ██  ███  ██████████████████████
█████████████████████       ███        ██  ████  ██        ███      ██████  ██████      ███  ████  █████████████████████
*/

package dev.supachain.robot.director

import dev.supachain.robot.answer.Answer
import dev.supachain.robot.director.directive.Directive
import dev.supachain.robot.director.directive.Objective
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.tool.ToolMap
import dev.supachain.utilities.Debug
import dev.supachain.utilities.RunsInBackground
import dev.supachain.utilities.by
import dev.supachain.utilities.createObjectFromNoArgClass
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KVisibility

internal interface RobotInterface {
    val defaultProvider: Provider<*>
    val directives: MutableMap<String, Directive>
    val toolMap: ToolMap
    val allTools get() = toolMap.values.toList()
}

/**
 * Core class for orchestrating AI interactions and managing tool execution.
 *
 * This class serves as the central hub for communication between the user, the AI provider,
 * and the available tools. It handles the following key responsibilities:
 *
 * - **Directive Management:** Stores a collection of `Directive` objects that define the possible
 *   actions or operations the AI can perform based on user requests.
 * - **Tool Integration:** Maintains a `ToolMap` that associates tool names with their
 *   configurations, allowing dynamic tool execution.
 * - **Message Handling:** Utilizes a `Messenger` to manage the conversation history between
 *   the user and the AI, including system instructions, user prompts, AI responses, and tool outputs.
 * - **Provider Interaction:** Communicates with the AI provider using the `defaultProvider`,
 *   sending requests and receiving responses.
 * - **Tool Execution:** Provides a `toolProxy` object for facilitating the execution of tool functions
 *   when requested by the AI.
 *
 * @param P The type of the AI provider used for generating responses and potentially executing tool calls.
 * @param API The interface defining the methods available for interacting with the AI and its tools.
 * @param ToolType The type of the tool class containing the available tool functions.
 *
 * @property defaultProvider The default AI provider for handling requests.
 * @property toolMap A map of tool names to their configurations.
 * @property directives A map of directive names to their corresponding `Directive` objects.
 * @property toolProxy A proxy object for executing tool functions.
 * @property toolProxyObject A lazy-initialized function that creates an instance of the `ToolType` class
 *   containing the tool functions.
 *
 * @since 0.1.0
 */
data class RobotCore<P : Provider<*>, API : Any, ToolType : Any>(
    override var defaultProvider: P,
    override val toolMap: ToolMap = mutableMapOf(),
    override val directives: MutableMap<String, Directive> = mutableMapOf(),
    override val job: CompletableJob = SupervisorJob(),
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job,
) : FunctionHandling<ToolType>, RobotInterface, RunsInBackground {

    override val toolProxy by lazy { toolProxyObject() }
    lateinit var toolProxyObject: () -> ToolType

    val logger: Logger by lazy { LoggerFactory.getLogger(RobotCore::class.java) }

    /**
     * Registers a tool interface within the RobotCore, enabling access to its functionalities.
     *
     * This function integrates a tool interface into the RobotCore by analyzing its methods and annotations.
     * It supports method chaining by returning the RobotCore instance itself.
     *
     * **Parameters:**
     * - `ToolInterface`: The tool interface to register. This should contain methods annotated with `@Tool`
     *   and/or `@Parameters` or be a class labeled `@Toolset`.
     *
     * **Returns:**
     * - A reference to the current `RobotCore` instance for method chaining.
     *
     * **Throws:**
     * - `IllegalArgumentException` if the `ToolInterface` is not publicly accessible.
     *
     * @since 0.1.0
     */
    inline fun <reified ToolInterface : ToolType> setUpToolset(): RobotCore<P, API, *> = this.also {
        toolProxyObject = {
            if (ToolInterface::class.visibility != KVisibility.PUBLIC)
                throw IllegalArgumentException("Your class must not have private visibility")
            createObjectFromNoArgClass<ToolInterface>()
        }

        ToolInterface::class.getToolMethods()
            .map { it.toToolConfig() }
            .forEach { toolMap[it.function.name] = it }

        logger.debug(
            Debug("Director"), "[Configuration]\nUses Toolset: {},\nTools: {}",
            ToolInterface::class.simpleName, toolMap
        )
    }

    /**
     * Sets up directives by mapping the methods of the API interface to their corresponding `Directive` objects.
     *
     * Creates a proxy instance of the specified API interface that intercepts method calls, which are
     * then handled using the registered directives and tool configurations.
     *
     * @param API The type of the API interface.
     * @return A proxy instance implementing the specified API interface.
     *
     * @since 0.1.0
     */
    inline fun <reified API : Any> setUpDirectives(): API {
        val directiveInterface = API::class
        directives.putAll(directiveInterface.getDirectives().associateBy { it.name })

        return directiveInterface by { parent, name, args, returnType ->
            Answer<Any>(returnType, scope.async { handleDirectiveRequest(parent, name, args) })
        }
    }


    /**
     * Processes a directive request, sends it to the provider, and returns the result.
     *
     * This method handles core logic for directive execution:
     * 1. Directive Retrieval: Retrieves the directive by name from the `directives` map.
     * 2. Objective Formation: Forms an `Objective` from the directive and arguments.
     * 3. Send Message: Sends the request to the provider's `messenger`.
     * 4. Extract Result: Extracts and returns the final response.
     *
     * @param parent The parent context initiating the request (used for logging).
     * @param name The directive name to execute.
     * @param args The arguments to pass to the directive function.
     * @return The final message content representing the AI's response.
     * @throws IllegalStateException If the directive is not found.
     * @throws Exception If any error occurs during processing.
     *
     * @since 0.1.0
     */
    suspend
    fun handleDirectiveRequest(parent: String, name: String, args: Array<Any?>): String {
        try {
            // Get Corresponding Directive
            val directive = directives[name] ?: throw IllegalStateException("Method $name not found in methodLookUp")
            val objective = Objective(directive, args)

            // Debug
            logger.debug(Debug("Director"), objective.toString())

            // Get Provider
            val provider = defaultProvider
            return with(provider) {
                messenger.send(this@RobotCore, objective)
                messenger.finalResponse().toString()
            }

        } catch (e: Exception) {
            logger.error("Error handling method $name of $parent", e)
            throw e
        }
    }
}