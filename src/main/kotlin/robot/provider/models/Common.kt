package dev.supachain.robot.provider.models

import dev.supachain.robot.tool.ToolConfig
import dev.supachain.utilities.Parameter
import kotlinx.serialization.Serializable

fun List<ToolConfig>.asOpenAITools() = map { CommonTool.OpenAI(it) }
interface CommonTool {
    @Serializable
    data class OpenAI(val type: String, val function: OpenAIFunction) {


        constructor(toolConfig: ToolConfig) : this("function", OpenAIFunction(toolConfig))
    }


    @Serializable
    data class OpenAIFunction(val name: String, val description: String?, val parameters: Parameters) {
        @Serializable
        data class Parameters(
            val type: String,
            val properties: Map<String, Property>,
            val required: List<String>
        ) {
            constructor(parameters: List<Parameter>) : this(
                "object",
                parameters.toProperties(),
                parameters.filter { it.required }.map { it.name }
            )
        }

        constructor(toolConfig: ToolConfig) :
                this(
                    toolConfig.function.name,
                    toolConfig.function.description.ifBlank { null },
                    Parameters(toolConfig.function.parameters)
                )
    }
}