package dev.supachain.robot.provider

import dev.supachain.robot.director.DirectorCore
import dev.supachain.robot.messenger.messaging.CommonResponse
import dev.supachain.robot.provider.Feature.*
import kotlin.reflect.KSuspendFunction1

/**
 * Type alias for a function that handles feature requests to provider.
 *
 * This type alias represents a function that takes a `Director` instance as input
 * and returns a `CommonResponse` object containing the AI's response or the results
 * of tool executions.
 *
 * @since 0.1.0-alpha

 */
internal typealias ProviderFeatureRequest =
        KSuspendFunction1<DirectorCore, CommonResponse>
/**
 * Creates a map associating supported features with their corresponding functions.
 *
 * This function builds a mutable map where each key is a `Feature` enum value representing
 * a specific capability (e.g., `Chat`, `Embedding`, `Moderation`, etc.), and the corresponding value
 * is a function reference (`KFunction`) that implements the logic for that feature.
 *
 * The resulting map serves as a lookup table to dynamically execute the appropriate function based on
 * the requested feature.
 *
 * @receiver The `Robot` instance whose functions will be mapped to the features.
 * @return A mutable map of `Feature` to its corresponding function (`KFunction`).
 *
 * @since 0.1.0-alpha

 */
internal fun Provider<*>.getFeatureMap(): MutableMap<Feature, ProviderFeatureRequest> = mutableMapOf(
    Chat to ::chat,
    Embedding to ::embedding,
    Moderation to ::moderation,
    CreateSpeech to ::createSpeech,
    CreateTranscription to ::createTranscription,
    CreateTranslation to ::createTranslation,
    CreateFineTune to ::createFineTune,
    ListFineTunes to ::listFineTunes,
    ListFineTuneEvents to ::listFineTuneEvents,
    ListFineTuneCheckPoints to ::listFineTuneCheckPoints,
    FineTuneInfo to ::fineTuneInfo,
    FineTuneCancel to ::fineTuneCancel,
    CreateBatch to ::createBatch,
    GetBatch to ::getBatch,
    CancelBatch to ::cancelBatch,
    ListBatches to ::listBatches,
    UploadFile to ::uploadFile,
    ListFiles to ::listFiles,
    GetFile to ::getFile,
    DeleteFile to ::deleteFile,
    GetFileContent to ::getFileContent,
    CreateImage to ::createImage,
    ReadImage to ::readImage,
    UpdateImage to ::updateImage,
    VaryImage to ::varyImage,
    CreateAudio to ::createAudio,
    ReadAudio to ::readAudio,
    UpdateAudio to ::updateAudio,
    VaryAudio to ::varyAudio,
    CreateVideo to ::createVideo,
    ReadVideo to ::readVideo,
    UpdateVideo to ::updateVideo,
    VaryVideo to ::varyVideo,
    ListModels to ::listModels
).also { val x = 8; it }