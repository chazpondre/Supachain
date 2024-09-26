@file:Suppress("unused")

package dev.supachain.robot.provider

import dev.supachain.robot.messenger.MessageFilter
import dev.supachain.robot.messenger.Messenger
import dev.supachain.robot.provider.models.Message
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
 * @since 0.1.0
 */
abstract class Provider<T : Provider<T>> {
    var loopDetection: Boolean = true
    var userMessagePrimer: Boolean = true
    var useFormatMessage: Boolean = true
    var messageFilter: MessageFilter = MessageFilter.None
    var includeSeekCompletionMessage: Boolean = true
    abstract var name: String
    abstract var maxRetries: Int
    abstract var toolsAllowed: Boolean
    abstract var toolStrategy: ToolUseStrategy
    internal abstract val actions: Actions

    internal abstract val messenger: Messenger
    internal abstract fun onToolResult(result: String)
    internal abstract fun onReceiveMessage(message: Message)

    /**
     * Executes a request to the AI provider for a specific feature.
     *
     * This function dynamically dispatches the request to the appropriate handler
     * based on the `featureMap`. It uses a suspending function to asynchronously
     * communicate with the AI model or tool, performing various actions like
     * chatting, generating embeddings, or managing batches.
     *
     * @param feature The AI feature to be executed (e.g., `Feature.Chat`, `Feature.Embedding`).
     * @param tools A list of `ToolConfig` that may be used during the request.
     * @return A `Message` object containing the result of the feature execution.
     * @throws IllegalStateException If the requested feature is not supported by this AI provider.
     *
     * @since 0.1.0
     */
    internal suspend inline
    fun request(feature: Feature, tools: List<ToolConfig>): Message =
        when (feature) {
            // Feature chat is chatting
            Feature.Chat -> actions.chat(tools.allowed)
            Feature.Embedding -> actions.embedding()
            Feature.Moderation -> actions.moderation()
            Feature.CreateSpeech -> actions.createSpeech()
            Feature.CreateTranscription -> actions.createTranscription()
            Feature.CreateTranslation -> actions.createTranslation()
            Feature.CreateFineTune -> actions.createFineTune()
            Feature.ListFineTunes -> actions.listFineTunes()
            Feature.ListFineTuneEvents -> actions.listFineTuneEvents()
            Feature.ListFineTuneCheckPoints -> actions.listFineTuneCheckPoints()
            Feature.FineTuneInfo -> actions.fineTuneInfo()
            Feature.FineTuneCancel -> actions.fineTuneCancel()
            Feature.CreateBatch -> actions.createBatch()
            Feature.GetBatch -> actions.getBatch()
            Feature.CancelBatch -> actions.cancelBatch()
            Feature.ListBatches -> actions.listBatches()
            Feature.UploadFile -> actions.uploadFile()
            Feature.ListFiles -> actions.listFiles()
            Feature.GetFile -> actions.getFile()
            Feature.DeleteFile -> actions.deleteFile()
            Feature.GetFileContent -> actions.getFileContent()
            Feature.CreateImage -> actions.createImage()
            Feature.ReadImage -> actions.readImage()
            Feature.UpdateImage -> actions.updateImage()
            Feature.VaryImage -> actions.varyImage()
            Feature.CreateAudio -> actions.createAudio()
            Feature.ReadAudio -> actions.readAudio()
            Feature.UpdateAudio -> actions.updateAudio()
            Feature.VaryAudio -> actions.varyAudio()
            Feature.CreateVideo -> actions.createVideo()
            Feature.ReadVideo -> actions.readVideo()
            Feature.UpdateVideo -> actions.updateVideo()
            Feature.VaryVideo -> actions.varyVideo()
            Feature.ListModels -> actions.listModels()
        }

    @Suppress("UNCHECKED_CAST")
    inline operator
    fun invoke(modify: T.() -> Unit) = (this as T).modify()

    /**
     * Filters the allowed tools based on the `toolsAllowed` flag.
     *
     * If the provider allows the use of tools, this property will return the tools
     * that are available and allowed in the current strategy. Otherwise, it will return an empty list.
     *
     * @since 0.1.0
     */
    internal val List<ToolConfig>.allowed: List<ToolConfig>
        get() = if (toolsAllowed) toolStrategy.getTools(this) else emptyList()
}