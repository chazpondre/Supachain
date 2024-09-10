import dev.supachain.robot.Defaults
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.Anthropic
import dev.supachain.robot.tool.ToolSet
import dev.supachain.robot.tool.strategies.BackAndForth
import dev.supachain.utilities.Debug
import dev.supachain.utilities.toJson
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

@ToolSet
class MockWeatherTool {
    fun get_weather(location: String, unit: String): String = "The weather in $location is 18°C."
}

class AnthropicTesting {

    @Test
    fun `test chat request without function call`(): Unit = runBlocking {
        Debug show "None"
        val mockEngine = MockEngine { request ->
            // Check if the correct URL is used
            assertEquals(request.url.toString(), "https://api.anthropic.com/v1/messages")
            // Check if the correct headers are included in the request
            assertEquals("ANTHROPIC_API_KEY", request.headers["x-api-key"])
            assertEquals("2023-06-01", request.headers["anthropic-version"])

            // Verify the request body by reading the payload as JSON
            val requestBody = request.body.toByteReadPacket().readText()
            val jsonRequest = Json.parseToJsonElement(requestBody).jsonObject

            // Expected JSON structure
            val expectedJson = buildJsonObject {
                put("model", JsonPrimitive("claude-3-5-sonnet-20240620"))
                put("max_tokens", JsonPrimitive(2048))
                put("temperature", JsonPrimitive(0.0))
                put("stream", JsonPrimitive(false))
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", JsonPrimitive("user"))
                        put("content", JsonPrimitive("Hello, World"))
                    })
                })
            }

            // Compare the JSON request and the expected JSON, ignoring order
            assertTrue(jsonRequest.equalsJsonUnordered(expectedJson))

            respond(
                content = ByteReadChannel(
                    """
                    {
                      "content": [
                        {
                          "text": "Hi! My name is Claude.",
                          "type": "text"
                        }
                      ],
                      "id": "msg_013Zva2CMHLNnXjNJJKqJ2EF",
                      "model": "claude-3-5-sonnet-20240620",
                      "role": "assistant",
                      "stop_reason": "end_turn",
                      "stop_sequence": null,
                      "type": "message",
                      "usage": {
                        "input_tokens": 2095,
                        "output_tokens": 503
                      }
                    }
                """.trimIndent()
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val robot = Robot<Anthropic, Defaults.Chat, Defaults.NoTools> {
            defaultProvider {
                apiKey = "ANTHROPIC_API_KEY"
                network.engine = { mockEngine }
                useOnlyUserMessages = true
            }
        }

        val answer = robot.chat("Hello, World").await()
        assertEquals("Hi! My name is Claude.", answer)
    }

    @Test
    fun `test chat request with function call`() {
        Debug show "Network"
        val mockEngine = MockEngine { request ->
            // Check if the correct URL is used
            assertEquals(request.url.toString(), "https://api.anthropic.com/v1/messages")
            // Check if the correct headers are included in the request
            assertEquals("ANTHROPIC_API_KEY", request.headers["x-api-key"])
            assertEquals("2023-06-01", request.headers["anthropic-version"])

            // Verify the request body by reading the payload as JSON
            val requestBody = request.body.toByteReadPacket().readText()
            val jsonRequest = Json.parseToJsonElement(requestBody).jsonObject

            // Expected JSON structure
            val expectedJson = buildJsonObject {
                put("model", JsonPrimitive("claude-3-5-sonnet-20240620"))
                put("max_tokens", JsonPrimitive(2048))
                put("temperature", JsonPrimitive(0.0))
                put("stream", JsonPrimitive(false))
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", JsonPrimitive("user"))
                        put("content", JsonPrimitive("What's the weather like in San Francisco?"))
                    })
                })

                // Add the tools key and its array
                put("tools", buildJsonArray {
                    add(buildJsonObject {
                        put("name", JsonPrimitive("get_weather"))
                        put("description", JsonPrimitive(""))
                        put("input_schema", buildJsonObject {
                            put("type", JsonPrimitive("object"))
                            put("properties", buildJsonObject {
                                put("location", buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put("description", JsonPrimitive(""))
                                })
                                put("unit", buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put("description", JsonPrimitive(""))
                                })
                            })
                            put("required", buildJsonArray {
                                add(JsonPrimitive("location"))
                                add(JsonPrimitive("unit"))
                            })
                        })
                    })
                })
            }

            // Compare the JSON request and the expected JSON, ignoring order
            assertTrue(jsonRequest.equalsJsonUnordered(expectedJson))

            respond(
                content = ByteReadChannel(
                    """
                    {
                      "id": "msg_01Aq9w938a90dw8q",
                      "model": "claude-3-5-sonnet-20240620",
                      "stop_reason": "tool_use",
                      "role": "assistant",
                      "content": [
                        {
                          "type": "text",
                          "text": "<thinking>I need to use the get_weather, and the user wants SF, which is likely San Francisco, CA.</thinking>"
                        },
                        {
                          "type": "tool_use",
                          "id": "toolu_01A09q90qw90lq917835lq9",
                          "name": "get_weather",
                          "input": {
                            "location": "San Francisco",
                            "unit": "celsius"
                          }
                        }
                      ]
                    }
                """.trimIndent()
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val robot = Robot<Anthropic, Defaults.PlainChat, MockWeatherTool> {
            defaultProvider {
                apiKey = "ANTHROPIC_API_KEY"
                network.engine = { mockEngine }
                toolStrategy = BackAndForth
                useOnlyUserMessages = true
            }
        }

        val question = "What's the weather like in San Francisco?"
        val answer = robot.chat(question).await()

        // Validate if the tool call is properly invoked and response is correct
        assertEquals("<thinking>I need to use the get_weather, and the user wants SF, which is likely San Francisco, CA.</thinking>", answer)
    }
}
