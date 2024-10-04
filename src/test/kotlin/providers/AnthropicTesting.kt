package providers

import dev.supachain.robot.Defaults
import dev.supachain.robot.Robot
import dev.supachain.robot.messenger.MessageFilter
import dev.supachain.robot.provider.models.Anthropic
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

@ToolSet
class MockWeatherTool {
    fun get_weather(location: String, unit: String = "Celsius"): Int = 18
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
                        put("content", buildJsonArray {
                            add(buildJsonObject {
                                put("type", "text")
                                put("text", "Hello, World")
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
                useFormatMessage = false
                userMessagePrimer = false
                messageFilter = MessageFilter.OnlyUserMessages
            }
        }

        val answer = robot.chat("Hello, World").await()
        assertEquals("Hi! My name is Claude.", answer)
    }

    @Test
    fun `test chat request with function call`() {
        Debug show "Network"
        var messageNumber = 0

        val mockEngine = MockEngine { request ->
            // Check if the correct URL is used
            assertEquals(request.url.toString(), "https://api.anthropic.com/v1/messages")
            // Check if the correct headers are included in the request
            assertEquals("ANTHROPIC_API_KEY", request.headers["x-api-key"])
            assertEquals("2023-06-01", request.headers["anthropic-version"])

            // Verify the request body by reading the payload as JSON
            val requestBody = request.body.toByteReadPacket().readText()
            val jsonRequest = Json.parseToJsonElement(requestBody).jsonObject

            if (messageNumber == 0) {
                // Expected JSON structure
                val expectedJson = buildJsonObject {
                    put("model", JsonPrimitive("claude-3-5-sonnet-20240620"))
                    put("max_tokens", JsonPrimitive(2048))
                    put("temperature", JsonPrimitive(0.0))
                    put("stream", JsonPrimitive(false))
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", JsonPrimitive("user"))
                            put(
                                "content",
                                buildJsonArray {
                                    add(buildJsonObject {
                                        put("type", JsonPrimitive("text"))
                                        put("text", JsonPrimitive("What's the weather like in San Francisco?"))
                                    })
                                }
                            )
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
                                    })
                                    put("unit", buildJsonObject {
                                        put("type", JsonPrimitive("string"))
                                    })
                                })
                                put("required", buildJsonArray {
                                    add(JsonPrimitive("location"))
                                })
                            })
                        })
                    })
                }

                // Compare the JSON request and the expected JSON, ignoring order
                assertTrue(expectedJson.isContainedIn(jsonRequest))
                messageNumber++

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
                            "location": "San Francisco"
                          }
                        }
                      ]
                    }
                """.trimIndent()
                    ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                assertTrue(expectedChatToolCallMessage().isContainedIn(jsonRequest))
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
                          "text": "The weather is 18 degrees"
                        }
                      ]
                    }
                """.trimIndent()
                    ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val robot = Robot<Anthropic, Defaults.PlainChat, MockWeatherTool> {
            defaultProvider {
                apiKey = "ANTHROPIC_API_KEY"
                network.engine = { mockEngine }
                toolStrategy = BackAndForth
                userMessagePrimer = false
                useFormatMessage = false
                messageFilter = MessageFilter.OnlyUserMessages
            }
        }

        val question = "What's the weather like in San Francisco?"
        val answer = robot.chat(question).await()

        assertEquals("The weather is 18 degrees", answer)
    }

    private fun expectedChatToolCallMessage() = buildJsonObject {
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", JsonPrimitive("user"))
                put("content", buildJsonArray {
                    buildJsonArray {
                        add(buildJsonObject{
                            put("type", JsonPrimitive("tool_result"))
                            put("tool_use_id", JsonPrimitive("toolu_01A09q90qw90lq917835lq9"))
                            put("content", "18")
                        })
                    }
                })
            })
        })
    }
}