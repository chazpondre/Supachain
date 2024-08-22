import dev.supachain.robot.Defaults.*
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.LocalAI


fun main() {
    val robot = Robot<LocalAI, Chat, NoTools> {
        defaultProvider {
            url = "http://localhost:8888"
            chatModel = "meta-llama-3.1-8b-instruct"
        }
    }

    val answer = robot.chat("Whats 11 * 7 + 1").await()
    println(answer)
}