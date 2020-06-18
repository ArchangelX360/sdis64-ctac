package fr.sdis64.brain

import com.ulisesbocchio.jasyptspringboot.environment.StandardEncryptableEnvironment
import fr.sdis64.brain.systel.SystelClient
import fr.sdis64.brain.systel.SystelConfiguration
import fr.sdis64.brain.utilities.PrometheusPlugin
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
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
    runApplication<BrainApplication>(*args) {
        setEnvironment(StandardEncryptableEnvironment()) // allow encryption to even be used in bootstrap phase (like `lockback-spring.xml` property resolving)
    }
}

@SpringBootApplication
@EntityScan(basePackageClasses = [BrainApplication::class, Jsr310JpaConverters::class])
@EnableScheduling
@ConfigurationPropertiesScan
class BrainApplication {
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
