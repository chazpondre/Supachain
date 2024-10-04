import dev.supachain.mixer.concept
import dev.supachain.mixer.mix
import dev.supachain.robot.Defaults.Chat
import dev.supachain.robot.Defaults.NoTools
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Groq
import dev.supachain.utilities.Debug

fun main() {
    Debug show "Messenger"
    val robot = Robot<Groq, Chat, NoTools>{
        defaultProvider {
            apiKey = "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"
            model = models.chat.llama32_11BTextPreview
        }
    }

//    val robot = Robot<Ollama, Chat, NoTools>()
    val pros = concept { "Show me all the pros for this issue: $input" }
    val cons = concept { "Show me all the cons for this issue: $input" }

    val summary = mix {
        "Show the pros and cons and weigh out the answers. " +
                "If you had a third holistic perspective share it. Pros/Cons: $input"
    }

    val answer = pros + cons to summary

    val question = "Should I own a macbook?"

    val generatedAnswer = (answer using { robot.chat(it).await() })(question)

    println(generatedAnswer)
}