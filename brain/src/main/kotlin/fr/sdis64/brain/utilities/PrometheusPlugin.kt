package fr.sdis64.brain.utilities

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.util.concurrent.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class PrometheusPlugin(configuration: Configuration) {
    private val registry = configuration.registry
    private val urlParametersToRedact = configuration.urlParametersToRedact

    class Configuration {
        var registry: MeterRegistry = SimpleMeterRegistry()
        var urlParametersToRedact = emptyList<String>()
    }

    companion object Plugin : HttpClientPlugin<Configuration, PrometheusPlugin> {
        override val key: AttributeKey<PrometheusPlugin> = AttributeKey("Prometheus")

        override fun prepare(block: Configuration.() -> Unit): PrometheusPlugin =
            PrometheusPlugin(Configuration().apply(block))

        override fun install(plugin: PrometheusPlugin, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
                val startTime = System.nanoTime()
                try {
                    proceed()
                } catch (e: Exception) {
                    val failTime = System.nanoTime()
                    val requestDuration = (failTime - startTime).nanoseconds
                    val urlCopy = URLBuilder(context.url).build() // `build()` on URLBuilder is not immutable
                    plugin.registry
                        .summary(
                            "http_client_request_duration_seconds", mutableListOf(
                                ImmutableTag("method", context.method.value),
                                ImmutableTag("host", urlCopy.hostWithPort),
                                ImmutableTag("path", urlCopy.redactedFullPath(plugin.urlParametersToRedact)),
                                ImmutableTag("status", if (e is CancellationException) "CANCELLED" else "CLIENT_ERROR"),
                                ImmutableTag("error", "${e::class.simpleName}"),
                            )
                        )
                        .record(requestDuration.toDouble(DurationUnit.SECONDS))
                    throw e
                }
            }

            scope.receivePipeline.intercept(HttpReceivePipeline.Before) {
                plugin.registry
                    .summary(
                        "http_client_request_duration_seconds", mutableListOf(
                            ImmutableTag("method", it.request.method.value),
                            ImmutableTag("host", it.request.url.hostWithPort),
                            ImmutableTag("path", it.request.url.redactedFullPath(plugin.urlParametersToRedact)),
                            ImmutableTag("status", it.status.value.toString()),
                            ImmutableTag("error", ""),
                        )
                    )
                    .record(it.requestDuration.toDouble(DurationUnit.SECONDS))
            }
        }

        private fun Url.redactedFullPath(urlParametersToRedact: List<String>): String {
            val copyBuilder = URLBuilder(this)
            urlParametersToRedact.forEach {
                if (copyBuilder.parameters.contains(it)) {
                    copyBuilder.parameters.remove(it)
                    copyBuilder.parameters.append(it, "REDACTED")
                }
            }
            return copyBuilder.build().fullPath
        }
    }
}

private val HttpResponse.requestDuration
    get() = (responseTime.timestamp - requestTime.timestamp).milliseconds
