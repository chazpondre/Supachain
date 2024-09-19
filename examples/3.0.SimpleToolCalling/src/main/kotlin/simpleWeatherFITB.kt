import dev.supachain.robot.Defaults.ChatMarkdown
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.LocalAI
import dev.supachain.robot.tool.ToolSet
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.utilities.Debug

typealias Coordinates = Array<Int>

@ToolSet
class Tools {
    fun getLongLat(city: String): Coordinates = arrayOf(0, 0)
    fun getWeather(coordinates: Coordinates): Double = 27.0
}

fun main() {
    Debug show "Messenger"

    val robot = Robot<LocalAI, ChatMarkdown, Tools> {
        defaultProvider {
            url = "http://localhost:8888"
            temperature = 0.0
            chatModel = "meta-llama-3.1-8b-instruct"
            toolStrategy = FillInTheBlank // This is the default strategy
        }
    }

    println(robot.chat("Whats the weather in Tokyo and Toronto").await())
}