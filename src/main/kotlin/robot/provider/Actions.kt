/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░      ░░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████        ██  ████  █████  ████████  █████  ████  ██  ██    ████████  ██████████████████████████
██████████████████████████  ████  ███      ██████  █████        ███      ███  ███   ███      ███████████████████████████
 */
package dev.supachain.robot.provider

import dev.supachain.robot.director.DirectorCore
import dev.supachain.robot.provider.responses.CommonResponse

/**
 * Interface defining the actions that a robot can perform.
 *
 * This interface outlines the various actions that a robot can take, such as chatting,
 * generating embeddings, moderating content, and more. Each action is represented by a
 * suspend function that returns a type of `CommonResponse`.
 *
 * @since 0.1.0-alpha

 */
internal interface Actions {
    val name: String

    suspend
    fun chat(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support chat, try updating the project if $name supports this feature"
        )

    suspend
    fun embedding(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support embedding, try updating the project if $name supports this feature"
        )

    suspend
    fun moderation(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support moderation, try updating the project if $name supports this feature"
        )

    suspend
    fun createSpeech(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support createSpeech, try updating the project if $name supports this feature"
        )

    suspend
    fun createTranscription(director: DirectorCore)
            : CommonResponse =
        throw NotImplementedError(
            "$name does not currently support createTranscription, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun createTranslation(director: DirectorCore)
            : CommonResponse =
        throw NotImplementedError(
            "$name does not currently support createTranslation, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun createFineTune(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support createFineTune, try updating the project if $name supports this feature"
        )

    suspend
    fun listFineTunes(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support listFineTunes, try updating the project if $name supports this feature"
        )

    suspend
    fun listFineTuneEvents(director: DirectorCore)
            : CommonResponse =
        throw NotImplementedError(
            "$name does not currently support listFineTuneEvents, try updating the project if $name supports this " +
                    "feature"
        )

    suspend
    fun listFineTuneCheckPoints(director: DirectorCore)
            : CommonResponse =
        throw NotImplementedError(
            "$name does not currently support listFineTuneCheckPoints, try updating the project if $name supports this" +
                    " feature"
        )

    suspend
    fun fineTuneInfo(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support fineTuneInfo, try updating the project if $name supports this feature"
        )

    suspend
    fun fineTuneCancel(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support fineTuneCancel, try updating the project if $name supports this feature"
        )

    suspend
    fun createBatch(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support createBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun getBatch(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support getBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun cancelBatch(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support cancelBatch, try updating the project if $name supports this feature"
        )

    suspend
    fun listBatches(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support listBatches, try updating the project if $name supports this feature"
        )

    suspend
    fun uploadFile(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support uploadFile, try updating the project if $name supports this feature"
        )

    suspend
    fun listFiles(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support listFiles, try updating the project if $name supports this feature"
        )

    suspend
    fun getFile(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support getFile, try updating the project if $name supports this feature"
        )

    suspend
    fun deleteFile(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support deleteFile, try updating the project if $name supports this feature"
        )

    suspend
    fun getFileContent(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support getFileContent, try updating the project if $name supports this feature"
        )

    suspend
    fun createImage(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support createImage, try updating the project if $name supports this feature"
        )

    suspend
    fun readImage(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support readImage, try updating the project if $name supports this feature"
        )

    suspend
    fun updateImage(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support updateImage, try updating the project if $name supports this feature"
        )

    suspend
    fun varyImage(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support varyImage, try updating the project if $name supports this feature"
        )

    suspend
    fun createAudio(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support createAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun readAudio(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support readAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun updateAudio(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support updateAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun varyAudio(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support varyAudio, try updating the project if $name supports this feature"
        )

    suspend
    fun createVideo(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support createVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun readVideo(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support readVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun updateVideo(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support updateVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun varyVideo(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support varyVideo, try updating the project if $name supports this feature"
        )

    suspend
    fun listModels(director: DirectorCore): CommonResponse =
        throw NotImplementedError(
            "$name does not currently support listModels, try updating the project if $name supports this feature"
        )

}