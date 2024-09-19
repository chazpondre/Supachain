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
import dev.supachain.utilities.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KVisibility

internal interface RobotInterface {
    val defaultProvider: Provider<*, *>
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
 * @property toolProxy A proxy object for executing tool functions.
 * @property toolProxyObject A lazy-initialized function that creates an instance of the `ToolType` class
 *                           containing the tool functions.
 *
 * @since 0.1.0-alpha

 */
data class RobotCore<P : Provider<*, *>, API : Any, ToolType : Any>(
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
     * Registers a tool interface within the director, enabling access to its functionalities.
     *
     * This function integrates a tool interface into the director instance by analyzing its methods and their annotations.
     *
     * **Parameters:**
     *  - `ToolInterface`: The type of the tool interface to be registered. This interface should be public and contain
     *  methods annotated with `@Tool` and `@Parameters` or class labelled `@Toolset`.
     *
     * **Returns:**
     *  An instance of `ExtendedClient<CLIENT, API>`, allowing for method chaining for further configuration.
     *
     * **Throws:**
     *  - `IllegalArgumentException` if the provided `ToolInterface` is not publicly accessible.
     *
     * **Process:**
     * 1. **Visibility Check:** Ensures the provided tool interface is public (not private).
     * 2. **Tool Instance Creation:** Attempts to create an instance of the tool interface using a no-argument constructor.
     * 3. **Tool Configuration Extraction:** Analyzes methods within the interface decorated with `@Tool` annotations and extracts their corresponding configurations.
     * 4. **Logging:** Logs information about the registered toolset and its configurations for debugging purposes.
     * 5. **Configuration Storage:** Stores the extracted tool configurations for later use by the director.
     *
     * **Preconditions:**
     *  - The `defaultProvider.toolsAllowed` flag must be enabled to allow tool registration.
     *
     * **By registering a tool interface, you provide the director with access to its functionalities for use within your application.**
     *
     * @since 0.1.0-alpha
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
     * Creates a proxy instance of the specified API interface.
     *
     * This function maps the methods of the API interface to their corresponding [Directive]
     * objects and creates a proxy that intercepts method calls and handles them using the provided
     * toolset and configurations.
     *
     * @param API The type of the API interface.
     * @return A proxy instance implementing the specified API interface.
     *
     * @since 0.1.0-alpha

     * */
    inline fun <reified API : Any> setUpDirectives(): API {
        val directiveInterface = API::class
        directives.putAll(directiveInterface.getDirectives().associateBy { it.name })

        return directiveInterface by { parent, name, args, returnType ->
            Answer<Any>(returnType, scope.async { handleDirectiveRequest(parent, name, args) })
        }
    }

    /**
     * Processes a directive request, handles the provider's response, and returns the final message content.
     *
     * This function is responsible for the core logic of handling directives within the `RobotCore`.
     * It performs the following key steps:
     *
     * 1. **Directive Retrieval:** Fetches the `RobotDirective` associated with the given proxied `name` from the `directives` map.
     *    If the directive is not found, an `IllegalStateException` is thrown.
     * 2. **Provider Interaction:** Sends the provided arguments to the default provider's `messenger`, along with the directive.
     * 3. **Result Extraction:** Retrieves the final response from the `messenger`.
     * 4. **Error Handling:** Catches any exceptions during processing, logs an error message with context,
     *    and rethrows the exception for further handling by the caller.
     *
     * @param parent The name of the parent or context initiating this directive request (used for logging purposes).
     * @param name The name of the directive to be executed.
     * @param args An array of arguments to be passed to the underlying function associated with the directive.
     * @return The content of the final message in the `Messenger`, representing the response or result.
     * @throws IllegalStateException If the specified directive is not found in the `directives` map.
     * @throws Exception If any other error occurs during the processing of the directive.
     *
     * @since 0.1.0-alpha
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
                messenger.finalResponse()
            }

        } catch (e: Exception) {
            logger.error("Error handling method $name of $parent", e)
            throw e
        }
    }
}