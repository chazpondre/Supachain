import dev.supachain.robot.Defaults
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.OpenAI
import dev.supachain.utilities.Debug
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class OpenAITesting {

    @Test
    fun `test chat request without function call`(): Unit = runBlocking {
        Debug show "None"

        val mockEngine = MockEngine { request ->
            // Ensure the correct request details
            assertEquals("https://api.openai.com/v1/chat/completions", request.url.toString())
            assertEquals("Bearer OPENAI_API_KEY", request.headers[HttpHeaders.Authorization])

            // Deserialize the request body using Ktor's Json parser
            val requestBody = request.body.toByteReadPacket().readText()
            val jsonRequest = Json.decodeFromString<JsonObject>(requestBody)

            // Expected JSON structure (using concise helper function)
            val expectedJson = buildExpectedJson()

            // Use a helper method for unordered JSON comparison
            assertTrue(expectedJson.isSubsetIn(jsonRequest))

            // Mock a successful response
            respond(
                content = ByteReadChannel(mockSuccessResponse()), // Use helper for response content
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // Setup the robot and perform the chat interaction
        val robot = Robot<OpenAI, Defaults.Chat, Defaults.NoTools> {
            defaultProvider {
                apiKey = "OPENAI_API_KEY"
                network.engine = { mockEngine }
                useOnlyUserMessages = true
                chatModel = models.chat.gpt4o
            }
        }

        // Send chat request and assert the response
        val answer = robot.chat("Hello, World").await()
        assertEquals("\n\nHello there, how may I assist you today?", answer)
    }

    // Helper function to build expected JSON
    private fun buildExpectedJson(): JsonObject {
        return buildJsonObject {
            put("model", JsonPrimitive("gpt-4o"))
            put("temperature", JsonPrimitive(0.5))
            put("top_p", JsonPrimitive(0.0))
            put("max_tokens", JsonPrimitive(2048))
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", JsonPrimitive("user"))
                    put("content", JsonPrimitive("Hello, World"))
                })
            })
        }
    }

    // Helper function to mock a success response
    private fun mockSuccessResponse(): String {
        return """
        {
            "id": "chatcmpl-123",
            "object": "chat.completion",
            "created": 1677652288,
            "model": "gpt-4o-mini",
            "system_fingerprint": "fp_44709d6fcb",
            "choices": [{
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": "\n\nHello there, how may I assist you today?"
                },
                "logprobs": null,
                "finish_reason": "stop"
            }],
            "usage": {
                "prompt_tokens": 9,
                "completion_tokens": 12,
                "total_tokens": 21
            }
        }
    """.trimIndent()
    }

}