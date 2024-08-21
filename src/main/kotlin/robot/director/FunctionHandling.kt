package dev.supachain.robot.director

import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.messenger.asFunctionMessage
import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.tool.ToolMap
import dev.supachain.utilities.*
import kotlin.reflect.KFunction

@Suppress("EmptyMethod")
interface FunctionHandling<T> {
    val toolMap: ToolMap
    val messenger: Messenger
    val toolProxy: T

    /**
     * Executes a function call within a tool invocation process, preventing redundant calls.
     *
     * This function ensures controlled execution of functions during tool usage. It performs the following checks and actions:
     *
     * 1. **Information Gathering:** Extracts the Kotlin `KFunction` object, argument array, and a unique string representation of the call from the provided `FunctionCall` object.
     * 2. **Call History Check:** Checks if the function call (identified by its unique string representation) has already been made by looking it up in the `callHistory` map. If found, a `Recalled` result is returned to prevent unnecessary repetition.
     * 3. **Function Execution:** If the call hasn't been made before, the function is invoked using the provided arguments and the `toolProxy` object.
     * 4. **Messaging and Logging:** Wraps the function call result in a `CommonFunctionMessage` and sends it to the `messenger` for further processing. Additionally, the function call and its result are logged in the `callHistory` map for tracking purposes.
     * 5. **Success Indication:** If the execution is successful, a `Success` result is returned.
     * 6. **Error Handling:** If an exception occurs during execution, an `Error` result is returned, encapsulating the encountered exception.
     *
     * **Parameters:**
     *  - `callHistory`: A mutable map used to store the history of function calls and their results, preventing redundant executions.
     *
     * **Returns:**
     *  A `CallResult` object indicating the outcome of the function call:
     *    - `Success`: Function call executed successfully.
     *    - `Recalled`: Function call already executed previously.
     *    - `Error`: An exception occurred during function execution.
     *
     * @since 0.1.0-alpha
     */
    operator fun FunctionCall.invoke(callHistory: MutableMap<String, String>): CallResult {
        val (function, arguments, callString) = this.info()

        return if (callString in callHistory) Recalled
        else try {
            function(toolProxy, * arguments).let {
                // Add Function Call message to Messenger
                this@FunctionHandling.messenger(it.asFunctionMessage(name))
                // Register Call
                callHistory[callString] = it.toString()
                Success
            }
        } catch (e: Exception) {
            Error(e)
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
     * Extracts essential details about a function call for tool execution.
     *
     * This function parses a `FunctionCall` object representing a tool invocation and retrieves the following information:
     *
     * 1. **Tool Function:** Retrieves the corresponding Kotlin `KFunction` object from the `toolMap`, ensuring it represents a valid tool function.
     * 2. **Function Arguments:** Constructs an array of `Any?` arguments based on the function's parameters and the values provided in the `FunctionCall` object.
     * 3. **Call String:** Generates a human-readable string representation of the function call, including the function name and its arguments.
     *
     * **Parameters:**
     *  - `FunctionCall`: The `FunctionCall` object containing information about the tool invocation.
     *  - `argFormatter` (Optional): A custom function for formatting arguments based on a parameter map. Defaults to `formatArgumentsFromJson`, which formats arguments assuming JSON representation.
     *
     * **Returns:**
     *  A `Triple` containing the following elements:
     *    - `KFunction<*>`: The Kotlin function object representing the tool function.
     *    - `Array<Any?>`: An array containing the argument values for the function call.
     *    - `String`: A string representing the function call, including the function name and arguments.
     *
     * **Throws:**
     *  - `IllegalStateException`: If the requested tool or its function definition is not found in the `toolMap`.
     *
     * @since 0.1.0-alpha
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
sealed interface CallResult

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

