import dev.supachain.robot.Defaults
import dev.supachain.robot.Robot
import dev.supachain.robot.answer.Answer
import dev.supachain.robot.provider.models.LocalAI
import dev.supachain.utilities.Debug

enum class Days {
    Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
}

private interface DaysDirectives {
    fun pickADay(prompt: String): Answer<Days>
}

fun main() {
    Debug show "Messenger"

    val robot = Robot<LocalAI, DaysDirectives, Defaults.NoTools> {
        defaultProvider {
            url = "http://localhost:8888"
            temperature = 0.0
            chatModel = "LocalAI-llama3-8b-function-call-v0.2"
        }
    }

    robot.pickADay("In Japan many business are closed on this day of the week")
        .onAnswer { println(it) }
        .await()
}