package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.NetworkOwner
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.Provider

abstract class GroqBuilder : Provider<GroqBuilder>(), NetworkOwner {

}

internal interface GroqAPI : Extension<Groq> {

}


/*
░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  ░░░░  ░░░      ░░░       ░░░        ░░  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒   ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓        ▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓  ▓▓      ▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
████████████████████████████████████  █  █  ██  ████  ██  ████  ██  ████████  ██████████████████████████████████████████
████████████████████████████████████  ████  ███      ███       ███        ██        ████████████████████████████████████
 */
@Suppress("unused")
interface GroqModels {

    val models get() = Models

    object Models {
        val chat = Chat
        val audio = Audio
        val vision = Vision
    }

    object Chat {
        // Llama 3 Groq Models
        val llama3Groq70BToolUsePreview: String get() = "llama3-groq-70b-8192-tool-use-preview"
        val llama3Groq8BToolUsePreview: String get() = "llama3-groq-8b-8192-tool-use-preview"

        // Meta Llama 3.1 Preview Models
        val llama31_70BPreview: String get() = "llama-3.1-70b-versatile"
        val llama31_8BPreview: String get() = "llama-3.1-8b-instant"

        // Meta Llama 3.2 Preview Models
        val llama32_1BTextPreview: String get() = "llama-3.2-1b-preview"
        val llama32_3BTextPreview: String get() = "llama-3.2-3b-preview"
        val llama32_11BTextPreview: String get() = "llama-3.2-11b-text-preview"
        val llama32_90BTextPreview: String get() = "llama-3.2-90b-text-preview"

        // Llama 3.1 Large Model (Offline)
        val llama31_405B: String get() = "llama-3.1-405b"

        // Meta Llama Guard 3 Model
        val llamaGuard3_8B: String get() = "llama-guard-3-8b"

        // Meta Llama 3 General
        val metaLlama3_70B: String get() = "llama3-70b-8192"
        val metaLlama3_8B: String get() = "llama3-8b-8192"

        // Google's Gemma Models
        val gemma2_9B: String get() = "gemma2-9b-it"
        val gemma7B: String get() = "gemma-7b-it"

        // Mistral's Mixtral Model
        val mixtral8x7B: String get() = "mixtral-8x7b-32768"
    }

    object Audio {
        // HuggingFace Distil-Whisper Model
        val distilWhisperEnglish: String get() = "distil-whisper-large-v3-en"

        // OpenAI Whisper Model
        val whisperLargeV3: String get() = "whisper-large-v3"
    }

    object Vision {
        // Haotian Liu's LLAVA Model
        val llavaV1_5_7B: String get() = "llava-v1.5-7b-4096-preview"
    }
}

private sealed interface GroqActions : NetworkOwner, Actions, Extension<Groq>


class Groq {
}

