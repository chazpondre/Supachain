@file:Suppress("unused")

import dev.supachain.robot.Defaults.Chat
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Ollama
import dev.supachain.robot.tool.ToolSet
import dev.supachain.utilities.Debug
import kotlin.math.pow

interface SimpleMath {
    fun add(left: Double, right: Double): Double = left + right
    fun subtract(left: Double, right: Double): Double = left - right
    fun multiply(left: Double, right: Double): Double = left * right

    fun divide(left: Double, right: Double): Double {
        require(right != 0.0) { "Division by zero is not allowed" }
        return left / right
    }

    fun power(a: Double, b: Double): Double = a.pow(b)
}

@ToolSet
class SimpleMathExampleTools : SimpleMath

fun main() {
    Debug show "Messenger"

    val robot = Robot<Ollama, Chat, SimpleMathExampleTools>()

    val problem = "What is (16 * 16) / 2 - 2 ^ 2?" // 128 - 4 = 123
    val answer = robot.chat(problem).await()
    println(answer)
}

