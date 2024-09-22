@file:Suppress("unused")

package dev.supachain.robot.provider

import dev.supachain.robot.messenger.MessageFilter
import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.provider.models.Message
import dev.supachain.robot.provider.models.TextMessage
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.strategies.ToolUseStrategy

/**
 * Abstract base class for model providers.
 *
 * This class provides a foundation for implementing different providers,
 * each with its own capabilities and configurations. It manages features,
 * handles requests, and encapsulates error handling.
 *
 * @param T The specific type of the `Provider` subclass, enabling fluent configuration using the `invoke` operator.
 *
 * @since 0.1.0-alpha

 */
abstract class Provider<T : Provider<T>> : Actions {
    abstract var maxRetries: Int
    abstract var toolsAllowed: Boolean
    abstract var toolStrategy: ToolUseStrategy
    var loopDetection: Boolean = true
    var userMessagePrimer: Boolean = true
    var useFormatMessage: Boolean = true
    var messageFilter : MessageFilter = MessageFilter.None
    var includeSeekCompletionMessage: Boolean = true

    internal abstract var messenger: Messenger
    internal abstract val toolResultMessage: (result: String) -> TextMessage

    /**
     * Executes a request to the AI provider for a specific feature.
     *
     * This function dynamically dispatches the request to the appropriate handler
     * based on the `featureMap`.
     *
     * @param feature The AI feature to be executed (e.g., `Feature.CHAT`, `Feature.EMBEDDING`).
     * @param director The `Director` instance responsible for orchestrating the AI interaction.
     * @return A `CommonResponse` object containing the result of the feature execution.
     * @throws IllegalStateException If the requested feature is not supported by this AI provider.
     *
     * @since 0.1.0-alpha

     */
    internal suspend inline
    fun request(feature: Feature, tools: List<ToolConfig>): Message =
        when (feature) {
            // Feature chat is chatting
            Feature.Chat -> chat(tools.allowed)
            Feature.Embedding -> embedding()
            Feature.Moderation -> moderation()
            Feature.CreateSpeech -> createSpeech()
            Feature.CreateTranscription -> createTranscription()
            Feature.CreateTranslation -> createTranslation()
            Feature.CreateFineTune -> createFineTune()
            Feature.ListFineTunes -> listFineTunes()
            Feature.ListFineTuneEvents -> listFineTuneEvents()
            Feature.ListFineTuneCheckPoints -> listFineTuneCheckPoints()
            Feature.FineTuneInfo -> fineTuneInfo()
            Feature.FineTuneCancel -> fineTuneCancel()
            Feature.CreateBatch -> createBatch()
            Feature.GetBatch -> getBatch()
            Feature.CancelBatch -> cancelBatch()
            Feature.ListBatches -> listBatches()
            Feature.UploadFile -> uploadFile()
            Feature.ListFiles -> listFiles()
            Feature.GetFile -> getFile()
            Feature.DeleteFile -> deleteFile()
            Feature.GetFileContent -> getFileContent()
            Feature.CreateImage -> createImage()
            Feature.ReadImage -> readImage()
            Feature.UpdateImage -> updateImage()
            Feature.VaryImage -> varyImage()
            Feature.CreateAudio -> createAudio()
            Feature.ReadAudio -> readAudio()
            Feature.UpdateAudio -> updateAudio()
            Feature.VaryAudio -> varyAudio()
            Feature.CreateVideo -> createVideo()
            Feature.ReadVideo -> readVideo()
            Feature.UpdateVideo -> updateVideo()
            Feature.VaryVideo -> varyVideo()
            Feature.ListModels -> listModels()
        }

    @Suppress("UNCHECKED_CAST")
    inline operator
    fun invoke(modify: T.() -> Unit) = (this as T).modify()

    internal val List<ToolConfig>.allowed: List<ToolConfig>
        get() = if (toolsAllowed) toolStrategy.getTools(this) else emptyList()

}