package dev.supachain.robot.director

import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.messenger.asFunctionMessage
import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.tool.ToolMap
import dev.supachain.utilities.*
import kotlin.reflect.KFunction

internal interface FunctionHandling<T> : Extensions {
    val toolMap: ToolMap
    val messenger: Messenger
    val toolProxy: T

    /**
     * Executes a function call, managing restrictions and call history.
     *
     * This function handles the execution of a function call within a tool invocation process. It performs the following steps:
     *
     * 1. **Information Extraction:** Retrieves the Kotlin `KFunction` object, the arguments array, and a string representation
     *    of the call from the `FunctionCall` object.
     * 2. **Restriction Check:** Iterates through the list of `DiscreteRestriction` objects to verify whether the
     *    arguments violate any restrictions defined for the function. If a restriction is violated, a `BreaksRestriction`
     *    result is returned, indicating the violated restriction and the problematic argument.
     * 3. **Call History Check:** If the function call (identified by its unique string representation) has already been made,
     *    it returns a `Recalled` result, preventing redundant executions.
     * 4. **Function Execution:** If the call is valid and hasn't been made before, the function is executed using the provided
     *    arguments and the `toolProxy`.
     * 5. **Message Sending and Logging:** The result of the function call is wrapped in a `CommonFunctionMessage` and sent
     *    to the `messenger`. The function call and its result are also recorded in the `callHistory` map.
     * 6. **Success Indication:** A `Success` result is returned to signal the successful execution of the function call.
     *
     * @param call The `FunctionCall` object representing the function to be executed.
     * @param callHistory A map to track the history of function calls and their results.
     * @param restrictions A list of `DiscreteRestriction` objects defining the allowed values and constraints for function parameters.
     * @return A `CallResult` indicating the outcome of the function call: `Success` if executed successfully,
     *         `BreaksRestriction` if a restriction was violated, or `Recalled` if the call has already been made.
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    // For recurrent function calling
    operator fun FunctionCall.invoke(callHistory: MutableMap<String, String>): CallResult {
        val (function, arguments, callString) = this.info()

        if (callString in callHistory) return Recalled
        else try {
            return function(toolProxy, * arguments).let {
                // Add Function Call message to Messenger
                this@FunctionHandling.messenger(it.asFunctionMessage(name))
                // Register Call
                callHistory[callString] = it.toString()
                Success
            }
        } catch (e: Exception) {
            return Error(e)
        }
    }

    /**
     * Invokes a tool function represented by a `FunctionCall` object.
     *
     * This operator function provides a convenient way to execute a tool function dynamically at runtime.
     * It performs the following steps:
     *
     * 1. **Information Extraction:** Uses the `getInfo` function to retrieve details about the function call, including:
     *    - The Kotlin `KFunction` object representing the function to be called.
     *    - An array of arguments extracted and converted from the `FunctionCall`'s arguments string.
     *    - A string representation of the function call (not used in this specific implementation).
     * 2. **Argument Conversion:**
     *    - Splits the `FunctionCall`'s arguments string into individual argument strings.
     *    - For each argument:
     *        - Determines the argument type (literal, function call, value, or vararg).
     *        - Converts the argument string to the appropriate Kotlin type based on the parameter information.
     *        - Handles nested function calls recursively.
     * 3. **Function Invocation:** Calls the extracted `KFunction` with the converted arguments, using the `toolProxy` as the receiver.
     * 4. **Result Return:** Returns the result of the function invocation.
     *
     * @receiver The `FunctionCall` object representing the function to be invoked.
     * @return The result of the function call, or `null` if the function's return type is `Unit`.
     *
     * @throws IllegalStateException If the tool or its function definition is not found.
     * @throws IllegalArgumentException If any argument conversion fails or an unsupported argument type is encountered.
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    operator fun FunctionCall.invoke(): Any? {
        val (function, arguments, _) = info { paramMap ->
            val parameters = paramMap.values.map { it.second }
            splitArguments().mapIndexed { i, value ->
                val parameter = parameters[i]
                val argumentType = if (parameter.kParameter.isVararg) ArgumentType.VarArg else value.argumentType()

                when (argumentType) {
                    is ArgumentType.Literal -> value.asParameterType(parameter)
                    ArgumentType.FunctionCall -> value.asFunctionCall()()
                    ArgumentType.Value -> TODO()// value.asValueCall()()
                    ArgumentType.VarArg -> value.asVarargType(parameter)
                }

            }.toTypedArray()
        }

        return function(toolProxy, * arguments)
    }

    /**
     * Extracts information about a function call for tool execution.
     *
     * This function takes a `FunctionCall` object representing a tool invocation and retrieves the following:
     *
     * 1. The corresponding Kotlin `KFunction` object from the `toolMap`, ensuring it's a valid tool.
     * 2. An array of `Any?` arguments prepared based on the function's parameters and the arguments specified in the `FunctionCall`.
     * 3. A string representation of the function call, including the function name and its arguments.
     *
     * @param functionCall The `FunctionCall` object to extract information from.
     * @return A `Triple` containing the `KFunction`, arguments array, and a string representation of the function call.
     *
     * @throws IllegalStateException If the tool or its function definition is not found.
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    fun FunctionCall.info(argFormatter: String.(ParamMap) -> Array<Any?> = { formatArgumentsFromJson(it) }): Triple<KFunction<*>, Array<Any?>, String> {
        val tool = toolMap[name]?.function ?: throw IllegalStateException("Unknown tool call $name")
        val function = tool.kFunction ?: throw IllegalStateException("Unknown tool function for ${tool.name}")

        val parameterMap: ParamMap = tool.parameters.foldIndexed(mutableMapOf())
        { i, map, parameter -> map.also { it[parameter.name] = Pair(i, parameter) } }

        val arguments = arguments.argFormatter(parameterMap)
        val callString = "${function.name}(${arguments.joinToString(", ")})"
        return Triple(function, arguments, callString)
    }
}

private typealias ParamMap = Map<String, Pair<Int, Parameter>>

/**
 * Represents the result of attempting to call a tool function.
 *
 * This sealed interface defines three possible outcomes:
 *  - `Success`: The function call was executed successfully.
 *  - `Recalled`: The function call has already been made with the same arguments and was not re-executed.
 *  - `Error`: The function call failed due to an exception.
 */
internal sealed interface CallResult

/**
 * Indicates that a function call was executed successfully.
 */
internal data object Success : CallResult

/**
 * Indicates that a function call was not executed because it has already been made with the same arguments.
 */
internal data object Recalled : CallResult

/**
 * Indicates that a function call failed due to an exception.
 *
 * @property exception The exception that occurred during the function call.
 */
internal class Error(val exception: Exception) : CallResult

