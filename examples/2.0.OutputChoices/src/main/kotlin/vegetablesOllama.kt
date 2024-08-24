import dev.supachain.robot.Defaults.NoTools
import dev.supachain.robot.Robot
import dev.supachain.robot.answer.Answer
import dev.supachain.robot.provider.models.Ollama
import dev.supachain.utilities.Debug

enum class Vegetable {
    Tomato, Broccoli, Orange, Kiwi, Strawberry, Potato
}

private interface VegetableDirectives {
    fun pickAVegetable(prompt: String): Answer<Vegetable>
}

fun main() {
    Debug show "Messenger"

    val robot = Robot<Ollama, VegetableDirectives, NoTools>()

    robot.pickAVegetable("Which vegetable has the highest vitamin C content?")
        .onAnswer {
            if (it == Vegetable.Kiwi) {
                println("Correct! Kiwis are rich in vitamin C.")
            } else {
                println("Incorrect. Kiwis have the highest vitamin C content among the options.")
            }
        }
        .await()
}