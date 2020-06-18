package fr.sdis64.brain.utilities

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Test
import java.net.ConnectException
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class PrometheusPluginTest {

    @Test
    fun success() = runBlocking {
        val sucessUrl = "http://success.com/code/200"

        val mockRegistry = SimpleMeterRegistry()
        val client = HttpClient(MockEngine) {
            install(PrometheusPlugin) {
                registry = mockRegistry
            }
            engine {
                addHandler { request ->
                    when (request.url.toString()) {
                        sucessUrl -> respondOk("ok")
                        else -> error("Unhandled ${request.url}")
                    }
                }
            }
        }

        client.get(sucessUrl)

        val search = mockRegistry.find("http_client_request_duration_seconds")
        val summary = search.summary() ?: fail("could not find HTTP summary")
        assertTrue(summary.count() == 1L)
    }

    @Test
    fun fail() = runBlocking {
        val mockRegistry = SimpleMeterRegistry()
        val client = HttpClient {
            install(PrometheusPlugin) {
                registry = mockRegistry
            }
        }

        assertFailsWith<ConnectException> {
            client.get("http://127.0.0.1/somethingwrong")
        }

        val search = mockRegistry.find("http_client_request_duration_seconds")
        val summary = search.summary() ?: fail("could not find HTTP summary")

        assertTrue(summary.count() == 1L)
    }


    @OptIn(ExperimentalTime::class)
    @Test
    fun timeout() = runBlocking {
        val mockRegistry = SimpleMeterRegistry()
        val client = HttpClient(MockEngine) {
            install(PrometheusPlugin) {
                registry = mockRegistry
            }
            engine {
                addHandler {
                    delay(1.seconds)
                    respondOk("ok")
                }
            }
        }

        // If the timeout happens within the machinery of the Ktor client, we can
        // get a wrongly wrapped CancellationException that is not caught by
        // withTimeoutOrNull (which expects TimeoutCancellationException).
        // 150ms is usually enough to get inside the handler.
        val v = withTimeoutOrNull(150.milliseconds) {
            client.get("some_url")
        }
        assertNull(v, "The call should timeout")

        val search = mockRegistry.find("http_client_request_duration_seconds")
        val summary = search.summary() ?: fail("could not find HTTP summary")

        assertTrue(summary.count() == 1L)
    }
}
