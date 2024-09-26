/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
 */
package dev.supachain.robot.provider

import dev.supachain.robot.provider.models.Message
import dev.supachain.robot.tool.ToolConfig

/**
 * Interface defining the actions that a robot can perform.
 *
 * This interface outlines the various actions that a robot can take, such as chatting,
 * generating embeddings, moderating content, and more. Each action is represented by a
 * suspend function that returns a type of `CommonResponse`.
 *
 * @since 0.1.0

 */
internal interface Actions {
    val name: String

    suspend
    fun chat(tools: List<ToolConfig>): Message =
        throw NotImplementedError(
            "$name does not currently support chat, try updating the project if $name supports this feature"
        )

    suspend
    fun embedding(): Message =
        throw NotImplementedError(
            "$name does not currently support embedding, try updating the project if $name supports this feature"
        )

    suspend
    fun moderation(): Message =
        throw NotImplementedError(
            "$name does not currently support moderation, try updating the project if $name supports this feature"
        )

    suspend
    fun createSpeech(): Message =
        throw NotImplementedError(
            "$name does not currently support createSpeech, try updating the project if $name supports this feature"
        )

    suspend
    fun createTranscription()
            : Message =
        throw NotImplementedError(
            "$name does not currently support createTranscription, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun createTranslation()
            : Message =
        throw NotImplementedError(
            "$name does not currently support createTranslation, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun createFineTune(): Message =
        throw NotImplementedError(
            "$name does not currently support createFineTune, try updating the project if $name supports this feature"
        )

    suspend
    fun listFineTunes(): Message =
        throw NotImplementedError(
            "$name does not currently support listFineTunes, try updating the project if $name supports this feature"
        )

    suspend
    fun listFineTuneEvents()
            : Message =
        throw NotImplementedError(
            "$name does not currently support listFineTuneEvents, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun listFineTuneCheckPoints()
            : Message =
        throw NotImplementedError(
            "$name does not currently support listFineTuneCheckPoints, try updating the project if $name supports this" +
                    " feature"
        )

    suspend
    fun fineTuneInfo(): Message =
        throw NotImplementedError(
            "$name does not currently support fineTuneInfo, try updating the project if $name supports this feature"
        )

    suspend
    fun fineTuneCancel(): Message =
        throw NotImplementedError(
            "$name does not currently support fineTuneCancel, try updating the project if $name supports this feature"
        )

    suspend
    fun createBatch(): Message =
        throw NotImplementedError(
            "$name does not currently support createBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun getBatch(): Message =
        throw NotImplementedError(
            "$name does not currently support getBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun cancelBatch(): Message =
        throw NotImplementedError(
            "$name does not currently support cancelBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun listBatches(): Message =
        throw NotImplementedError(
            "$name does not currently support listBatches, try updating the project if $name supports this feature"
        )

    suspend
    fun uploadFile(): Message =
        throw NotImplementedError(
            "$name does not currently support uploadFile, try updating the project if $name supports this feature"
        )

    suspend
    fun listFiles(): Message =
        throw NotImplementedError(
            "$name does not currently support listFiles, try updating the project if $name supports this feature"
        )

    suspend
    fun getFile(): Message =
        throw NotImplementedError(
            "$name does not currently support getFile, try updating the project if $name supports this feature"
        )

    suspend
    fun deleteFile(): Message =
        throw NotImplementedError(
            "$name does not currently support deleteFile, try updating the project if $name supports this feature"
        )

    suspend
    fun getFileContent(): Message =
        throw NotImplementedError(
            "$name does not currently support getFileContent, try updating the project if $name supports this feature"
        )

    suspend
    fun createImage(): Message =
        throw NotImplementedError(
            "$name does not currently support createImage, try updating the project if $name supports this feature"
        )

    suspend
    fun readImage(): Message =
        throw NotImplementedError(
            "$name does not currently support readImage, try updating the project if $name supports this feature"
        )

    suspend
    fun updateImage(): Message =
        throw NotImplementedError(
            "$name does not currently support updateImage, try updating the project if $name supports this feature"
        )

    suspend
    fun varyImage(): Message =
        throw NotImplementedError(
            "$name does not currently support varyImage, try updating the project if $name supports this feature"
        )

    suspend
    fun createAudio(): Message =
        throw NotImplementedError(
            "$name does not currently support createAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun readAudio(): Message =
        throw NotImplementedError(
            "$name does not currently support readAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun updateAudio(): Message =
        throw NotImplementedError(
            "$name does not currently support updateAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun varyAudio(): Message =
        throw NotImplementedError(
            "$name does not currently support varyAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun createVideo(): Message =
        throw NotImplementedError(
            "$name does not currently support createVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun readVideo(): Message =
        throw NotImplementedError(
            "$name does not currently support readVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun updateVideo(): Message =
        throw NotImplementedError(
            "$name does not currently support updateVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun varyVideo(): Message =
        throw NotImplementedError(
            "$name does not currently support varyVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun listModels(): Message =
        throw NotImplementedError(
            "$name does not currently support listModels, try updating the project if $name supports this feature"
        )

}