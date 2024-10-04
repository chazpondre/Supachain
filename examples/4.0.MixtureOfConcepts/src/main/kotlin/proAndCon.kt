import dev.supachain.mixer.concept
import dev.supachain.mixer.mix
import dev.supachain.mixer.usingInParallel
import dev.supachain.robot.Defaults.Chat
import dev.supachain.robot.Defaults.NoTools
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Ollama
import dev.supachain.utilities.Debug
import kotlin.system.measureTimeMillis

suspend fun main() {
    Debug show "Messenger"
    val robot = Robot<Ollama, Chat, NoTools>()

    val pros = concept { "Show me all the pros for this issue: $input" }
    val cons = concept { "Show me all the cons for this issue: $input" }

    val summary = mix {
        "Show the pros and cons and weigh out the answers. " +
                "If you had a third holistic perspective share it. Pros/Cons: $input"
    }

    val answer = pros + cons to summary

    val question = "Should I own a macbook?"

    val time = measureTimeMillis {
        val generatedAnswer = (answer usingInParallel robot::chat)(question)
        println(generatedAnswer)
    }

    println("Time taken: $time")
}

