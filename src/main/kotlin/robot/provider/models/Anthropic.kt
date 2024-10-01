package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.*
import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.messenger.Role
import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.messenger.messaging.ToolCall
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
abstract class AnthropicBuilder : Provider<AnthropicBuilder>(), NetworkOwner {
    var apiKey: String = ""
    var beta: String = ""
    var modelName: String = ""
    var maxTokens: Int = 2048
    var temperature: Double = 0.0
    var topP: Double = 0.0
    var topK: Int = 0
    var version: String = "2023-06-01"
    val stream: Boolean = false
    abstract var chatModel: String

    // Network
    val network: NetworkConfig = NetworkConfig()
    override var url: String = "https://api.anthropic.com/v1"

    // Provider
    override var name: String = "Anthropic"
    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = FillInTheBlank
}

@Suppress("unused")
class Anthropic : AnthropicBuilder(), AnthropicAPI, AnthropicModels, AnthropicActions {
    override val actions: Actions = this
    override var chatModel: String = models.chat.claude35Sonnet_20240620
    internal val headers
        get() = mutableMapOf(
            "x-api-key" to apiKey,
            "anthropic-version" to version
        ).apply { if (beta.isNotBlank()) put("x-beta", beta) }

    override val networkClient: NetworkClient by lazy { KTORClient(network) }

    override val self: () -> Anthropic get() = { this }

    override var messenger: Messenger = Messenger(this)

    override fun onReceiveMessage(message: CommonMessage) {
        messenger.send(AnthropicAPI.AnthropicMessage(message))
    }

    override fun onToolResult(toolCall: ToolCall, result: String) {
        val last = messenger.lastMessage()
        if (last is AnthropicAPI.AnthropicMessage) {
            val lastContent = last.content!!.last()
            val id = if (lastContent is AnthropicAPI.Content.ToolUse) lastContent.id
            else throw IllegalStateException("Expected ToolUse but got $lastContent, result received: $result")

            val content = AnthropicAPI.Content.ToolResult(id, result)
            messenger.send(AnthropicAPI.AnthropicMessage(Role.USER, listOf(content)))
        } else throw IllegalStateException("There is no Anthropic message matching tool call message for the tool call result of $result")
    }
}

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░       ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████████████████████████        ██  ███████████  █████████████████████████████████████████████████
██████████████████████████████████████████████  ████  ██  ████████        ██████████████████████████████████████████████
*/
internal interface AnthropicAPI : Extension<Anthropic> {
    @Serializable
    data class AnthropicMessage(
        val role: Role,
        val content: List<Content>? = null,
    ) : CommonMessage {
        constructor(message: CommonMessage) : this(message.role(), message.contents().asAnthropicContent())

        override fun text() =
            TextContent((content?.filterIsInstance<Content.Text>())?.joinToString { it.text } ?: "")

        override fun role(): Role = role
        override fun contents(): List<CommonMessage.Content> =
            content?.filter { it !is Content.ToolUse }?.map { it.asMessageContent() } ?: emptyList()

        override fun calls(): List<ToolCall> = content?.filterIsInstance<Content.ToolUse>()?.map {
            ToolCall(it.id, "function", FunctionCall(it.input.toJson(false), it.name))
        } ?: emptyList()
    }

    @Serializable
    sealed interface Content {
        fun asMessageContent(): CommonMessage.Content

        @Serializable
        @SerialName("text")
        data class Text(val text: String) : Content {
            override fun asMessageContent() = TextContent(text)
        }

        @Serializable
        @SerialName("tool_use")
        data class ToolUse(val id: String, val name: String, val input: Map<String, String>) : Content {
            override fun asMessageContent(): CommonMessage.Content =
                FunctionCallContent(FunctionCall(input.toJson(false),name))
        }

        @Serializable
        @SerialName("tool_result")
        data class ToolResult(
            @SerialName("tool_use_id")
            val id: String,
            val content: String
        ) : Content {
            override fun asMessageContent(): CommonMessage.Content = TextContent(content)
        }

        @Serializable
        @SerialName("image")
        data class Image(val source: ImageSource) : Content {
            @Serializable
            data class ImageSource(
                val type: String, // This can be "base64"
                @SerialName("media_type")
                val mediaType: String, // The media type of the image (e.g., image/jpeg)
                val data: String // Base64 encoded image string
            )

            override fun asMessageContent(): CommonMessage.Content = Base64ImageContent(source.data, source.type)
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
    ) /*: Message*/ {
        @Serializable
        data class Usage(
            @SerialName("input_tokens") val inputTokens: Int,
            @SerialName("output_tokens") val outputTokens: Int
        )
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
     * @since 0.1.0
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

internal fun List<CommonMessage>.asAnthropicMessage() = this.map {
    if (it is AnthropicAPI.AnthropicMessage) it
    else AnthropicAPI.AnthropicMessage(it)
}

internal fun List<CommonMessage.Content>.asAnthropicContent(): List<AnthropicAPI.Content> = mapNotNull {
    when (it) {
        is Base64ImageContent -> AnthropicAPI.Content.Image(
            AnthropicAPI.Content.Image.ImageSource("base64", it.mediaType, it.base64)
        )

        is TextContent -> AnthropicAPI.Content.Text(it.value)
        is DocumentContent -> null
        is FunctionCallContent -> null
    }
}

private sealed interface AnthropicActions : NetworkOwner, Actions, Extension<Anthropic> {
    override suspend fun chat(tools: List<ToolConfig>): AnthropicAPI.AnthropicMessage = with(self()) {
        val response: AnthropicAPI.ChatResponse = post(
            "$url/messages",
            AnthropicAPI.ChatRequest(
                chatModel, maxTokens, temperature, tools.asAnthropicTools(), stream,
                messenger.messages().asAnthropicMessage(),
            ), headers
        )

        return AnthropicAPI.AnthropicMessage(response.role, response.content)
    }
}

