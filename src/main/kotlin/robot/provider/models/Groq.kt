package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.*
import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.messenger.Role
import dev.supachain.robot.messenger.messaging.ToolCall
import dev.supachain.robot.messenger.messaging.Usage
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.CommonChatRequest
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.provider.models.GroqAPI.ChatResponse.Error
import dev.supachain.robot.tool.ToolChoice
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import dev.supachain.utilities.toJson
import io.ktor.http.*
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
abstract class GroqBuilder : Provider<GroqBuilder>(), NetworkOwner, GroqModels {
    abstract var model: String
    var apiKey: String = ""
    var frequencyPenalty = 0.0
    var logitBias: Map<String, Int>? = null
    var logProbabilities: Boolean? = null
    var maxTokens: Int = 2048
    var numberOfChoices: Int? = null
    var presencePenalty: Double = 0.0
    var responseFormat: GroqResponseFormat? = null
    var seed: Int? = null
    var stream: Boolean = false
    var stop: List<String>? = null
    var temperature: Double = 0.0
    var topP: Double = 0.0
    var toolChoice = ToolChoice.AUTO
    var user: String? = null

    // Network
    val network: NetworkConfig = NetworkConfig()
    override var url: String = "https://api.groq.com/openai/v1"

    // Provider
    override var name: String = "Groq"
    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = FillInTheBlank
}

@SerialName("type")
enum class GroqResponseFormat {
    @SerialName("text")
    TEXT,

    @SerialName("json_object")
    JSON
}

@Suppress("unused")
internal interface GroqAPI : Extension<Groq> {
    @Serializable
    data class Message(
        val role: Role,
        val content: String? = null,
        @SerialName("tool_calls")
        val toolCalls: List<ToolCall>? = null,
        val name: String? = null,
        @SerialName("tool_call_id")
        val toolCallID: String? = null,
    ) : CommonMessage {
        constructor(message: CommonMessage) : this(message.role(), message.text().value)

        override fun role(): Role = role
        override fun text(): TextContent = TextContent(content ?: "")
        override fun calls(): List<ToolCall> = toolCalls ?: emptyList()
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
        @SerialName("frequency_penalty")
        val frequencyPenalty: Double,
        @SerialName("logit_bias")
        val logitBias: Map<String, Int>?,
        val logProbabilities: Boolean?,
        val model: String,
        val messages: List<Message>,
        @SerialName("max_tokens")
        val maxTokens: Int,
        val n: Int? = null,
        @SerialName("presence_penalty")
        val presencePenalty: Double,
        val responseFormat: GroqResponseFormat?,
        val seed: Int? = null,
        val stream: Boolean? = null,
        val stop: List<String>?,
        val temperature: Double,
        @SerialName("top_p")
        var topP: Double = 1.0,
        val tools: List<CommonTool.OpenAI> = emptyList(),
        @SerialName("tool_choice")
        val toolChoice: ToolChoice,
        val user: String? = null
    ) : CommonChatRequest

    @Serializable
    data class ChatResponse(
        val id: String,
        @SerialName("object")
        val type: String,
        val created: Int,
        val model: String,
        val choices: List<Choice>,
        val usage: Usage,
        @SerialName("service_tier")
        val serviceTier: String? = null,
        val systemFingerprint: String? = null,
        val completion: Boolean? = null,
        val error: Error? = null
    ) {
        @Serializable
        data class Choice(
            val index: Int,
            val message: Message
        )

        @Serializable
        data class Error(
            val code: Int? = null,
            val message: String,
            val type: String,
            val param: String? = null
        )
    }

    @Serializable
    data class ErrorResponse(
        val error: Error? = null
    )
}

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  ░░░░  ░░░      ░░░       ░░░        ░░  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒   ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓        ▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓  ▓▓      ▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
████████████████████████████████████  █  █  ██  ████  ██  ████  ██  ████████  ██████████████████████████████████████████
████████████████████████████████████  ████  ███      ███       ███        ██        ████████████████████████████████████
 */
@Suppress("unused")
interface GroqModels {

    val models get() = Models

    object Models {
        val chat = Chat
        val audio = Audio
        val vision = Vision
    }

    object Chat {
        // Llama 3 Groq Models
        val llama3Groq70BToolUsePreview: String get() = "llama3-groq-70b-8192-tool-use-preview"
        val llama3Groq8BToolUsePreview: String get() = "llama3-groq-8b-8192-tool-use-preview"

        // Meta Llama 3.1 Preview Models
        val llama31_70BVersatile: String get() = "llama-3.1-70b-versatile"
        val llama31_8BInstant: String get() = "llama-3.1-8b-instant"

        // Meta Llama 3.2 Preview Models
        val llama32_1BTextPreview: String get() = "llama-3.2-1b-preview"
        val llama32_3BTextPreview: String get() = "llama-3.2-3b-preview"
        val llama32_11BTextPreview: String get() = "llama-3.2-11b-text-preview"
        val llama32_90BTextPreview: String get() = "llama-3.2-90b-text-preview"

        // Llama 3.1 Large Model (Offline)
        val llama31_405B: String get() = "llama-3.1-405b"

        // Meta Llama Guard 3 Model
        val llamaGuard3_8B: String get() = "llama-guard-3-8b"

        // Meta Llama 3 General
        val metaLlama3_70B: String get() = "llama3-70b-8192"
        val metaLlama3_8B: String get() = "llama3-8b-8192"

        // Google's Gemma Models
        val gemma2_9B: String get() = "gemma2-9b-it"
        val gemma7B: String get() = "gemma-7b-it"

        // Mistral's Mixtral Model
        val mixtral8x7B: String get() = "mixtral-8x7b-32768"
    }

    object Audio {
        // HuggingFace Distil-Whisper Model
        val distilWhisperEnglish: String get() = "distil-whisper-large-v3-en"

        // OpenAI Whisper Model
        val whisperLargeV3: String get() = "whisper-large-v3"
    }

    object Vision {
        // Haotian Liu's LLAVA Model
        val llavaV1_5_7B: String get() = "llava-v1.5-7b-4096-preview"
    }
}

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
 */
private fun List<CommonMessage>.asGroqAIMessage() = this.map {
    if (it is GroqAPI.Message) it
    else GroqAPI.Message(it)
}

@Suppress("unused")
private sealed interface GroqActions : NetworkOwner, Actions, Extension<Groq> {
    override suspend fun chat(tools: List<ToolConfig>): GroqAPI.Message = with(self()) {


        val response: GroqAPI.ChatResponse =
            try {
                post(
                    "$url/chat/completions", GroqAPI.ChatRequest(
                        frequencyPenalty, logitBias, logProbabilities, model, messenger.messages().asGroqAIMessage(),
                        maxTokens, numberOfChoices, presencePenalty, responseFormat, seed, stream, stop, temperature,
                        topP, tools.asOpenAITools(), toolChoice, user
                    ), headers
                )
            } catch (e: UnexpectedMessage) {
                throw GroqException().apply { error = e.readMessage<GroqAPI.ErrorResponse>() }
            }

        response.choices[0].message
    }
}

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░       ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████████████████████████████████  ████  ██  ████  ██  ███  ███  ███████████████████████████████████████████████
██████████████████████████████████████████      ████      ███  ████  ██        █████████████████████████████████████████
 */
class Groq : GroqBuilder(), GroqActions {
    override val actions: Actions = this
    override var messenger: Messenger = Messenger(this)
    internal val headers get() = mutableMapOf(HttpHeaders.Authorization to "Bearer $apiKey")

    override fun onToolResult(toolCall: ToolCall, result: String) {
        messenger.send(GroqAPI.Message(Role.TOOL, result, toolCallID = toolCall.id))
    }

    override fun onReceiveMessage(message: CommonMessage) {
        messenger.send(message)
    }

    override val networkClient: NetworkClient by lazy { KTORClient(network) }
    override var model: String = models.chat.llama31_70BVersatile
    override val self: () -> Groq get() = { this }
}

class GroqException : Exception() {
    override val message: String get() = "Groq Error: ${error.toJson()}"
    internal lateinit var error: GroqAPI.ErrorResponse
}
