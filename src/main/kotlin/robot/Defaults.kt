@file:Suppress("unused")

package dev.supachain.robot

import dev.supachain.robot.director.directive.FromSystem
import dev.supachain.robot.answer.Answer

interface Defaults {
    interface NoTools

    interface Chat {
        @FromSystem("You are an helpful assistant. If you guess the answer, you should always use the tool if available." +
                " If you are uncertain about the answer, state that.\n")
        fun chat(prompt: String): Answer<String>
    }

    interface ChatMarkdown {
        @FromSystem("You are an helpful assistant. If you guess the answer, you should always use the tool if available." +
                " Your answer must be in Markdown formating. If you are uncertain about the answer, state that.\n")
        fun chat(prompt: String): Answer<String>
    }
}