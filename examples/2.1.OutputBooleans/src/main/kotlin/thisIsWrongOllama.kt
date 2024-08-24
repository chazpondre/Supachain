import dev.supachain.robot.Defaults.Chat
import dev.supachain.robot.Defaults.NoTools
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Ollama



fun main() {
    val robot = Robot<Ollama, Chat, NoTools>()
    val answer = robot.chat("Whats 11 * 7 + 1").await()
    println(answer)
}