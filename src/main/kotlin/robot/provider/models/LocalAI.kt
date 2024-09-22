@file:Suppress("unused")

package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.Modifiable
import dev.supachain.robot.*
import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.messenger.Role
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.CommonChatRequest
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.BackAndForth
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import dev.supachain.utilities.Parameter
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
 * Configuration for interacting with a LocalAI model.
 *
 * This class provides settings to control how a robot interacts with a locally hosted AI model.
 * It includes parameters for customizing the model's behavior and network communication.
 *
 * @property name The name of this configuration ("LocalAI API").
 * @property url The base URL of the local AI model server.
 * @property temperature  Sampling temperature (0.0 to 2.0) to control randomness.
 *   Higher values make output more creative, lower values make it more deterministic.
 * @property topP Nucleus sampling threshold (0.0 to 1.0).
 *   Controls the diversity of output by selecting from the most likely tokens.
 * @property topK Top-K sampling parameter (0 to infinity).
 *   Limits token selection to the top K most likely tokens.
 * @property maxTokens Maximum number of tokens to generate in a response.
 * @property chatModel The name or identifier of the local chat model to use.
 * @property toolsAllowed Indicates whether the use of tools is permitted (defaults to `false`).
 * @property network Configuration for network communication settings.
 * @property networkClient The Ktor client for making API requests to the local model.
 *
 * @since 0.1.0-alpha
 */
class LocalAI : Provider<LocalAI>(), LocalAIActions, NetworkOwner {
    override val name: String get() = "Mr Robot"
    override var url: String = "http://localhost:$8888"
    override val toolResultMessage: (result: String) -> TextMessage =
        { TextMessage(Role.FUNCTION, it) }

    var backend = "llama-cpp"
    var batch = 0
    var temperature: Double = 0.5
    var topP: Double = 0.0
    var topK: Int = 0
    var maxTokens: Int = 0
    var chatModel: String = "meta-llama-3.1-8b-instruct"

    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = BackAndForth
    override var messenger: Messenger = Messenger(this)

    // Network
    val network: NetworkConfig = NetworkConfig()
    override val networkClient: NetworkClient by lazy { KTORClient(network) }

    // #Modifiable
    companion object : Modifiable<LocalAI>({ LocalAI() })

    override val self: () -> LocalAI get() = { this }

    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double,
        @SerialName("top_p")
        val topP: Double,
        @SerialName("top_k")
        val topK: Int,
        @SerialName("max_tokens")
        val maxTokens: Int,
        val tools: List<Tool> = emptyList(),
    ) : CommonChatRequest {
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
    }

}


/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
 */
private fun List<ToolConfig>.asLocalAITools() = map { LocalAI.ChatRequest.Tool(it) }

private sealed interface LocalAIActions : NetworkOwner, Actions, Extension<LocalAI> {
    override suspend fun chat(tools: List<ToolConfig>): OpenAIAPI.ChatResponse = with(self()) {
        return post(
            "$url/v1/chat/completions", LocalAI.ChatRequest(
                chatModel, messenger.messages(),
                temperature, topP, topK, maxTokens, tools.asLocalAITools()
            )
        )
    }
}
