@file:Suppress("unused")

package dev.supachain.robot.tool

import dev.supachain.utilities.Parameter
import dev.supachain.utilities.getShortName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.KFunction

/**
 * Data class representing the configuration of a tool function.
 *
 * This class encapsulates the essential information about a tool function,
 * including its name, description, parameters, and associated metadata.
 *
 * @property name The unique identifier or name of the tool function.
 * @property description A concise explanation of the tool function's purpose or functionality.
 * @property parameters A list of [Parameter] objects describing the input parameters required by the function.
 * @property annotations (Internal) A list of annotation names associated with the function. This property
 * is marked as `@Transient` and is not intended for direct external use. It might be used internally for reflection or analysis.
 * @property kFunction (Internal) A reference to the actual Kotlin `KFunction` object representing the tool function.
 * This property is marked as `@Transient` and is likely used for dynamic invocation of the function at runtime.
 *
 * @constructor Creates a new `RobotTool` instance.
 *
 * @since 0.1.0-alpha

 */
@Serializable
data class RobotTool(
    val name: String,
    val description: String,
    val parameters: List<Parameter>,
    @Transient val annotations: List<String> = emptyList(),
    @Transient val kFunction: KFunction<*>? = null
) {
    fun toToolConfig() = ToolConfig( this)
    /**
     * Generates a Kotlin-like function signature string from the tool configuration.
     *
     * This function constructs a string representation of the tool function's signature,
     * including its name, parameter types, and return type (if available).
     *
     * @return A string resembling a Kotlin function declaration for the tool.
     */
    fun asKFunctionString() =
        "fun ${name}(${
            parameters.joinToString(", ") {
                "${it.name}: ${it.type.getShortName()}"
            }
        })" + ":${kFunction?.returnType?.getShortName()}"
}
/**
 * A type alias representing a set of tool configurations.
 *
 * Tool configurations describe the capabilities and parameters of tools
 * that can be used by the AI model to perform actions or access external information.
 *
 * @since 0.1.0-alpha

 */
typealias ToolMap = MutableMap<String, ToolConfig>

/**
 * Data class representing the configuration of a tool.
 *
 * This class defines the type of the tool and the specific function
 * (represented by `RobotTool`) that the tool can execute.
 *
 * @property type The type of tool (e.g., FUNCTION).
 * @property function The configuration details of the tool's function, including its name, description,
 * parameters, and other metadata.
 *
 * @since 0.1.0-alpha

 */
@Serializable
data class ToolConfig(val function: RobotTool, val type: ToolType = ToolType.FUNCTION) {
    /**
     * Generates a Kotlin-like function signature string from the tool configuration.
     *
     * This function constructs a string representation of the tool function's signature,
     * including its name, parameter types, and return type (if available).
     *
     * @return A string resembling a Kotlin function declaration for the tool.
     */
    fun asKFunctionString() = function.asKFunctionString()
}

/**
 * Generates a comma-separated string of Kotlin-like function signatures from a list of `ToolConfig` objects.
 *
 * This extension function iterates through a list of `ToolConfig` and uses the `asKFunctionString()` method
 * of each `ToolConfig` to create a string representation of its corresponding function signature.
 * These signatures are then joined with commas to form a single string.
 *
 * @receiver A list of `ToolConfig` objects.
 * @return A comma-separated string containing Kotlin-like function signatures for each tool in the list.
 *
 * @since 0.1.0-alpha

 */
fun List<ToolConfig>.asKFunctionString() = joinToString(", ") { it.asKFunctionString() }

/**
 * Enum representing the types of tools available.
 *
 * Currently, only the `FUNCTION` tool type is supported.
 * This indicates a tool that executes a specific function based on its configuration.
 *
 * @since 0.1.0-alpha

 */
@Serializable
enum class ToolType {
    @SerialName("function")
    FUNCTION,
}

/**
 * Enum representing the choices for how the AI model should use tools.
 *
 * - `AUTO`: The model can automatically decide whether to use a tool.
 * - `NONE`: The model will not use any tools.
 * - `REQUIRED`: The model is required to use at least one tool.
 *
 * @since 0.1.0-alpha

 */
@Serializable
enum class ToolChoice {
    @SerialName("auto")
    AUTO,

    @SerialName("none")
    NONE,

    @SerialName("required")
    REQUIRED
}