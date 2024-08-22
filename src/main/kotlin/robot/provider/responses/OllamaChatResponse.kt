package dev.supachain.robot.provider.responses

import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.messenger.messaging.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaChatResponse(
    val model: String,
    @SerialName("created_at")
    val createdAt: String,
    val message: Message.FromAssistant,
    val done: Boolean,
    @SerialName("total_duration")
    val totalDuration: Long,
    @SerialName("load_duration")
    val loadDuration: Long,
    @SerialName("prompt_eval_count")
    val promptEvalCount: Long,
    @SerialName("prompt_eval_duration")
    val promptEvalDuration: Long,
    @SerialName("eval_count")
    val evalCount: Long,
    @SerialName("eval_duration")
    val evalDuration: Long
) : CommonResponse {

    override val rankMessages: List<Message.FromAssistant> = listOf(message)
    override val requestedFunctions: List<FunctionCall>
        get() = rankMessages[0].toolCalls
            ?.filter { it.type == "function" }
            ?.map { it.function }
            ?: emptyList()
}