/*
░░░░░░░░░░░░░░░░░░░░░        ░░        ░░░      ░░░        ░░  ░░░░  ░░       ░░░        ░░░      ░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓      ▓▓▓▓      ▓▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓      ▓▓▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████████████  ████████  ████████        █████  █████  ████  ██  ███  ███  ██████████████  █████████████████████
█████████████████████  ████████        ██  ████  █████  ██████      ███  ████  ██        ███      ██████████████████████
 */
package dev.supachain.robot.provider

/**
 * Enum representing the features supported by a Robot.
 *
 * This enum defines a comprehensive set of functionalities that robots can interact with, encompassing
 * various AI-related tasks and capabilities.
 *
 * The features are categorized for better organization and understanding:
 *
 * - **Base:**
 *   - `Chat`: Engaging in conversational interactions with the AI.
 *   - `Embedding`: Generating numerical representations of text data for analysis and comparison.
 *   - `Moderation`: Filtering and classifying content for safety and appropriateness.
 *
 * - **Audio:**
 *   - `CreateSpeech`: Synthesizing speech from text input.
 *   - `CreateTranscription`: Transcribing spoken language into written text.
 *   - `CreateTranslation`: Translating text or speech from one language to another.
 *
 * - **FineTune:**
 *   - `CreateFineTune`: Initiating the fine-tuning of a model on custom data.
 *   - `ListFineTunes`: Retrieving information about active or completed fine-tuning jobs.
 *   - `ListFineTuneEvents`: Accessing the events associated with a fine-tuning process.
 *   - `ListFineTuneCheckPoints`: Listing available checkpoints from a fine-tuned model.
 *   - `FineTuneInfo`: Getting details about a specific fine-tuning job.
 *   - `FineTuneCancel`: Cancelling an ongoing fine-tuning job.
 *
 * - **Batch:**
 *   - `CreateBatch`: Creating batches of jobs for efficient processing.
 *   - `GetBatch`: Retrieving information about a specific batch of jobs.
 *   - `CancelBatch`: Cancelling a running or pending batch of jobs.
 *   - `ListBatches`: Listing batches of jobs.
 *
 * - **Files:**
 *   - `UploadFile`: Uploading files to be used with the model.
 *   - `ListFiles`: Retrieving a list of uploaded files.
 *   - `GetFile`: Downloading a specific file.
 *   - `DeleteFile`: Deleting an uploaded file.
 *   - `GetFileContent`: Getting the content of a file.
 *
 * - **Images/Audio/Video:**
 *   - `CreateImage`, `CreateAudio`, `CreateVideo`: Generating images, audio, or videos.
 *   - `ReadImage`, `ReadAudio`, `ReadVideo`: Analyzing or processing images, audio, or videos.
 *   - `UpdateImage`, `UpdateAudio`, `UpdateVideo`: Modifying existing images, audio, or videos.
 *   - `VaryImage`, `VaryAudio`, `VaryVideo`: Creating variations of existing images, audio, or videos.
 *
 * - **Models:**
 *   - `ListModels`: List available models.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
@Suppress("unused")
enum class Feature {
    // Base
    Chat, Embedding, Moderation,

    // Audio
    CreateSpeech, CreateTranscription, CreateTranslation,

    // FineTune
    CreateFineTune, ListFineTunes, ListFineTuneEvents, ListFineTuneCheckPoints,
    FineTuneInfo, FineTuneCancel,

    // Batch
    CreateBatch, GetBatch, CancelBatch, ListBatches,

    // Files
    UploadFile, ListFiles, GetFile, DeleteFile, GetFileContent,

    // Images
    CreateImage, ReadImage, UpdateImage, VaryImage,
    CreateAudio, ReadAudio, UpdateAudio, VaryAudio,
    CreateVideo, ReadVideo, UpdateVideo, VaryVideo,

    // Models
    ListModels
}