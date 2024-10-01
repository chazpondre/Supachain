/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
 */
package dev.supachain.robot.provider

import dev.supachain.robot.provider.models.CommonMessage
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
    fun chat(tools: List<ToolConfig>): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support chat, try updating the project if $name supports this feature"
        )

    suspend
    fun embedding(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support embedding, try updating the project if $name supports this feature"
        )

    suspend
    fun moderation(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support moderation, try updating the project if $name supports this feature"
        )

    suspend
    fun createSpeech(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support createSpeech, try updating the project if $name supports this feature"
        )

    suspend
    fun createTranscription()
            : CommonMessage =
        throw NotImplementedError(
            "$name does not currently support createTranscription, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun createTranslation()
            : CommonMessage =
        throw NotImplementedError(
            "$name does not currently support createTranslation, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun createFineTune(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support createFineTune, try updating the project if $name supports this feature"
        )

    suspend
    fun listFineTunes(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support listFineTunes, try updating the project if $name supports this feature"
        )

    suspend
    fun listFineTuneEvents()
            : CommonMessage =
        throw NotImplementedError(
            "$name does not currently support listFineTuneEvents, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun listFineTuneCheckPoints()
            : CommonMessage =
        throw NotImplementedError(
            "$name does not currently support listFineTuneCheckPoints, try updating the project if $name supports this" +
                    " feature"
        )

    suspend
    fun fineTuneInfo(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support fineTuneInfo, try updating the project if $name supports this feature"
        )

    suspend
    fun fineTuneCancel(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support fineTuneCancel, try updating the project if $name supports this feature"
        )

    suspend
    fun createBatch(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support createBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun getBatch(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support getBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun cancelBatch(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support cancelBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun listBatches(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support listBatches, try updating the project if $name supports this feature"
        )

    suspend
    fun uploadFile(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support uploadFile, try updating the project if $name supports this feature"
        )

    suspend
    fun listFiles(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support listFiles, try updating the project if $name supports this feature"
        )

    suspend
    fun getFile(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support getFile, try updating the project if $name supports this feature"
        )

    suspend
    fun deleteFile(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support deleteFile, try updating the project if $name supports this feature"
        )

    suspend
    fun getFileContent(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support getFileContent, try updating the project if $name supports this feature"
        )

    suspend
    fun createImage(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support createImage, try updating the project if $name supports this feature"
        )

    suspend
    fun readImage(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support readImage, try updating the project if $name supports this feature"
        )

    suspend
    fun updateImage(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support updateImage, try updating the project if $name supports this feature"
        )

    suspend
    fun varyImage(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support varyImage, try updating the project if $name supports this feature"
        )

    suspend
    fun createAudio(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support createAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun readAudio(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support readAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun updateAudio(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support updateAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun varyAudio(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support varyAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun createVideo(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support createVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun readVideo(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support readVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun updateVideo(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support updateVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun varyVideo(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support varyVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun listModels(): CommonMessage =
        throw NotImplementedError(
            "$name does not currently support listModels, try updating the project if $name supports this feature"
        )

}