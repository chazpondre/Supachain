@file:Suppress("unused")

package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.Modifiable
import dev.supachain.robot.*
import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.messenger.Role
import dev.supachain.robot.messenger.messaging.ToolCall
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.CommonChatRequest
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.BackAndForth
import dev.supachain.robot.tool.strategies.ToolUseStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░       ░░░  ░░░░  ░░        ░░  ░░░░░░░░       ░░░        ░░       ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓       ▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓      ▓▓▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████  ████  ██  ████  █████  █████  ████████  ████  ██  ████████  ███  ███████████████████████████
██████████████████████████       ████      ███        ██        ██       ███        ██  ████  ██████████████████████████
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
 * @since 0.1.0
 */
class LocalAI : Provider<LocalAI>(), LocalAIActions, NetworkOwner {
    override val actions: Actions = this
    override var name: String = "Local AI"
    override var url: String = "http://localhost:$8888"

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

    override fun onToolResult(toolCall: ToolCall, result: String) {
        messenger.send(TextMessage(Role.FUNCTION, result))
    }

    override fun onReceiveMessage(message: CommonMessage) {
        messenger.send(message)
    }


    interface API {
        /*
        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░  ░░░░  ░░░      ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
        ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
        ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓        ▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
        █████████████████████████████████████  ████  ██  ████  ██        █████  ████████████████████████████████████████
        ██████████████████████████████████████      ███  ████  ██  ████  █████  ████████████████████████████████████████
        */
        @Serializable
        data class ChatRequest(
            val model: String,
            val messages: List<CommonMessage>,
            val temperature: Double,
            @SerialName("top_p")
            val topP: Double,
            @SerialName("top_k")
            val topK: Int,
            @SerialName("max_tokens")
            val maxTokens: Int,
            val tools: List<CommonTool.OpenAI> = emptyList(),
        ) : CommonChatRequest
    }
}

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
*/
private sealed interface LocalAIActions : NetworkOwner, Actions, Extension<LocalAI> {
    override suspend fun chat(tools: List<ToolConfig>): OpenAIAPI.ChatResponse = with(self()) {
        return post(
            "$url/v1/chat/completions", LocalAI.API.ChatRequest(
                chatModel, messenger.messages(),
                temperature, topP, topK, maxTokens, tools.asOpenAITools()
            )
        )
    }
}
