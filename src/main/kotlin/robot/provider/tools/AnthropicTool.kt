package dev.supachain.robot.provider.tools

import dev.supachain.robot.tool.ToolConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class AnthropicTool: ToolListSend<AnthropicTool.Schema>() {
    @Serializable
    data class Schema(
        var name: String,
        var description: String,
        @SerialName("input_schema")
        var inputSchema: OpenAIToolSendSchema.Function.Parameters
    )

    override fun toolOut(toolConfig: ToolConfig) = Schema(
        toolConfig.function.name,
        toolConfig.function.description,
        toolConfig.function.parameters.asParameters()
    )

    override fun outSerializer(): KSerializer<Schema> = Schema.serializer()
}