package dev.supachain.robot.provider.models

import dev.supachain.Modifiable
import dev.supachain.Extension

import dev.supachain.robot.provider.Provider
import dev.supachain.robot.provider.Actions

import dev.supachain.robot.*
import dev.supachain.robot.director.DirectorCore
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.messenger.messaging.CommonAudioRequest
import dev.supachain.robot.messenger.messaging.CommonChatRequest
import dev.supachain.robot.messenger.messaging.CommonEmbedRequest
import dev.supachain.robot.messenger.messaging.CommonImageRequest
import dev.supachain.robot.messenger.messaging.CommonModRequest
import dev.supachain.robot.post
import dev.supachain.robot.provider.responses.CommonResponse
import dev.supachain.robot.provider.responses.OpenAIChatResponse
import dev.supachain.robot.tool.ToolChoice
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.BackAndForth
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░       ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████████████████████████████████  ████  ██  ████  ██  ███  ███  ███████████████████████████████████████████████
██████████████████████████████████████████      ████      ███  ████  ██        █████████████████████████████████████████
 */

/**
 * Configuration for interacting with OpenAI's API as an AI Robot.
 *
 * This class provides settings to control how the robot communicates with OpenAI's language models and services.
 * It includes parameters for customizing the model's behavior, network communication, and tool usage.
 *
 * @property name The name of this configuration ("Open AI API").
 * @property models A reference to the available OpenAI models.
 * @property chatModel The default chat model to use (e.g., "gpt-4o").
 * @property temperature  Sampling temperature (0.0 to 2.0) to control randomness.
 *   Higher values make output more creative, lower values make it more deterministic.
 * @property topP Nucleus sampling threshold (0.0 to 1.0).
 *   Controls the diversity of output by selecting from the most likely tokens.
 * @property maxTokens Maximum number of tokens to generate in a response.
 * @property maxRetries Maximum retry attempts for failed requests.
 * @property maxToolExecutions Maximum number of tool calls allowed.
 * @property logProbabilities Whether to include log probabilities of tokens in responses.
 * @property parallelToolCalling Whether to execute multiple tool calls concurrently.
 * @property n Number of responses to generate for each request.
 * @property presencePenalty Controls the model's tendency to repeat phrases.
 * @property frequencyPenalty Controls the model's tendency to repeat the same tokens.
 * @property stop List of strings that signal the model to stop generating further tokens.
 * @property logitBias Modifies the likelihood of specific tokens appearing in the response.
 * @property defaultUser A default identifier for the end-user.
 * @property url Base URL for the OpenAI API.
 * @property network Configuration for network communication settings.
 * @property networkClient The Ktor client for making API requests.
 * @property toolsAllowed Indicates whether the use of tools is permitted.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
@Suppress("unused")
class OpenAI : Provider<OpenAI>(), OpenAIActions {
    override val name: String get() = "Open AI API"
    val models = OpenAIModels

    var chatModel: String = ChatModels.gpt4o
    var temperature: Double = 0.5
    var topP: Double = 0.0
    var maxTokens: Int = 0
    var maxToolExecutions: Int = 4
    var logProbabilities: Boolean = true
    var parallelToolCalling: Boolean = true
    var n: Int = 1
    var presencePenalty = 1.0
    var frequencyPenalty = 1.0
    var stop: List<String> = emptyList()
    var logitBias: Map<String, Int> = emptyMap()
    var defaultUser: String = ""
    val network: NetworkConfig = NetworkConfig()

    override var maxRetries: Int = 3
    override var url: String = "http://localhost:$8080"
    override val networkClient: NetworkClient by lazy { KTORClient(network) }
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = BackAndForth

    // #Modifiable, #Extended
    override val self = { this }

    companion object : Modifiable<OpenAI>({ OpenAI() })
}

@Suppress("unused")
/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░       ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████████████████████████        ██  ███████████  █████████████████████████████████████████████████
██████████████████████████████████████████████  ████  ██  ████████        ██████████████████████████████████████████████
*/
private interface OpenAIAPI {
    /**
     * Data class representing a chat completion request with additional parameters.
     *
     * This class builds upon the `CommonChatRequest` by providing a richer set of options
     * to control the behavior of the AI model's response generation.
     *
     * @property messages The list of messages in the conversation history.
     * @property model The name or identifier of the model to use.
     * @property frequencyPenalty Number between -2.0 and 2.0. Positive values penalize new tokens based on their
     * existing frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
     * @property logitBias Modifies the likelihood of specified tokens appearing in the completion.
     *   Accepts a map of token IDs to bias values (-100 to 100).
     * @property logProbabilities If `true`, includes the log probabilities of the most likely tokens.
     * @property maxTokens The maximum number of tokens to generate in the completion.
     * @property n How many completions to generate for each prompt.
     * @property parallelToolCalls Whether to execute multiple tool calls in parallel.
     * @property presencePenalty Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they
     *   appear in the text so far, increasing the model's likelihood to talk about new topics.
     * @property seed An optional integer to use as a seed for the random number generator.
     * @property stop Up to 4 sequences where the API will stop generating further tokens.
     * @property stream If `true`, partial progress will be streamed back as it's generated.
     * @property temperature What sampling temperature to use, between 0 and 2.
     *   Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused
     *   and deterministic.
     * @property topP An alternative to sampling with temperature, called nucleus sampling, where the model considers
     * the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10%
     * probability mass are considered.
     * @property tools List of tools the model can use.
     * @property toolChoice Controls which tool the model can use.
     *   Can be "none", "auto", "required", or a specific function name.
     * @property user A unique identifier representing your end-user
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    @Serializable
    data class ChatRequest(
        val messages: List<Message>,
        val model: String,
        @SerialName("frequency_penalty") val frequencyPenalty: Double = 0.0,
        @SerialName("logit_bias") val logitBias: Map<String, Int>? = null,
        @SerialName("logprobs") val logProbabilities: Boolean = false,
        @SerialName("max_tokens") val maxTokens: Int? = null,
        val n: Int = 1,
        @SerialName("parallel_tool_calls") val parallelToolCalls: Boolean = true,
        @SerialName("presence_penalty") val presencePenalty: Double = 0.0,
        val seed: Int? = null,
        val stop: List<String>? = null,
        val stream: Boolean = false,
        val temperature: Double = 1.0,
        @SerialName("top_p") val topP: Double = 1.0,
        val tools: List<ToolConfig> = emptyList(),
        @SerialName("tool_choice") val toolChoice: ToolChoice = ToolChoice.AUTO,
        val user: String? = null
    ) : CommonChatRequest

    /**
     * Represents a request to generate text embeddings.
     *
     * This data class encapsulates the parameters required for an embedding request. Embeddings are dense vector
     * representations of text that capture semantic meaning.
     *
     * @param model The ID of the embedding model to use. This determines how the text will be converted into an
     * embedding.
     *
     * @param input A list of strings representing the text input for which to generate embeddings. Each string in the
     * list is processed as a separate input.
     *
     * @param dimensions (Optional) The number of dimensions the resulting output embeddings should have.
     *
     * @param returnFormat (Optional) The format for the returned embeddings.
     *
     * @param user (Optional) A unique identifier representing the end-user.
     *
     * @since 0.1.0-alpha
     * @version 1.0.0
     * @author Che Andre
     */
    @Serializable
    data class EmbedRequest(
        val model: String,
        val input: List<String>,
        val dimensions: Int? = null,
        @SerialName("encoding_format") val returnFormat: String? = null,
        val user: String? = null
    ) : CommonEmbedRequest


    /**
     * Data class representing a moderation request for
     *
     * @param input The input text to classify
     * @param model The model to use for moderation. Defaults to "text-moderation-latest"
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    @Serializable
    data class ModerationRequest(
        val input: String,
        val model: String
    ) : CommonModRequest

    /**
     * Data class representing an audio transcription request.
     *
     * @param model The model to use for transcription. Defaults to "whisper-1"
     * @param file The audio file to transcribe, in one of these formats: mp3, mp4, mpeg, mpga, m4a, wav, or webm.
     * @param responseFormat The format of the transcript output, in one of these formats: json, text, srt, verbose_json, or vtt.
     * @param temperature The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.
     * @param language The language of the input audio. Supplying the input language in ISO-639-1 format will improve accuracy and latency.
     * @param prompt An optional text to guide the model's style or continue a previous audio segment. The prompt should match the audio language.
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    @Serializable
    data class WriteAudioRequest(
        val model: String = "whisper-1",
        val input: String,
        val file: String,
        val temperature: Double? = null,
        val language: String? = null,
        @SerialName("response_format") val responseFormat: String? = null,
        val prompt: String? = null
    ) : CommonAudioRequest

    /**
     * Data class representing an image generation request.
     *
     * @param prompt A text description of the desired image(s). The maximum length is 1000 characters.
     * @param n The number of images to generate. Must be between 1 and 10.
     * @param size The size of the generated images. Must be one of "256x256", "512x512", or "1024x1024".
     * @param responseFormat The format in which the generated images are returned. Must be one of "url" or "b64_json".
     * @param user A unique identifier representing your end-user, which can help to monitor and detect abuse
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    @Serializable
    data class WriteImageRequest(
        val prompt: String,
        val n: Int = 1,
        val size: String = "1024x1024",
        @SerialName("response_format") val responseFormat: String? = null,
        val user: String? = null
    ) : CommonImageRequest
}

@Suppress("unused")
/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  ░░░░  ░░░      ░░░       ░░░        ░░  ░░░░░░░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒   ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓        ▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓  ▓▓      ▓▓▓▓  ▓▓▓▓▓▓▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
███████████████████████████████  █  █  ██  ████  ██  ████  ██  ████████  ██████████████  ███████████████████████████████
███████████████████████████████  ████  ███      ███       ███        ██        ███      ████████████████████████████████
 */
/**
 * Provides convenient access to OpenAI model names for various tasks.
 *
 * This object serves as a central repository for OpenAI model identifiers,
 * categorized by their primary functions (chat, embedding, moderation).
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
object OpenAIModels {
    /** Models suitable for chat completions. */
    val chat = ChatModels

    /** Models designed for generating text embeddings. */
    val embedding = EmbedModels

    /** Models specializing in content moderation. */
    val moderation = ModerationModels
}

/**
 * Object containing names for OpenAI's chat models.
 */
@Suppress("unused")
object ChatModels {
    val gpt4o get() = "gpt-4o"
    val gpt4Turbo get() = "gpt-4-turbo"
    val gpt4TurboPreview get() = "gpt-4-turbo-preview"
    val gpt4V0125Preview get() = "gpt-4-0125-preview"
    val gpt4V1106Preview get() = "gpt-4-1106-preview"
    val gpt4 get() = "gpt-4"
    val gpt4V0613 get() = "gpt-4-0613"
    val gpt35Turbo get() = "gpt-3.5-turbo"
    val gpt35TurboV0125 get() = "gpt-3.5-turbo-0125"
    val gpt35TurboV1106 get() = "gpt-3.5-turbo-1106"
    val gpt35TurboInstruct get() = "gpt-3.5-turbo-instruct"
}

/**
 * Object containing names for OpenAI's text embedding models.
 */
@Suppress("unused")
object EmbedModels {
    val textEmbedding3Large get() = "text-embed-3-large"
    val textEmbedding3Small get() = "text-embed-3-small"
    val textEmbeddingAda002 get() = "text-embed-3-ada-002"
}

/**
 * Object containing names for OpenAI's moderation models.
 */
@Suppress("unused")
object ModerationModels {
    val textModerationLatest get() = "text-moderation-latest"
    val textModerationStable get() = "text-moderation-stable"
    val textModeration007 get() = "text-moderation-007"
}

@Suppress("unused")
/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
 */
/**
 * Defines actions for interacting with the OpenAI API.
 *
 * This sealed interface specifies the set of operations that a client can perform
 * against the OpenAI API, including chat completion, embedding generation, and moderation.
 *
 * It extends the `NetworkOwner` and `Actions` interfaces, providing access to network capabilities
 * and common action definitions. It also serves as an `Extension` for the `OpenAI` configuration class.
 *
 * Implementations of this interface are expected to provide concrete logic for executing these actions,
 * typically by making HTTP requests to the OpenAI API endpoints.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
private sealed interface OpenAIActions : NetworkOwner, Actions, Extension<OpenAI> {
    // private extension OpenAi.Actions : NetworkOwner, Transactions
    override suspend
    fun chat(director: DirectorCore): OpenAIChatResponse =
        with(self()) {
            return post(
                "$url/v1/chat/completions", OpenAIAPI.ChatRequest(
                    director.messages, chatModel, frequencyPenalty, logitBias, logProbabilities, maxTokens, n,
                    parallelToolCalling, presencePenalty, director.defaultSeed, stop, network.streamable,
                    temperature, topP, director.tools
                )
            )
        }

    override suspend
    fun embedding(director: DirectorCore): CommonResponse =
        with(self()) {
            return post(
                "$url/v1/chat/completions", OpenAIAPI.ChatRequest(
                    director.messages, chatModel, frequencyPenalty, logitBias, logProbabilities, maxTokens, n,
                    parallelToolCalling, presencePenalty, director.defaultSeed, stop, network.streamable,
                    temperature, topP, director.tools
                )
            )
        }

    override suspend
    fun moderation(director: DirectorCore): CommonResponse =
        with(self()) {
            return post(
                "$url/v1/chat/completions", OpenAIAPI.ChatRequest(
                    director.messages, chatModel, frequencyPenalty, logitBias, logProbabilities, maxTokens, n,
                    parallelToolCalling, presencePenalty, director.defaultSeed, stop, network.streamable,
                    temperature, topP, director.tools
                )
            )
        }
}
