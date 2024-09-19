/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
 */
package dev.supachain.robot.provider

import dev.supachain.robot.tool.ToolConfig

/**
 * Interface defining the actions that a robot can perform.
 *
 * This interface outlines the various actions that a robot can take, such as chatting,
 * generating embeddings, moderating content, and more. Each action is represented by a
 * suspend function that returns a type of `CommonResponse`.
 *
 * @since 0.1.0-alpha

 */
internal interface Actions<Response> {
    val name: String

    suspend
    fun chat(tools: List<ToolConfig>): Response =
        throw NotImplementedError(
            "$name does not currently support chat, try updating the project if $name supports this feature"
        )

    suspend
    fun embedding(): Response =
        throw NotImplementedError(
            "$name does not currently support embedding, try updating the project if $name supports this feature"
        )

    suspend
    fun moderation(): Response =
        throw NotImplementedError(
            "$name does not currently support moderation, try updating the project if $name supports this feature"
        )

    suspend
    fun createSpeech(): Response =
        throw NotImplementedError(
            "$name does not currently support createSpeech, try updating the project if $name supports this feature"
        )

    suspend
    fun createTranscription()
            : Response =
        throw NotImplementedError(
            "$name does not currently support createTranscription, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun createTranslation()
            : Response =
        throw NotImplementedError(
            "$name does not currently support createTranslation, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun createFineTune(): Response =
        throw NotImplementedError(
            "$name does not currently support createFineTune, try updating the project if $name supports this feature"
        )

    suspend
    fun listFineTunes(): Response =
        throw NotImplementedError(
            "$name does not currently support listFineTunes, try updating the project if $name supports this feature"
        )

    suspend
    fun listFineTuneEvents()
            : Response =
        throw NotImplementedError(
            "$name does not currently support listFineTuneEvents, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun listFineTuneCheckPoints()
            : Response =
        throw NotImplementedError(
            "$name does not currently support listFineTuneCheckPoints, try updating the project if $name supports this" +
                    " feature"
        )

    suspend
    fun fineTuneInfo(): Response =
        throw NotImplementedError(
            "$name does not currently support fineTuneInfo, try updating the project if $name supports this feature"
        )

    suspend
    fun fineTuneCancel(): Response =
        throw NotImplementedError(
            "$name does not currently support fineTuneCancel, try updating the project if $name supports this feature"
        )

    suspend
    fun createBatch(): Response =
        throw NotImplementedError(
            "$name does not currently support createBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun getBatch(): Response =
        throw NotImplementedError(
            "$name does not currently support getBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun cancelBatch(): Response =
        throw NotImplementedError(
            "$name does not currently support cancelBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun listBatches(): Response =
        throw NotImplementedError(
            "$name does not currently support listBatches, try updating the project if $name supports this feature"
        )

    suspend
    fun uploadFile(): Response =
        throw NotImplementedError(
            "$name does not currently support uploadFile, try updating the project if $name supports this feature"
        )

    suspend
    fun listFiles(): Response =
        throw NotImplementedError(
            "$name does not currently support listFiles, try updating the project if $name supports this feature"
        )

    suspend
    fun getFile(): Response =
        throw NotImplementedError(
            "$name does not currently support getFile, try updating the project if $name supports this feature"
        )

    suspend
    fun deleteFile(): Response =
        throw NotImplementedError(
            "$name does not currently support deleteFile, try updating the project if $name supports this feature"
        )

    suspend
    fun getFileContent(): Response =
        throw NotImplementedError(
            "$name does not currently support getFileContent, try updating the project if $name supports this feature"
        )

    suspend
    fun createImage(): Response =
        throw NotImplementedError(
            "$name does not currently support createImage, try updating the project if $name supports this feature"
        )

    suspend
    fun readImage(): Response =
        throw NotImplementedError(
            "$name does not currently support readImage, try updating the project if $name supports this feature"
        )

    suspend
    fun updateImage(): Response =
        throw NotImplementedError(
            "$name does not currently support updateImage, try updating the project if $name supports this feature"
        )

    suspend
    fun varyImage(): Response =
        throw NotImplementedError(
            "$name does not currently support varyImage, try updating the project if $name supports this feature"
        )

    suspend
    fun createAudio(): Response =
        throw NotImplementedError(
            "$name does not currently support createAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun readAudio(): Response =
        throw NotImplementedError(
            "$name does not currently support readAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun updateAudio(): Response =
        throw NotImplementedError(
            "$name does not currently support updateAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun varyAudio(): Response =
        throw NotImplementedError(
            "$name does not currently support varyAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun createVideo(): Response =
        throw NotImplementedError(
            "$name does not currently support createVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun readVideo(): Response =
        throw NotImplementedError(
            "$name does not currently support readVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun updateVideo(): Response =
        throw NotImplementedError(
            "$name does not currently support updateVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun varyVideo(): Response =
        throw NotImplementedError(
            "$name does not currently support varyVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun listModels(): Response =
        throw NotImplementedError(
            "$name does not currently support listModels, try updating the project if $name supports this feature"
        )

}