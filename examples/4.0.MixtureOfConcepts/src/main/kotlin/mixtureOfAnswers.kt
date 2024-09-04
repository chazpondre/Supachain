import dev.supachain.mixer.*

import dev.supachain.robot.Defaults.*
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Ollama

import dev.supachain.utilities.Debug

fun main() {
    // Show messages between user, system and assistant
    Debug show "Messenger"

    // Initializing the robot instance without any tools
    val robot = Robot<Ollama, Chat, NoTools>()

    // Defining different concepts (philosophical, scientific, mathematical) for processing a question
    val philosophicalAnswer = concept { "Provide a philosophical perspective on: $inputted" }
    val scientificAnswer = concept { "Provide a scientific explanation for: $inputted" }
    val mathematicalAnswer = concept { "Provide a mathematical interpretation of: $inputted" }
    val historicalAnswer = concept { "Provide a historical analysis related to: $inputted" }
    val psychologicalAnswer = concept { "Provide a psychological interpretation of: $inputted" }
    val literaryAnswer = concept { "Provide a literary analysis or reference related to: $inputted" }

    // Defining a summary mixer that combines the answers into one coherent response
    val summary = mix { "Summarize the answers into one coherent response. Answers: $results" }

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
    val finalAnswer = (combinedAnswer using { robot.chat(it).await() }).produce(question)

    // Output the generated answer
    println(finalAnswer)
}
