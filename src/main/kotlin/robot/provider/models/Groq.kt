package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.NetworkOwner
import dev.supachain.robot.messenger.Role
import dev.supachain.robot.messenger.messaging.Usage
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.Provider

import dev.supachain.robot.tool.ToolChoice
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import dev.supachain.utilities.Parameter
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
    abstract val model: String
    var apiKey: String = ""
    var frequencyPenalty = 0.0
    var maxTokens: Int = 2048
    val presencePenalty: Double = 0.0
    val stream: Boolean = false
    var stop: List<String>? = null
    var temperature: Double = 0.0
    var topP: Double = 0.0
    val toolChoice = ToolChoice.AUTO

    // Network
    val network: NetworkConfig = NetworkConfig()
    override var url: String = "https://api.groq.com/openai/v1"

    // Provider
    override var name: String = "Groq"
    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = FillInTheBlank
}

@Suppress("unused")
internal interface GroqAPI : Extension<Groq> {
    @Serializable
    data class GroqMessage(
        val role: Role,
        val content: String? = null
    ) {
        constructor(message: Message) : this(message.role(), message.text().value)
    }

    @Serializable
    data class Tool(val type: String, val function: Function) {
        @Serializable
        data class Function(val name: String, val description: String?, val parameters: Parameters) {
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

        constructor(toolConfig: ToolConfig) : this("function", Function(toolConfig))
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
        val model: String,
        val messages: List<GroqMessage>,
        @SerialName("max_tokens")
        val maxTokens: Int,
        @SerialName("presence_penalty")
        val presencePenalty: Double,
        val stream: Boolean? = null,
        val stop: List<String>?,
        val temperature: Double,
        @SerialName("top_p")
        var topP: Double = 1.0,
        @SerialName("tool_choice")
        val tools: List<Tool> = emptyList(),
        val toolChoice: ToolChoice
    )

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
    ) : Message {
        @Serializable
        data class Choice(
            val index: Int,
            val message: GroqMessage
        )

        @Serializable
        data class Error(
            val code: Int,
            val message: String,
            val type: String,
            val param: String
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
        val llama31_70BPreview: String get() = "llama-3.1-70b-versatile"
        val llama31_8BPreview: String get() = "llama-3.1-8b-instant"

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

private sealed interface GroqActions : NetworkOwner, Actions, Extension<Groq> {

}


class Groq {
}

