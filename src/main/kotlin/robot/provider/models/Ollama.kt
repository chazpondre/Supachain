package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.*
import dev.supachain.robot.NetworkOwner
import dev.supachain.robot.director.DirectorCore
import dev.supachain.robot.messenger.Role
import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.provider.CommonChatRequest
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.post
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.provider.tools.OpenAIToolSend
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import dev.supachain.utilities.mapToFunctionCall
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

class Ollama : Provider<Ollama>(), OllamaActions, NetworkOwner {
    var chatModel: String = "llama3.1"
    val stream: Boolean = false

    override val name: String get() = "Ollama"
    override var url: String = "http://localhost:11434"
    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = FillInTheBlank
    override val toolResultMessage: (result: String) -> Message =
        { Message(Role.FUNCTION, it, name) }

    val network: NetworkConfig = NetworkConfig()
    override val networkClient: NetworkClient by lazy { KTORClient(network) }

    override val self: () -> Ollama get() = { this }

    interface Api {
        @Serializable
        data class ChatRequest(
            val model: String,
            val messages: List<Message>,
            @Serializable(with = OpenAIToolSend::class)
            val tools: List<ToolConfig> = emptyList(),
            val stream: Boolean
        ) : CommonChatRequest
    }
}

private sealed interface OllamaActions : NetworkOwner, Actions, Extension<Ollama> {
    override suspend fun chat(director: DirectorCore): OllamaChatResponse = with(self()) {
        return post(
            "$url/api/chat",
            Ollama.Api.ChatRequest(chatModel, director.messages, director.tools, stream)
        )
    }
}

@Serializable
data class OllamaChatResponse(
    val model: String,
    val message: OllamaMessage,
    val done: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("total_duration")
    val totalDuration: Long,
    @SerialName("load_duration")
    val loadDuration: Long,
    @SerialName("prompt_eval_count")
    val promptEvalCount: Int,
    @SerialName("prompt_eval_duration")
    val promptEvalDuration: Long,
    @SerialName("eval_count")
    val evalCount: Int,
    @SerialName("eval_duration")
    val evalDuration: Long,
    @SerialName("done_reason")
    val doneReason: String?
) : CommonResponse {
    @Serializable
    data class OllamaMessage(
        val role: String,
        val content: String,
        @SerialName("tool_calls")
        val toolCalls: List<ToolCall>? = emptyList()
    ) {
        @Serializable
        data class ToolCall(
            val function: Function
        ) {
            @Serializable
            data class Function(
                val name: String,
                val arguments: Map<String, JsonElement>
            )
        }
    }

    override val rankMessages: List<Message.FromAssistant> by lazy {
        listOf(Message.FromAssistant(Message(Role.ASSISTANT, message.content)))
    }

    override val requestedFunctions: List<FunctionCall>
        get() = message.toolCalls?.map { FunctionCall(it.function.arguments.mapToFunctionCall(), it.function.name) }
            ?: emptyList()
}
