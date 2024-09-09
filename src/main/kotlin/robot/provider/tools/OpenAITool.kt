package dev.supachain.robot.provider.tools

import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.messenger.messaging.ToolCall
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.utilities.Parameter
import dev.supachain.utilities.toJSONSchemaType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class OpenAIToolSendSchema(
    val type: String,
    val function: Function
) {
    @Serializable
    internal data class Function(
        var name: String,
        var description: String,
        var parameters: Parameters
    ) {
        @Serializable
        internal data class Parameters(
            val properties: MutableMap<String, TypeDescription>,
            val required: List<String>,
            val type: String = "object"
        ) {
            @Serializable
            internal data class TypeDescription(val type: String, val description: String)
        }
    }
}

@Serializable
data class OpenAIToolReceiveSchema(
    val id: String? = null,   // optional
    val type: String? = null, // optional
    val function: Function
){
    @Serializable
    data class Function(
        val name: String,
        val arguments: Map<String, String>
    )
}

internal open class OpenAIToolReceive : ToolListReceive<OpenAIToolReceiveSchema>() {
    // Converters
    override fun toolIn(tool: OpenAIToolReceiveSchema): ToolCall {
        val arguments = tool.function.arguments.map { it.value }.joinToString()
        return ToolCall(tool.function.name, "function", FunctionCall(arguments, tool.function.name))
    }

    // Serializers
    override fun inSerializer(): KSerializer<OpenAIToolReceiveSchema> = OpenAIToolReceiveSchema.serializer()
}

internal open class OpenAIToolSend : ToolListSend<OpenAIToolSendSchema>() {
    // Converters
    override fun toolOut(toolConfig: ToolConfig) = OpenAIToolSendSchema(
        "function",
        OpenAIToolSendSchema.Function(
            toolConfig.function.name,
            toolConfig.function.description,
            toolConfig.function.parameters.asParameters()
        )
    )

    // Serializers
    override fun outSerializer(): KSerializer<OpenAIToolSendSchema> = OpenAIToolSendSchema.serializer()

    // Utils
    private fun List<Parameter>.asParameters(): OpenAIToolSendSchema.Function.Parameters {
        val required = this.filter { it.required }.map { it.name }
        val propMap = mutableMapOf<String, OpenAIToolSendSchema.Function.Parameters.TypeDescription>().also { properties ->
            this.forEach {
                properties[it.name] = OpenAIToolSendSchema.Function.Parameters.TypeDescription(it.type.toJSONSchemaType(), it.description)
            }
        }
        return OpenAIToolSendSchema.Function.Parameters(propMap, required)
    }
}