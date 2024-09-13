import dev.supachain.robot.Defaults
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.OpenAI
import dev.supachain.robot.tool.Parameters
import dev.supachain.robot.tool.Tool
import dev.supachain.robot.tool.ToolSet
import dev.supachain.robot.tool.strategies.BackAndForth
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
            val expectedJson = expectedDefaultJson()
            val expectedMessage = testChatWithoutFunctionCallMessage()

            // Use a helper method for unordered JSON comparison
            assertTrue(expectedJson.isContainedIn(jsonRequest))
            assertTrue(expectedMessage.isContainedIn(jsonRequest))

            // Mock a successful response
            respond(
                content = ByteReadChannel(mockSuccessResponse()), // Use helper for response content
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // Set up the robot and perform the chat interaction
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
    private fun expectedDefaultJson(): JsonObject = buildJsonObject {
        put("frequency_penalty", JsonPrimitive(0.0))
        put("logprobs", JsonPrimitive(false))
        put("max_tokens", JsonPrimitive(2048))
        put("model", JsonPrimitive("gpt-4o"))
        put("n", JsonPrimitive(1))
        put("parallel_tool_calls", JsonPrimitive(true))
        put("presence_penalty", JsonPrimitive(0.0))
        put("temperature", JsonPrimitive(0.5))
        put("tool_choice", JsonPrimitive("auto"))
        put("top_p", JsonPrimitive(0.0))
        put("seed", JsonNull)
        put("stream", JsonPrimitive(false))
        put("user", JsonNull)
        putJsonObject("logit_bias") {}
        putJsonArray("stop") {}
        putJsonArray("tools") {}
    }

    private fun testChatWithoutFunctionCallMessage() = buildJsonObject {
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", JsonPrimitive("user"))
                put("content", JsonPrimitive("Hello, World"))
            })
        })
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

    ///////
    @Test
    fun `test chat request with function call`(): Unit = runBlocking {
        Debug show "None"

        var messageNumber = 0

        val mockEngine = MockEngine { request ->
            // Ensure the correct request details
            assertEquals("https://api.openai.com/v1/chat/completions", request.url.toString())
            assertEquals("Bearer OPENAI_API_KEY", request.headers[HttpHeaders.Authorization])

            // Read the full request body correctly
            val requestBody = request.body.toByteArray().decodeToString()

            // Deserialize the request body using Ktor's Json parser
            val jsonRequest = Json.decodeFromString<JsonObject>(requestBody)

            if (messageNumber == 0) {
                // Expected JSON structure (using concise helper function)
                val expectedJson = expectedDefaultJson()
                val expectedMessage = expectedChatWithFunctionCallMessage()
                val expectedToolJson = expectedToolsJson()

                // Use a helper method for unordered JSON comparison
                assertTrue(expectedJson.isContainedIn(jsonRequest))
                assertTrue(expectedMessage.isContainedIn(jsonRequest))
                assertTrue(expectedToolJson.isContainedIn(jsonRequest))


                messageNumber++

                // Mock a successful response
                respond(
                    content = ByteReadChannel(mockSuccessFunctionResponse()), // Use helper for response content
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else respond(
                content = ByteReadChannel(mockFinalFunctionResponse()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        // Set up the robot and perform the chat interaction
        val robot = Robot<OpenAI, Defaults.Chat, Tools> {
            defaultProvider {
                apiKey = "OPENAI_API_KEY"
                network.engine = { mockEngine }
                useOnlyUserMessages = true
                chatModel = models.chat.gpt4o
                toolStrategy = BackAndForth
            }
        }

        // Send chat request and assert the response
        val answer = robot.chat("What's the weather like in Boston today?").await()
        assertEquals("The weather in Boston, MA is 27", answer)
    }

    @ToolSet
    class Tools {
        @Tool("Get the current weather in a given location")
        @Parameters(["The city and state, e.g. San Francisco, CA"])
        fun getCurrentWeather(location: String, unit: Unit = Unit.Celsius): Int = 27

        enum class Unit { Celsius, Fahrenheit }
    }

    // Helper function to build expected JSON
    private fun expectedToolsJson(): JsonObject {
        return buildJsonObject {
            put("tools", buildJsonArray {
                add(buildJsonObject {
                    put("type", JsonPrimitive("function"))
                    put("function", buildJsonObject {
                        put("name", JsonPrimitive("getCurrentWeather"))
                        put("description", JsonPrimitive("Get the current weather in a given location"))
                        put("parameters", buildJsonObject {
                            put("type", JsonPrimitive("object"))
                            put("properties", buildJsonObject {
                                put("location", buildJsonObject {
                                    put("type", JsonPrimitive("String"))
                                    put("description", JsonPrimitive("The city and state, e.g. San Francisco, CA"))
                                })
                                put("unit", buildJsonObject {
                                    put("type", JsonPrimitive("String"))
                                    put(
                                        "enum",
                                        JsonArray(listOf(JsonPrimitive("Celsius"), JsonPrimitive("Fahrenheit")))
                                    )
                                })
                            })
                            put("required", JsonArray(listOf(JsonPrimitive("location"))))
                        })
                    })
                })
            })
        }
    }

    private fun expectedChatWithFunctionCallMessage() = buildJsonObject {
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", JsonPrimitive("user"))
                put("content", JsonPrimitive("What's the weather like in Boston today?"))
            })
        })
    }

    private fun expectedChatToolCallMessage() = buildJsonObject {
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", JsonPrimitive("function"))
                put("content", JsonPrimitive("27"))
            })
        })
    }

    // Helper function to mock a success response
    private fun mockSuccessFunctionResponse(): String {
        return """
    {
        "id": "chatcmpl-abc123",
        "object": "chat.completion",
        "created": 1699896916,
        "model": "gpt-4o-mini",
        "choices": [
            {
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": null,
                    "tool_calls": [
                        {
                            "id": "call_abc123",
                            "type": "function",
                            "function": {
                                "name": "getCurrentWeather",
                                "arguments": "{\"location\": \"Boston, MA\"}"
                            }
                        }
                    ]
                },
                "logprobs": null,
                "finish_reason": "tool_calls"
            }
        ],
        "usage": {
            "prompt_tokens": 82,
            "completion_tokens": 17,
            "total_tokens": 99
        }
    }
""".trimIndent()
    }

    private fun mockFinalFunctionResponse(): String {
        return """
    {
        "id": "chatcmpl-abc123",
        "object": "chat.completion",
        "created": 1699896916,
        "model": "gpt-4o-mini",
        "choices": [
            {
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": "The weather in Boston, MA is 27"
                },
                "logprobs": null,
                "finish_reason": "tool_calls"
            }
        ],
        "usage": {
            "prompt_tokens": 82,
            "completion_tokens": 17,
            "total_tokens": 99
        }
    }
""".trimIndent()
    }
}