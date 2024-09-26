/*
░░░░░░░░░░░░░░░░░░░░░░░░░░       ░░░        ░░░      ░░░  ░░░░  ░░        ░░░      ░░░        ░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓       ▓▓▓      ▓▓▓▓  ▓▓ ▓  ▓▓  ▓▓▓▓  ▓▓      ▓▓▓▓▓      ▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████  ███  ███  ████████  ███   ██  ████  ██  ██████████████  █████  █████████████████████████████
██████████████████████████  ████  ██        ███      █ ██      ███        ███      ██████  █████████████████████████████
 */
@file:Suppress( "unused")

package dev.supachain.robot.provider

sealed interface CommonRequest

internal interface CommonChatRequest : CommonRequest
internal interface CommonEmbedRequest : CommonRequest
internal interface CommonModRequest : CommonRequest
internal interface CommonAudioRequest : CommonRequest
internal interface CommonImageRequest : CommonRequest
