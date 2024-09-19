package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.*
import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.messenger.Role
import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.messenger.messaging.asAssistantMessage
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.CommonChatRequest
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.provider.tools.AnthropicTool
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import dev.supachain.utilities.toJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("unused")
interface AnthropicModels {
    val models get() = Models

    /**
     * Provides convenient access to OpenAI model names for various tasks.
     *
     * This object serves as a central repository for OpenAI model identifiers,
     * categorized by their primary functions (chat, embedding, moderation).
     *
     * @since 0.1.0-alpha
     */
    object Models {
        /** Models suitable for chat completions. */
        val chat = Chat
    }

    /**
     * Object containing names for OpenAI's chat models.
     */
    @Suppress("unused")
    object Chat {
        val claude35Sonnet_20240620 get() = "claude-3-5-sonnet-20240620"
        val claude3Sonnet_20240229 get() = "claude-3-sonnet-20240229"
        val claude3Haiku_20240307 get() = "claude-3-haiku-20240307"
        val claude3Opus_20240229 get() = "claude-3-opus-20240229"
    }
}

sealed interface AnthropicMessage : CommonMessage

@Suppress("unused")
class Anthropic : Provider<AnthropicMessage, Anthropic>(), AnthropicAPI, AnthropicActions, NetworkOwner,
    AnthropicModels {
    var apiKey: String = ""
    var beta: String = ""
    var modelName: String = ""
    var maxTokens: Int = 2048
    var temperature: Double = 0.0
    var topP: Double = 0.0
    var topK: Int = 0
    override var url: String = "https://api.anthropic.com/v1"
    var version: String = "2023-06-01"
    override val toolResultMessage: (result: String) -> Message =
        { Message(Role.FUNCTION, it) }

    var chatModel: String = models.chat.claude35Sonnet_20240620
    val stream: Boolean = false

    override val name: String get() = "Anthropic"
    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = FillInTheBlank
    override var messenger: Messenger<AnthropicMessage> = Messenger(this)

    internal val headers
        get() = mutableMapOf(
            "x-api-key" to apiKey,
            "anthropic-version" to version
        ).apply {
            if (beta.isNotBlank()) put("x-beta", beta)
        }

    val network: NetworkConfig = NetworkConfig()
    override val networkClient: NetworkClient by lazy { KTORClient(network) }

    override val self: () -> Anthropic get() = { this }
}

private interface AnthropicAPI : Extension<Anthropic> {
    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        @SerialName("max_tokens")
        val maxTokens: Int,
        val temperature: Double,
        @Serializable(with = AnthropicTool::class)
        val tools: List<ToolConfig> = emptyList(),
        val stream: Boolean? = null
    ) : CommonChatRequest
}

private sealed interface AnthropicActions : NetworkOwner, Actions<AnthropicMessage>, Extension<Anthropic> {
    override suspend fun chat(tools: List<ToolConfig>): AnthropicChatResponse = with(self()) {
        return post(
            "$url/messages",
            AnthropicAPI.ChatRequest(chatModel, messenger.messages(), maxTokens, temperature, tools, stream),
            headers
        )
    }
}

@Serializable
data class AnthropicChatResponse(
    val content: List<Content>,
    val id: String,
    val model: String,
    val role: Role,
    @SerialName("stop_reason") val stopReason: String? = null,
    @SerialName("stop_sequence") val stopSequence: String? = null,
    val type: String? = null,
    val usage: Usage? = null
) : AnthropicMessage {
    @Serializable
    data class Content(
        val type: String,
        val text: String? = null,
        val id: String? = null,
        val name: String? = null,
        val input: Map<String, String>? = null
    )

    @Serializable
    data class Usage(
        @SerialName("input_tokens") val inputTokens: Int,
        @SerialName("output_tokens") val outputTokens: Int
    )

    override fun message(): Message = (content.last().text ?: "").asAssistantMessage()

    override fun functions(): List<FunctionCall> = content.mapNotNull {
        if (it.type == "tool_use") FunctionCall(
            it.input!!.toJson(false),
            it.name!!
        ) else null
    }
}
