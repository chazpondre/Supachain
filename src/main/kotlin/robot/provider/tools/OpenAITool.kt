package dev.supachain.robot.provider.tools

import dev.supachain.robot.tool.ToolConfig
import dev.supachain.utilities.Parameter
import dev.supachain.utilities.toJSONSchemaType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class OpenAITool: SerialToolList<OpenAITool.Schema>() {
    @Serializable
    data class Schema(
        var name: String,
        var description: String,
        var parameters: OpenAIFunctionParams
    )

    override fun serialTool(toolConfig: ToolConfig) = Schema(
        toolConfig.function.name,
        toolConfig.function.description,
        OpenAIFunctionParams(toolConfig.function.parameters)
    )

    override fun toolSerializer(): KSerializer<Schema> = Schema.serializer()
}

/**
 * Represents the parameters of a tool function in the format expected by servers using the OpenAI Schema.
 *
 * It's specifically designed to match the OpenAI API requirements for describing function parameters.
 *
 * @property type The general type of the parameters ("object" in most cases).
 * @property properties A map of property names to their corresponding `TypeDescription` objects.
 * @property required A list of property names that are required for the function call.
 *
 * @since 0.1.0-alpha
 */
@Serializable
internal data class OpenAIFunctionParams(
    val type: String,
    val properties: MutableMap<String, TypeDescription>,
    val required: List<String>
) {
    constructor(parameters: List<Parameter>) :
            this("object", mutableMapOf(), parameters.filter { it.required }.map { it.name }) {
        parameters.forEach { properties[it.name] = TypeDescription(it.type.toJSONSchemaType(), it.description) }
    }
}

/**
 * Describes the type and description of a parameter in JSON Schema format.
 *
 * This data class provides information about the type of parameter expected by an OpenAI function,
 * as well as a description for better understanding of the parameter's purpose.
 *
 * @property type The JSON Schema type of the parameter (e.g., "string", "integer").
 * @property description A human-readable description of the parameter's purpose and expected format.
 *
 * @since 0.1.0-alpha
 */
@Serializable
internal data class TypeDescription(val type: String, val description: String)
