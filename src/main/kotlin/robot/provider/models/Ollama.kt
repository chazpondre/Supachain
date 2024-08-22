package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.*
import dev.supachain.robot.NetworkOwner
import dev.supachain.robot.director.DirectorCore
import dev.supachain.robot.provider.CommonChatRequest
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.messenger.messaging.OpenAIFunctionListSerializer
import dev.supachain.robot.post
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.provider.responses.OllamaChatResponse
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import kotlinx.serialization.Serializable

class Ollama : Provider<Ollama>(), OllamaActions, NetworkOwner {
    var chatModel: String = "llama3.1"
    val stream: Boolean = false

    override val name: String get() = "Ollama"
    override var url: String = "http://localhost:11434"
    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = FillInTheBlank


    val network: NetworkConfig = NetworkConfig()
    override val networkClient: NetworkClient by lazy { KTORClient(network) }

    override val self: () -> Ollama get() = { this }
}

private interface OllamaApi : Extension<LocalAI> {
    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        @Serializable(with = OpenAIFunctionListSerializer::class)
        val tools: List<ToolConfig> = emptyList(),
        val stream: Boolean
    ) : CommonChatRequest
}

private sealed interface OllamaActions : NetworkOwner, Actions, Extension<Ollama> {
    override suspend fun chat(director: DirectorCore):
            OllamaChatResponse = with(self()) {
        return post(
            "$url/api/chat",
            OllamaApi.ChatRequest(chatModel, director.messages, director.tools, stream)
        )
    }
}

