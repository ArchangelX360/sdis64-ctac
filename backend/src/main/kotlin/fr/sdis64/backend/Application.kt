package fr.sdis64.backend

import com.ulisesbocchio.jasyptspringboot.environment.StandardEncryptableEnvironment
import fr.sdis64.backend.systel.SystelClient
import fr.sdis64.backend.systel.SystelConfiguration
import fr.sdis64.backend.utilities.PrometheusPlugin
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.contrib.attach.RuntimeAttach
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters
import org.springframework.format.FormatterRegistry
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json as KxJson

fun main(args: Array<String>) {
    val otelEnv =
        args.flagValue("--spring.profiles.active") ?: "local${System.getProperty("user.name")?.let { "-$it" }}"
    OpenTelemetryHelper.enableOpenTelemetry(otelEnv)
    runApplication<Application>(*args) {
        setEnvironment(StandardEncryptableEnvironment()) // allow encryption to even be used in bootstrap phase (like `lockback-spring.xml` property resolving)
    }
}

private fun Array<String>.flagValue(flagName: String): String? =
    toList().windowed(2, partialWindows = true).firstOrNull { (key) ->
        key.startsWith(flagName)
    }?.let { window ->
        val key = window[0]
        key.split("=", limit = 2).getOrNull(1) // --flagName=value
            ?: window.getOrNull(1) // --flagName value
    }

object OpenTelemetryHelper {
    private val LOG = LoggerFactory.getLogger(OpenTelemetryHelper::class.java)

    /**
     * Enables OpenTelemetry automatic instrumentation
     */
    internal fun enableOpenTelemetry(environment: String) {
        // instead of having to use -javaagent, we do it in the code
        LOG.info("Enabling OpenTelemetry instrumentation")
        mapOf(
            "otel.service.name" to "backend",
            "otel.resource.attributes" to listOf(
                "deployment.environment" to environment,
                "service.namespace" to "ctac",
                "service.version" to "TBD",
                "service.instance.id" to "$environment-1",
            ).joinToString(",") { (key, value) -> "$key=$value" },
            "otel.traces.exporter" to "otlp",
            "otel.metrics.exporter" to "otlp",
            "otel.logs.exporter" to "otlp",
            "otel.semconv-stability.opt-in" to "http",
            "otel.metric.export.interval" to "15000",
            "otel.instrumentation.log4j-appender.experimental-log-attributes" to "true",
            "otel.instrumentation.logback-appender.experimental-log-attributes" to "true",
            "otel.instrumentation.spring-webmvc.experimental-span-attributes" to "true",
            "otel.exporter.otlp.protocol" to "http/protobuf",
            // `OTEL_EXPORTER_OTLP_ENDPOINT` is set by `build.gradle.kts` or `*.service.yml` files
            "otel.exporter.otlp.endpoint" to System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT"),
            // `OTEL_EXPORTER_OTLP_HEADERS` is set by `build.gradle.kts` or `*.service.yml` files
            "otel.exporter.otlp.headers" to System.getenv("OTEL_EXPORTER_OTLP_HEADERS"),
        ).forEach { (key, value) ->
            LOG.info(" -> $key=${value.replaceAfter("Basic ", "***")}")
            System.setProperty(key, value)
        }
        RuntimeAttach.attachJavaagentToCurrentJvm()
    }
}

@SpringBootApplication
@EntityScan(basePackageClasses = [Application::class, Jsr310JpaConverters::class])
@EnableScheduling
@ConfigurationPropertiesScan
class Application {
    private val defaultHttpTimeout = 5.seconds

    @Bean
    fun systelClient(
        @Autowired httpClient: HttpClient,
        @Autowired systelConfiguration: SystelConfiguration,
    ): SystelClient = SystelClient(httpClient, systelConfiguration)

    @Bean
    fun httpClient(
        @Autowired registry: MeterRegistry,
    ) = HttpClient(Java) {
        expectSuccess = true

        engine {
            pipelining = true
        }
        install(HttpCookies)
        install(HttpTimeout) {
            socketTimeoutMillis = defaultHttpTimeout.inWholeMilliseconds * 2
            connectTimeoutMillis = defaultHttpTimeout.inWholeMilliseconds * 2
            requestTimeoutMillis = defaultHttpTimeout.inWholeMilliseconds
        }
        install(ContentNegotiation) {
            json(KxJson {
                ignoreUnknownKeys = true
            })
        }
        install(PrometheusPlugin) {
            this.registry = registry
            this.urlParametersToRedact = listOf("token")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Bean
    fun httpMessageConverter(): KotlinSerializationJsonHttpMessageConverter {
        return KotlinSerializationJsonHttpMessageConverter(KxJson {
            encodeDefaults = true
            explicitNulls = false
        })
    }

    @Configuration
    class WebConfig : WebMvcConfigurer {
        class StringToInstantConverter : Converter<String, Instant> {
            override fun convert(source: String): Instant {
                return Instant.parse(source)
            }
        }

        override fun addFormatters(registry: FormatterRegistry) {
            registry.addConverter(StringToInstantConverter())
        }
    }
}
