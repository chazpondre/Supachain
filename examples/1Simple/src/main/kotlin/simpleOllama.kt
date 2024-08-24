import dev.supachain.robot.Defaults.*
import dev.supachain.robot.Robot
import dev.supachain.robot.answer.Answer
import dev.supachain.robot.provider.models.Ollama

private interface TrueOrFalse {
    fun chat(prompt: String): Answer<Boolean>
}

fun main() {
    val robot = Robot<Ollama, TrueOrFalse, NoTools>()
    val answer = robot.chat("The tallest building in the world is taller than Mt. Everest").await()
    println(answer)
}