package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.*
import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.messenger.Role
import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.CommonChatRequest
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import dev.supachain.utilities.Parameter
import dev.supachain.utilities.toJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░       ░░░  ░░░░  ░░        ░░  ░░░░░░░░       ░░░        ░░       ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓       ▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓      ▓▓▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████  ████  ██  ████  █████  █████  ████████  ████  ██  ████████  ███  ███████████████████████████
██████████████████████████       ████      ███        ██        ██       ███        ██  ████  ██████████████████████████
*/
@Suppress("unused")
class Anthropic : Provider<Anthropic>(), AnthropicAPI, AnthropicActions, NetworkOwner,
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
    override val toolResultMessage: (result: String) -> TextMessage =
        { TextMessage(Role.FUNCTION, it) }

    var chatModel: String = models.chat.claude35Sonnet_20240620
    val stream: Boolean = false

    override val name: String get() = "Anthropic"
    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = FillInTheBlank
    override var messenger: Messenger = Messenger(this)

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

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░       ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████████████████████████        ██  ███████████  █████████████████████████████████████████████████
██████████████████████████████████████████████  ████  ██  ████████        ██████████████████████████████████████████████
*/



interface AnthropicAPI : Extension<Anthropic> {


    @Serializable
    data class AnthropicMessage(val role: Role, val content: List<Content>) {
        constructor(message: Message) : this(message.role(), message.contents().asAnthropicContent())
    }

    @Serializable
    sealed interface Content {
        @Serializable
        @SerialName("text")
        data class Text(val text: String) : Content

        @Serializable
        @SerialName("image")
        data class Image(val source: ImageSource) : Content {
            @Serializable
            data class ImageSource(
                val type: String, // This can be "base64"
                @SerialName("media_type") val mediaType: String, // The media type of the image (e.g., image/jpeg)
                val data: String // Base64 encoded image string
            )
        }
    }
    /*
    ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░  ░░░░  ░░░      ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
    ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
    ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓        ▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
    ███████████████████████████████████████  ████  ██  ████  ██        █████  ██████████████████████████████████████████
    ████████████████████████████████████████      ███  ████  ██  ████  █████  ██████████████████████████████████████████
    */


    @Serializable
    data class ChatRequest(
        val model: String,
        @SerialName("max_tokens")
        val maxTokens: Int,
        val temperature: Double,
        val tools: List<Tool> = emptyList(),
        val stream: Boolean? = null,
        val messages: List<AnthropicMessage>,
    ) : CommonChatRequest {
        @Serializable
        data class Tool(
            var name: String,
            var description: String,
            @SerialName("input_schema")
            var inputSchema: Parameters
        ) {
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
        }
    }


    @Serializable
    data class ChatResponse(
        val id: String,
        val model: String,
        val role: Role,
        @SerialName("stop_reason") val stopReason: String? = null,
        @SerialName("stop_sequence") val stopSequence: String? = null,
        val type: String? = null,
        val usage: Usage? = null,
        val content: List<Content>
    ) : Message {
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

        override fun text() = TextContent(content.last().text ?: "")
        override fun role(): Role = Role.ASSISTANT
        override fun contents(): List<Message.Content> = listOf(text())
        override fun functions(): List<FunctionCall> = content.mapNotNull {
            if (it.type == "tool_use") FunctionCall(
                it.input!!.toJson(false),
                it.name!!
            ) else null
        }

        override fun toString() = this.toJson()
    }
}

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  ░░░░  ░░░      ░░░       ░░░        ░░  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒   ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓        ▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓  ▓▓      ▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
████████████████████████████████████  █  █  ██  ████  ██  ████  ██  ████████  ██████████████████████████████████████████
████████████████████████████████████  ████  ███      ███       ███        ██        ████████████████████████████████████
 */
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

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
 */

private fun List<ToolConfig>.asAnthropicTools() = map {
    with(it.function) {
        AnthropicAPI.ChatRequest.Tool(
            name, description, AnthropicAPI.ChatRequest.Tool.Parameters(parameters)
        )
    }
}

internal fun List<Message>.asAnthropicMessage() = this.map { AnthropicAPI.AnthropicMessage(it) }

internal fun List<Message.Content>.asAnthropicContent(): List<AnthropicAPI.Content> = mapNotNull {
    when (it) {
        is Base64ImageContent -> AnthropicAPI.Content.Image(
            AnthropicAPI.Content.Image.ImageSource(
                "base64",
                it.mediaType,
                it.base64
            )
        )

        is TextContent -> AnthropicAPI.Content.Text(it.value)
        is DocumentContent -> null
    }
}

private sealed interface AnthropicActions : NetworkOwner, Actions, Extension<Anthropic> {
    override suspend fun chat(tools: List<ToolConfig>): AnthropicAPI.ChatResponse = with(self()) {
        return post(
            "$url/messages",
            AnthropicAPI.ChatRequest(
                chatModel, maxTokens, temperature, tools.asAnthropicTools(), stream,
                messenger.messages().asAnthropicMessage(),
            ),
            headers
        )
    }
}

