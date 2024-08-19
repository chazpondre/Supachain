package dev.supachain.robot.messenger.messaging

import dev.supachain.robot.tool.ToolConfig
import dev.supachain.utilities.SerializeWrite
import dev.supachain.utilities.Parameter
import dev.supachain.utilities.toJSONSchemaType
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes a list of `ToolConfig` objects into a format compatible with OpenAI function calls.
 *
 * This serializer transforms a list of `ToolConfig` objects, which represent tool configurations, into
 * a list of `OpenAiFunctionSchema` objects. This conversion is necessary for proper communication with the
 * OpenAI API, which expects function descriptions in a specific format.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
internal class OpenAIFunctionListSerializer : SerializeWrite<List<ToolConfig>> {
    override val descriptor: SerialDescriptor = ListSerializer(OpenAIFunctionSchema.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: List<ToolConfig>) {
        encoder.encodeSerializableValue(
            ListSerializer(OpenAIFunctionSchema.serializer()),
            value.map { OpenAIFunctionSchema(it) })
    }
}

/**
 * Represents a tool function description in the format expected by servers using the OpenAI Schema
 *
 * @property name The name of the tool function.
 * @property description A description of the tool function's purpose and functionality.
 * @property parameters A `OpenAIFunctionParams` object containing the parameter details.
 *
 * @since 0.1.0-alpha
 */
@Serializable
internal data class OpenAIFunctionSchema(
    val name: String,
    val description: String,
    val parameters: OpenAIFunctionParams
) {
    constructor(toolConfig: ToolConfig) : this(
        toolConfig.function.name,
        toolConfig.function.description,
        OpenAIFunctionParams(toolConfig.function.parameters)
    )
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
 * This data class provides information about the type of a parameter expected by an OpenAI function,
 * as well as a description for better understanding of the parameter's purpose.
 *
 * @property type The JSON Schema type of the parameter (e.g., "string", "integer").
 * @property description A human-readable description of the parameter's purpose and expected format.
 *
 * @since 0.1.0-alpha
 */
@Serializable
internal data class TypeDescription(val type: String, val description: String)
