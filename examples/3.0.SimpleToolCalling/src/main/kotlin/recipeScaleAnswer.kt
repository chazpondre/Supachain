@file:Suppress("unused")

import dev.supachain.robot.Defaults.Chat
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Ollama
import dev.supachain.robot.tool.ToolSet
import dev.supachain.utilities.Debug
import kotlin.math.ceil

interface RecipeScaler {
    fun scaleIngredients(originalAmount: Double, targetServings: Int, originalServings: Int): Double {
        val scaleFactor = targetServings.toDouble() / originalServings
        return ceil(originalAmount * scaleFactor) // Round up to ensure enough ingredients
    }
}

@ToolSet
class RecipeScalingTools : RecipeScaler

fun main() {
    Debug show "Messenger"

    val robot = Robot<Ollama, Chat, RecipeScalingTools>()

    val message = "I'm baking cookies! The recipe is for 12 servings, but I only need enough for 4 people. How much flour should I use? The recipe calls for 1.5 cups of flour."
    val answer = robot.chat(message).await()
    println(answer)
}