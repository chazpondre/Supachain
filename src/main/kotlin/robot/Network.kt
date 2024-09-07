/*
░░░░░░░░░░░░░░░░░░░░░░░░░░   ░░░  ░░        ░░        ░░  ░░░░  ░░░      ░░░       ░░░  ░░░░  ░░░░░░░░░░░░░░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒  ▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓  ▓  ▓▓      ▓▓▓▓▓▓▓  ▓▓▓▓▓        ▓▓  ▓▓▓▓  ▓▓       ▓▓▓     ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
██████████████████████████  ██    ██  ███████████  █████   ██   ██  ████  ██  ███  ███  ███  ███████████████████████████
██████████████████████████  ███   ██        █████  █████  ████  ███      ███  ████  ██  ████  ██████████████████████████
 */
@file:Suppress("unused")

package dev.supachain.robot

import dev.supachain.Modifiable
import dev.supachain.Modifies
import dev.supachain.robot.provider.CommonRequest
import dev.supachain.robot.messenger.messaging.ErrorResponse
import dev.supachain.utilities.Debug
import dev.supachain.utilities.toJson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.tls.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.URI
import java.security.KeyStore
import javax.net.ssl.TrustManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Interface defining the core components of a network client.
 *
 * This interface provides the basic structure for a network client, including the configuration
 * and the underlying HTTP client used for making requests.
 *
 * @since 0.1.0-alpha

 */
interface NetworkClient {
    /**
     * Configuration object specifying client behavior.
     */
    val config: NetworkConfig

    /**
     * The underlying Ktor HttpClient instance used for network requests.
     */
    val http: HttpClient
}

/**
 * Ktor-based Client for Network Communication (HTTP and WebSocket)
 *
 * Provides a network client built on Ktor for HTTP requests and WebSocket interactions. It offers:
 *
 * - **Highly Configurable:** Customize the client with a `NetworkConfig` object.
 * - **Efficient Engine:** Uses Ktor's CIO engine for non-blocking communication.
 * - **Secure by Default:** Supports HTTPS/TLS connections with configurable certificates and ciphers.
 * - **Content Negotiation:** Handles JSON serialization/deserialization with pretty-printing.
 * - **WebSocket Support:** Manages WebSocket connections and real-time communication.
 * - **Logging:** Optional detailed logging for debugging and monitoring.
 *
 * ## Example Usage (HTTP)
 *
 * ```kotlin
 * val config = NetworkConfig(
 *     headers = mapOf("Authorization" to "Bearer your_token"),
 *     isLogged = true
 * )
 * val client = KTORClient(config)
 *
 * val response = client.http.get("https://example.com")
 * // Process the response...
 * ```
 *
 * ## Example Usage (WebSocket)
 *
 * ```kotlin
 * client.webSocketSession(URI("wss://echo.websocket.org")) {
 *      send("Hello, WebSocket!")
 *      for (frame in incoming) {
 *          println(frame.readText())
 *      }
 * }
 * ```
 *
 * @param config A `NetworkConfig` instance defining the client's behavior.
 * @property http The underlying `HttpClient` used for communication (lazily initialized).
 *
 * @since 0.1.0-alpha
 */
class KTORClient(override val config: NetworkConfig) : NetworkClient {
    /**
     * Lazily initialized HttpClient instance with configurations from ClientConfig,
     * supporting both HTTP and WebSocket communication.
     */
    override val http by lazy {
        HttpClient(CIO) { // Use the CIO engine

            defaultRequest {
                // Set up default headers
                config.headers.forEach { (key, value) -> header(key, value) }
            }

            engine {
                // Maximum number of concurrent connections the client can maintain across all hosts.
                maxConnectionsCount = config.maxConnectionsCount

                // Maximum duration (in milliseconds) for a request to be fully processed (sent and received).
                requestTimeout = config.requestTimeout.inWholeMilliseconds

                // Enables pipelining, allowing multiple requests to be sent over a single connection without
                // waiting for responses to improve performance in specific scenarios.
                pipelining = config.pipelining

                // Optional proxy configuration for routing requests through an intermediary server.
                config.proxyConfig?.let { proxy = it }

                endpoint {
                    // Maximum number of connections allowed per unique host and port combination.
                    maxConnectionsPerRoute = config.maxConnectionsPerRoute

                    // Maximum number of pending requests allowed within the connection's pipeline before new
                    // requests start waiting. This helps manage concurrent requests and prevent resource exhaustion.
                    pipelineMaxSize = config.pipelineMaxSize

                    // Time (in milliseconds) a connection is kept alive after inactivity for potential reuse.
                    keepAliveTime = config.keepAliveTime.inWholeMilliseconds

                    // Number of attempts to establish a connection to a server before declaring failure.
                    connectAttempts = config.connectAttempts

                    // Maximum inactivity duration (in milliseconds) allowed between data packets within a connection.
                    socketTimeout = config.socketTimeout.inWholeMilliseconds

                    // Maximum duration (in milliseconds) the client waits for a connection to be established.
                    connectTimeout = config.connectTimeout.inWholeMilliseconds
                }

                // Configures HTTPS settings if provided.
                config.httpsConfig?.let { build ->
                    val tlsConfig = TLSConfiguration().apply(build)
                    https {
                        serverName = tlsConfig.serverName
                        cipherSuites = tlsConfig.cipherSuites
                        random = tlsConfig.random
                        trustManager = tlsConfig.trustManager
                        tlsConfig.storePasswordAliasList.forEach { (store, password, alias) ->
                            addKeyStore(store, password, alias)
                        }
                    }
                }
            }

            // Installs content negotiation with JSON support for pretty-printed output.
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }

            // Install WebSocket plugin to enable WebSocket support
            install(WebSockets)

            // Enables detailed request and response logging if configured.
            if (config.isLogged) install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
        }
    }

    /**
     * Establishes a WebSocket connection to the specified URI.
     *
     * This function initiates a WebSocket connection and launches a coroutine to
     * handle incoming WebSocket frames. You can customize the handling of WebSocket
     * events within the coroutine.
     *
     * @param uri The URI to connect to (e.g., "wss://your-websocket-server").
     */
    suspend fun webSocketSession(uri: URI, block: suspend DefaultClientWebSocketSession.() -> Unit) {
        http.webSocket(
            method = HttpMethod.Get,
            host = uri.host,
            port = uri.port,
            path = uri.path
        ) {
            block()
        }
    }
}

/**
 * Type alias for representing a network proxy configuration using Java's standard `java.net.Proxy`.
 *
 * A Proxy instance in Java allows you to specify the proxy type (e.g., HTTP, SOCKS) and the address and port of the
 * proxy server. This is used when the Ktor client needs to route its requests through an intermediary server instead
 * of connecting directly to the target host.
 *
 * @since 0.1.0-alpha
 */
typealias ProxyConfig = java.net.Proxy

/**
 * Configuration class for securing HTTPS (TLS) connections in a Ktor client.
 *
 * This class provides a convenient way to customize various aspects of the Transport Layer Security (TLS) handshake,
 * including server identification, cipher suites, trust management, and keystore handling.
 *
 * @property serverName The expected server name during TLS negotiation (Server Name Indication - SNI).
 * @property cipherSuites List of preferred cipher suites for encryption.
 * @property random Secure random number generator for cryptographic operations.
 * @property trustManager Custom trust manager to validate server certificates.
 *
 * @since 0.1.0-alpha

 */
class TLSConfiguration(
    val serverName: String? = null,
    val cipherSuites: List<CipherSuite> = listOf(),
    val random: java.security.SecureRandom? = null,
    var trustManager: TrustManager? = null,
) {
    /**
     * Internal list to store keystore, password, and alias combinations for later use in TLS configuration.
     * This allows you to add multiple keystores (e.g., containing client certificates or trusted root certificates) in
     * a convenient way.
     */
    internal val storePasswordAliasList = mutableListOf<Triple<KeyStore, CharArray?, String?>>()

    /**
     * Adds a keystore, its password, and an optional alias to the internal list.
     *
     * This function is designed to produce data that will be used within a `https { ... }` block in Ktor's
     * CIO engine configuration.
     *
     * @param store The KeyStore instance containing certificates or keys.
     * @param password Optional password to access the keystore (as a CharArray for security).
     * @param alias Optional alias to identify a specific entry within the keystore.
     */
    fun addKeyStore(store: KeyStore, password: CharArray?, alias: String? = null) {
        storePasswordAliasList.add(Triple(store, password, alias))
    }
}

/**
 * Data class holding client configuration parameters.
 *
 * @param isLogged Flag to enable/disable logging of requests and responses.
 * @param maxConnectionsPerRoute Maximum connections per route (host:port).
 * @param maxConnectionsCount Maximum total connections.
 * @param pipelineMaxSize Maximum requests in a connection's pipeline.
 * @param keepAliveTime Duration to keep connections alive after inactivity.
 * @param connectAttempts Number of connection retries before failure.
 * @param connectTimeout Maximum duration for establishing a connection.
 * @param requestTimeout Maximum duration for processing a request.
 * @param socketTimeout Maximum inactivity duration between data packets.
 * @param pipelining Flag to enable/disable HTTP pipelining.
 * @param proxyConfig Optional configuration for using a proxy server.
 * @param httpsConfig Optional configuration for HTTPS connections.
 *
 * @since 0.1.0-alpha

 */
data class NetworkConfig(
    var isLogged: Boolean = false,
    var maxConnectionsPerRoute: Int = 100,
    var maxConnectionsCount: Int = 100,
    var pipelineMaxSize: Int = 10,
    var keepAliveTime: Duration = 60.seconds,
    var connectAttempts: Int = 5,
    var connectTimeout: Duration = 60.seconds,
    var requestTimeout: Duration = 60.seconds,
    var socketTimeout: Duration = 60.seconds,
    var pipelining: Boolean = false,
    var proxyConfig: ProxyConfig? = null,
    var httpsConfig: ((TLSConfiguration).() -> Unit)? = null,
    val streamable: Boolean = false
) : Modifies<NetworkConfig> {
    /**
     * Sets all timeout durations to the specified value.
     *
     * @param time The duration to set for all timeouts.
     */
    fun setAllTimeouts(time: Duration) {
        requestTimeout = time
        connectTimeout = time
        socketTimeout = time
    }

    internal val headers = mutableListOf<Pair<String, Any?>>()

    // Appends a single header of key with a specific value if the value is not null.
    internal fun addHeader(key: String, value: Any?) {
        headers.add(key to value)
    }

    override val self = { this }

    companion object : Modifiable<NetworkConfig>({ NetworkConfig() })
}

/**
 * Configures an HTTP request builder for a JSON payload.
 *
 * This inline function is a convenient way to set up an HTTP request to send a JSON-encoded body.
 * It's used within a Ktor HTTP client request builder (e.g., `post`, `put`).
 *
 * @param T The type of the request object to be serialized as JSON.
 * @param request The request object to serialize and send as the request body.
 * @return A lambda function that configures an HttpRequestBuilder to:
 *   - Set the Content-Type header to `application/json`.
 *   - Serialize the `request` object into JSON and set it as the request body.
 *   - Prints log message to output of the Json request
 *
 * @since 0.1.0-alpha

 */
private inline fun <reified T> jsonRequest(request: T): HttpRequestBuilder.() -> Unit = {
    contentType(ContentType.Application.Json)
    setBody(request)
}

private val logger: org.slf4j.Logger get() = LoggerFactory.getLogger(NetworkOwner::class.java)

internal interface NetworkOwner {
    val url: String
    val networkClient: NetworkClient
}

/**
 * Makes a POST request to the specified URL with a JSON payload.
 *
 * This function is an extension on `NetworkOwner` (which presumably has a `networkClient` property) and simplifies
 * the process of making a POST request with a JSON-serialized request body.
 *
 * @param T The type of the request object to be serialized as JSON.
 * @param url The URL to make the POST request to.
 * @param request The request object to be serialized and sent in the request body.
 * @return An `HttpResponse` object representing the server's response.
 *
 * @since 0.1.0-alpha

 */
internal suspend inline fun<reified T: CommonRequest, reified R> NetworkOwner.post(url: String, request: T): R =
    networkClient.http
        .post(url, jsonRequest(request.apply { logger.debug(Debug("Network"), "[Request/Network]\nPost.Request[$url] ${toJson()}") }))
        .formatAndCheckResponse()

// Error handling function
internal suspend inline
fun <reified T> HttpResponse.formatAndCheckResponse(): T {
    var capturedString: String = ""
    val json = Json {
        ignoreUnknownKeys = true
    }
    return try {
        capturedString = bodyAsText()
        json.decodeFromString(capturedString)
    } catch (e: RedirectResponseException) {
        // Handle redirect (3xx status codes)
        throw Exception("Unexpected redirect: ${e.response.status.description}", e)
    } catch (e: ClientRequestException) {
        // Handle client errors (4xx status codes)
        val errorResponse = try {
            e.response.body<ErrorResponse>()
        } catch (_: Exception) {
            null
        }
        val errorMessage = errorResponse?.error?.message ?: "Client error: ${e.response.status.description}"
        throw Exception(errorMessage, e)
    } catch (e: ServerResponseException) {
        // Handle server errors (5xx status codes)
        throw Exception("Server error: ${e.response.status.description}", e)
    } catch (e: Exception) {
        // Handle other exceptions (e.g., network errors, serialization issues)
        logger.error("Unexpected error formatting common response. Provider message:\n " +
                "$capturedString. \n\nError -> $e")

        throw e
    }
}

