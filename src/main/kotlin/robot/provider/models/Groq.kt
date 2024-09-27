package dev.supachain.robot.provider.models

import dev.supachain.Extension
import dev.supachain.robot.NetworkOwner
import dev.supachain.robot.provider.Actions
import dev.supachain.robot.provider.Provider

abstract class GroqBuilder : Provider<GroqBuilder>(), NetworkOwner {

}

internal interface GroqAPI : Extension<Groq> {

}

class GroqModels {

}

private sealed interface GroqActions : NetworkOwner, Actions, Extension<Groq>

class Groq {

}
