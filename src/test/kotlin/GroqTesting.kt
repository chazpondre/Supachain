import dev.supachain.robot.Defaults
import dev.supachain.robot.Robot
import dev.supachain.robot.messenger.MessageFilter
import dev.supachain.robot.provider.models.Groq
import dev.supachain.robot.provider.models.GroqException
import dev.supachain.robot.tool.Parameters
import dev.supachain.robot.tool.Tool
import dev.supachain.robot.tool.ToolSet
import dev.supachain.robot.tool.strategies.BackAndForth
import dev.supachain.utilities.Debug
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class GroqTesting {

    private fun String.asJSON() = Json.parseToJsonElement(this).jsonObject

    // language=JSON
    private val expectedSimpleRequest = """
        {
          "frequency_penalty": 0.0,
          "max_tokens": 2048,
          "messages": [
            {
              "role": "user",
              "content": "Hi!"
            }
          ],
          "model": "llama-3.1-70b-versatile",
          "presence_penalty": 0.0,
          "stop": null,
          "stream": false,
          "temperature": 0.0,
          "tool_choice": "auto",
          "top_p": 0.0
        }
    """

    // language=JSON
    private val expectedSimpleSuccessResponse = """
        {
          "id": "chatcompletion_123456",
          "created": 1643723400,
          "model": "llama-3.1-70b-versatile",
          "serviceTier": "standard",
          "systemFingerprint": "abcdefg",
          "object": "chat.completion",
          "completion": true,
          "usage": {
            "prompt_tokens": 24,
            "completion_tokens": 377,
            "total_tokens": 401,
            "prompt_time": 0.009,
            "completion_time": 0.774,
            "total_time": 0.783
          },
          "choices": [
            {
              "index": 0,
              "message": {
                "role": "assistant",
                "content": "Hi"
              },
              "logprobs": null,
              "finish_reason": "finish"
            }
          ],
          "error": null
        }
    """

    // language=JSON
    private val expectedToolCallRequest = """
        {
          "frequency_penalty": 0.0,
          "max_tokens": 2048,
          "messages": [
            {
              "role": "user",
              "content": "What is the weather like in San Francisco today?"
            }
          ],
          "model": "llama-3.1-70b-versatile",
          "presence_penalty": 0.0,
          "stop": null,
          "stream": false,
          "temperature": 0.0,
          "tool_choice": "auto",
          "top_p": 0.0,
          "tools": [
            {
              "type": "function",
              "function": {
                "name": "getWeather",
                "description": "Get the current weather for a given location",
                "parameters": {
                  "type": "object",
                  "properties": {
                    "location": {
                      "type": "string",
                      "description": "The city and state, e.g. San Francisco, CA"
                    },
                    "unit": {
                      "type": "string",
                      "enum": ["Celsius", "Fahrenheit"]
                    }
                  },
                  "required": ["location"]
                }
              }
            }
          ]
        }
    """

    // language=JSON
    private val expectedToolCallSuccessResponse = """
        {
          "id": "chatcompletion_123456",
          "created": 1643723400,
          "model": "llama-3.1-70b-versatile",
          "serviceTier": "standard",
          "systemFingerprint": "abcdefg",
          "object": "chat.completion",
          "completion": true,
          "usage": {
            "prompt_tokens": 24,
            "completion_tokens": 377,
            "total_tokens": 401,
            "prompt_time": 0.009,
            "completion_time": 0.774,
            "total_time": 0.783
          },
          "choices": [
            {
              "index": 0,
              "message": {
                "role": "assistant",
                "content": "",
                "tool_calls": [
                    {
                      "id": "call_123",
                      "type": "function",
                      "name": "getWeather",
                      "function": {
                          "name": "getWeather",
                          "arguments": "{\"location\": \"San Francisco, CA\"}"
                      }
                    }
                  ]
              },
              "logprobs": null,
              "finish_reason": "tool_calls"
            }
          ],
          "error": null
        }
    """

    // language=JSON
    private val expectedToolResultRequest = """
        {
          "frequency_penalty": 0.0,
          "max_tokens": 2048,
          "messages": [
            {
              "role": "user",
              "content": "What is the weather like in San Francisco today?"
            },
            {
              "role": "assistant",
              "content": "",
              "tool_calls": [
                  {
                      "id": "call_123",
                      "type": "function",
                      "function": {
                          "name": "getWeather",
                          "arguments": "{\"location\": \"San Francisco, CA\"}"
                      }
                  }
              ]
            },
            {
              "role": "tool",
              "content": "27",
              "tool_call_id": "call_123"
            }
          ],
          "model": "llama-3.1-70b-versatile",
          "presence_penalty": 0.0,
          "stop": null,
          "stream": false,
          "temperature": 0.0,
          "tool_choice": "auto",
          "top_p": 0.0,
          "tools": [
            {
              "type": "function",
              "function": {
                "name": "getWeather",
                "description": "Get the current weather for a given location",
                "parameters": {
                  "type": "object",
                  "properties": {
                    "location": {
                      "type": "string",
                      "description": "The city and state, e.g. San Francisco, CA"
                    },
                    "unit": {
                      "type": "string",
                      "enum": ["Celsius", "Fahrenheit"]
                    }
                  },
                  "required": ["location"]
                }
              }
            }
          ]
        }
    """

    // language=JSON
    private val expectedToolResultSuccessResponse = """
        {
          "id": "chatcompletion_123456",
          "object": "chat.completion",
          "created": 1643723400,
          "model": "llama-3.1-70b-versatile",
          "systemFingerprint": "abcdefg",
          "serviceTier": "standard",
          "completion": true,
          "usage": {
            "prompt_tokens": 24,
            "completion_tokens": 377,
            "total_tokens": 401,
            "prompt_time": 0.009,
            "completion_time": 0.774,
            "total_time": 0.783
          },
          "choices": [
            {
             "message": {
                    "role": "assistant",
                    "content": "The current temperature in San Francisco, CA is 27°C."
                  },
              "finish_reason": "stop",
              "index": 0
            }
          ],
          "error": null
        }
    """

    // language=JSON
    val expectedErrorMessage = """
        {
          "error": {
            "message": "String - description of the specific error",
            "type": "invalid_request_error"
          }
        }
    """.trimIndent()

    @ToolSet
    class Tools {
        @Tool("Get the current weather for a given location")
        @Parameters(["The city and state, e.g. San Francisco, CA"])
        fun getWeather(location: String, unit: Unit = Unit.Celsius): Int = 27
        enum class Unit { Celsius, Fahrenheit }
    }

    private fun testBot(mockEngine: MockEngine) =
        Robot<Groq, Defaults.Chat, Tools> {
            Debug show "Network"
            defaultProvider {
                apiKey = "API_KEY"
                network.engine = { mockEngine }
                useFormatMessage = false
                userMessagePrimer = false
                messageFilter = MessageFilter.OnlyUserMessages
                model = models.chat.llama31_70BVersatile
                toolStrategy = BackAndForth
            }
        }

    private fun MockRequestHandleScope.simpleSuccessResponse() = respond(
        ByteReadChannel(expectedSimpleSuccessResponse),
        HttpStatusCode.OK,
        headersOf(HttpHeaders.ContentType, "application/json")
    )

    private fun MockRequestHandleScope.errorResponse() = respond(
        ByteReadChannel(expectedErrorMessage),
        HttpStatusCode.OK,
        headersOf(HttpHeaders.ContentType, "application/json")
    )

    private fun MockRequestHandleScope.toolCallSuccessResponse() = respond(
        ByteReadChannel(expectedToolCallSuccessResponse),
        HttpStatusCode.OK,
        headersOf(HttpHeaders.ContentType, "application/json")
    )

    private fun MockRequestHandleScope.toolResultSuccessResponse() = respond(
        ByteReadChannel(expectedToolResultSuccessResponse),
        HttpStatusCode.OK,
        headersOf(HttpHeaders.ContentType, "application/json")
    )


    @Test
    fun `Has Correct URL and Headers`() {
        val mockEngine = MockEngine { request ->
            // Ensure the correct request details
            assertEquals("https://api.groq.com/openai/v1/chat/completions", request.url.toString())
            assertEquals("Bearer API_KEY", request.headers[HttpHeaders.Authorization])
            simpleSuccessResponse()
        }

        testBot(mockEngine).chat("").await()
    }

    @Test
    fun `Has Correct Request (simple)`() {
        val mockEngine = MockEngine { request ->
            val requestJSON = Json.decodeFromString<JsonObject>(request.body.toByteArray().decodeToString())
            assertTrue(expectedSimpleRequest.asJSON().isContainedIn(requestJSON))
            simpleSuccessResponse()
        }

        testBot(mockEngine).chat("Hi!").await()
    }

    @Test
    fun `Has Correct Response (simple)`() {
        val mockEngine = MockEngine { _ -> simpleSuccessResponse() }
        val response = testBot(mockEngine).chat("").await()

        assertEquals("Hi", response)
    }

    @Test
    fun `Has Correct Request (tool call)`() {
        var messageNumber = 0
        val mockEngine = MockEngine { request ->
            val requestJSON = Json.decodeFromString<JsonObject>(request.body.toByteArray().decodeToString())
            when (messageNumber++) {
                0 -> {
                    assertTrue(expectedToolCallRequest.asJSON().isContainedIn(requestJSON))
                    toolCallSuccessResponse()
                }

                else -> {
                    assertTrue(expectedToolResultRequest.asJSON().isContainedIn(requestJSON))
                    toolResultSuccessResponse()
                }
            }
        }

        testBot(mockEngine).chat("What is the weather like in San Francisco today?").await()
    }

    @Test
    fun `Has Correct Response (tool call)`() {
        var messageNumber = 0
        val mockEngine = MockEngine { request ->
            when (messageNumber++) {
                0 -> toolCallSuccessResponse()
                else -> toolResultSuccessResponse()
            }
        }

        val response = testBot(mockEngine).chat("What is the weather like in San Francisco today?").await()
        assertEquals("The current temperature in San Francisco, CA is 27°C.", response)
    }

    @Test
    fun `Has Error Response`() {
        val mockEngine = MockEngine { _ -> errorResponse() }
        assertThrows<GroqException> { testBot(mockEngine).chat("").await() }
    }
}