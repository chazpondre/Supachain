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
import dev.supachain.utilities.mapToFunctionCall
import dev.supachain.utilities.toJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/*
░░░░░░░░░░░░░░░░░░░░░░░░░░       ░░░  ░░░░  ░░        ░░  ░░░░░░░░       ░░░        ░░       ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓       ▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓      ▓▓▓▓       ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████  ████  ██  ████  █████  █████  ████████  ████  ██  ████████  ███  ███████████████████████████
██████████████████████████       ████      ███        ██        ██       ███        ██  ████  ██████████████████████████
*/
@Suppress("MemberVisibilityCanBePrivate")
class Ollama : Provider<Ollama>(), OllamaActions, NetworkOwner {
    override val actions: Actions = this
    var chatModel: String = "llama3.1"
    val stream: Boolean = false

    override var name: String = "Ollama"
    override var url: String = "http://localhost:11434"
    override var maxRetries: Int = 3
    override var toolsAllowed: Boolean = true
    override var toolStrategy: ToolUseStrategy = FillInTheBlank
    override var messenger: Messenger = Messenger(this)

    val network: NetworkConfig = NetworkConfig()
    override val networkClient: NetworkClient by lazy { KTORClient(network) }

    override val self: () -> Ollama get() = { this }

    override fun onToolResult(result: String) {
        messenger.send(TextMessage(Role.FUNCTION, result))
    }

    override fun onReceiveMessage(message: Message) {
        messenger.send(message)
    }

    /*
    ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░       ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
    ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
    ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
    ████████████████████████████████████████████        ██  ███████████  ███████████████████████████████████████████████
    ████████████████████████████████████████████  ████  ██  ████████        ████████████████████████████████████████████
    */
    internal interface API {
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

        @Serializable
        data class OllamaMessage(
            val role: String,
            val content: String,
            @SerialName("tool_calls")
            val toolCalls: List<ToolCall>? = emptyList()
        ) {
            @Serializable
            data class ToolCall(val function: Function) {
                @Serializable
                data class Function(
                    val name: String,
                    val arguments: Map<String, JsonElement>
                )
            }
        }

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
            val messages: List<Message>,
            val tools: List<Tool> = emptyList(),
            val stream: Boolean
        ) : CommonChatRequest

        @Serializable
        data class ChatResponse(
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
        ) : Message {

            override fun contents(): List<Message.Content> = listOf(text())
            override fun text() = TextContent(message.content)
            override fun role() = Role.ASSISTANT
            override fun functions(): List<FunctionCall> =
                message.toolCalls?.map { FunctionCall(it.function.arguments.mapToFunctionCall(), it.function.name) }
                    ?: emptyList()

            override fun toString() = this.toJson()
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
private fun List<ToolConfig>.asOllamaTools() = map { Ollama.API.Tool(it) }
private sealed interface OllamaActions : NetworkOwner, Actions, Extension<Ollama> {
    override suspend fun chat(tools: List<ToolConfig>): Ollama.API.ChatResponse = with(self()) {
        return post(
            "$url/api/chat",
            Ollama.API.ChatRequest(chatModel, messenger.messages(), tools.asOllamaTools(), stream)
        )
    }
}
