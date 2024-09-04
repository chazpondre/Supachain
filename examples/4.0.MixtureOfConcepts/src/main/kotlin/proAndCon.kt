import dev.supachain.mixer.concept
import dev.supachain.mixer.mix
import dev.supachain.mixer.plus
import dev.supachain.mixer.using
import dev.supachain.robot.Defaults.Chat
import dev.supachain.robot.Defaults.NoTools
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Ollama
import dev.supachain.utilities.Debug

fun main() {
    Debug show "Messenger"
    val robot = Robot<Ollama, Chat, NoTools>()

    val pros = concept { "Show me all the pros for this issue: $inputted" }

    val cons = concept { "Show me all the cons for this issue: $inputted" }

    val summary = mix {
        "Show the pros and cons and weigh out the answers. " +
                "If you had a third holistic perspective share it. Pros/Cons: $inputted"
    }

    val answer = pros + cons to summary

    val question = "Should I own a macbook?"

    val generatedAnswer = (answer using { robot.chat(it).await() }).produce(question)

    println(generatedAnswer)
}