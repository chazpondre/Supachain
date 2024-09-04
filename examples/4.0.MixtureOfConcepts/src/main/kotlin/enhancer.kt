import dev.supachain.mixer.*
import dev.supachain.robot.Defaults.Chat
import dev.supachain.robot.Defaults.NoTools
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Ollama
import dev.supachain.utilities.Debug

fun main() {
    Debug show "Messenger"
    val robot = Robot<Ollama, Chat, NoTools>()

    val firstAttempt = concept { "Give an answer to the following question: $inputted" }

    val betterAnswer = mix {
        "```<Test Rubric>Ways to improve:\n" +
                "1. You must use concise and clear language\n" +
                "2. Carefully read and analyze the question to fully grasp its intent and scope\n" +
                "3. Organize your points logically and ensure a smooth flow of information.\n" +
                "4. Use simple language and avoid jargon.\n" +
                "5. Break down complex concepts into smaller, easier-to-understand parts.\n" +
                "6. Provide examples or illustrations to support your explanations.\n" +
                "7. Make sure your answer is directly relevant to the question asked.\n" +
                "8. Avoid going off on tangents or providing unnecessary information.\n" +
                "9. Avoid vague or general statements\n" +
                "10 Do not say what your doing, just do it.\n" +
                "11. Acknowledge and address potential counterarguments or alternative viewpoints.</Test Rubric>\n" +
                "The last_article_answer received a grade of B= based on the Test Rubric. " +
                "Show the improved A+ article last_article_answer and improve the length. Question:$inputted\n" +
                "last_article_answer=```$results```"
    }

    val answer= firstAttempt to betterAnswer * 2

    val question = "What do you know about the Steel Drum?"

    val generatedAnswer = (answer using { robot.chat(it).await() }).produce(question)

    println(generatedAnswer)
}