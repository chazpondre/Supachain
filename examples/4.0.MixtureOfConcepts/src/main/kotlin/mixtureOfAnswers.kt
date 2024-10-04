import dev.supachain.mixer.concept
import dev.supachain.mixer.mix
import dev.supachain.robot.Defaults.Chat
import dev.supachain.robot.Defaults.NoTools
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Ollama
import dev.supachain.utilities.Debug

fun main() {
    // Show messages between user, system and assistant
    Debug show "Messenger"

    // Initializing the robot instance without any tools
    val robot = Robot<Ollama, Chat, NoTools>()

    // Defining different concepts (philosophical, scientific, mathematical) for processing a question
    val philosophicalAnswer = concept { "Provide a philosophical perspective on: $input" }
    val scientificAnswer = concept { "Provide a scientific explanation for: $input" }
    val mathematicalAnswer = concept { "Provide a mathematical interpretation of: $input" }
    val historicalAnswer = concept { "Provide a historical analysis related to: $input" }
    val psychologicalAnswer = concept { "Provide a psychological interpretation of: $input" }
    val literaryAnswer = concept { "Provide a literary analysis or reference related to: $input" }

    // Defining a summary mixer that combines the answers into one coherent response
    val summary = mix { "Summarize the answers into one coherent response. Answers: $input" }

    // Creating a Mix object that applies all three concepts and then summarizes them
    val combinedAnswer =
                philosophicalAnswer +
                mathematicalAnswer +
                scientificAnswer +
                historicalAnswer +
                psychologicalAnswer +
                literaryAnswer to summary

    // Example question to be processed
    val question = "If nothing is still something, does nothing exist?"

    // Producing the final answer by using the robot to process the combined answer mix
    val finalAnswer = (combinedAnswer using { robot.chat(it).await() })(question)

    // Output the generated answer
    println(finalAnswer)
}
