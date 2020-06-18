package fr.sdis64.brain.indicators

import fr.sdis64.api.indicators.WeatherIndicator
import fr.sdis64.api.indicators.WeatherIndicatorColor
import fr.sdis64.api.indicators.WeatherIndicatorType
import fr.sdis64.brain.utilities.AbstractScheduledFetcherService
import fr.sdis64.brain.utilities.FetcherScheduler
import fr.sdis64.brain.utilities.InvalidatingFetcher
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.Serializable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@ConfigurationProperties(prefix = "ctac.weather")
data class WeatherConfiguration(
    val tokenUrl: String,
    val indicatorsUrl: String,
    val department: String,
)

@Service
class WeatherService(
    @Autowired private val weatherConfiguration: WeatherConfiguration,
    @Autowired private val httpClient: HttpClient,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    private val weather = FetcherScheduler(
        Weather(
            httpClient,
            weatherConfiguration,
        ),
        5.minutes,
        registry,
        Duration.ZERO,
    )

    init {
        weather.startIn(scheduledFetcherScope)
    }

    suspend fun getIndicators() = weather.getValue()
}

private class Weather(
    private val httpClient: HttpClient,
    private val configuration: WeatherConfiguration,
) : InvalidatingFetcher<Set<WeatherIndicator>> {
    override val name = "weather"

    override suspend fun fetch(): Set<WeatherIndicator> {
        val sessionToken = getSessionToken(configuration.tokenUrl)
        val token = generateAuthToken(sessionToken)
        val rawIndicators: RawWeatherIndicators = httpClient
            .get(configuration.indicatorsUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("domain", configuration.department)
            }.body()
        return rawIndicators.toWeatherIndicators()
    }

    override suspend fun onError(): Set<WeatherIndicator> = emptySet()

    /**
     * Reproduce the client side generation of authentication token from a Session token in the weather website
     *
     * The original code looks like such (JS):
     * ```
     *     sessionToken.replace(/[a-zA-Z]/g, function (e) {
     *         const t = e <= "Z" ? 65 : 97;
     *         return String.fromCharCode(t + (e.charCodeAt(0) - t + 13) % 26)
     *     }).replace('\n', '')
     * ```
     */
    private fun generateAuthToken(sessionToken: String): String {
        return sessionToken
            .map { c ->
                if (c.isLetter()) {
                    val t = if (c <= 'Z') 65 else 97
                    Char(t + (c.code - t + 13) % 26)
                } else {
                    c
                }
            }
            .joinToString("")
            .replace("\n", "")
    }

    private suspend fun getSessionToken(url: String): String {
        val response = httpClient.get(url)
        val cookies = response.headers.getAll(HttpHeaders.SetCookie)
        return cookies
            ?.flatMap {
                it.split(";")
            }?.firstOrNull {
                it.startsWith("mfsession=")
            }
            ?.replace("mfsession=", "") ?: error("Session cookie not found")
    }
}

@Serializable
private data class PhenomenonItem(
    val phenomenon_id: Int,
    val phenomenon_max_color_id: Int,
)

@Serializable
private class RawWeatherIndicators(
    private val phenomenons_items: List<PhenomenonItem>,
) {
    fun toWeatherIndicators(): Set<WeatherIndicator> {
        return phenomenons_items
            .mapNotNull {
                WeatherIndicator.create(it.phenomenon_id, it.phenomenon_max_color_id)
            }.toSet()
    }
}

private fun WeatherIndicator.Companion.create(typeId: Int, colorId: Int): WeatherIndicator? {
    val color = colorsById[colorId] ?: return null
    val typeLabel = typesById[typeId] ?: return null
    return WeatherIndicator(
        level = WeatherIndicatorColor(color, colorId),
        type = WeatherIndicatorType(typeLabel, typeId),
    )
}

private val colorsById = mapOf(
    1 to "green",
    2 to "yellow",
    3 to "orange",
    4 to "red",
)

private val typesById = mapOf(
    1 to "Vent violent",
    2 to "Pluie - Inondation",
    3 to "Orages",
    4 to "Crues",
    5 to "Neige - Verglas",
    6 to "Canicule",
    7 to "Grand froid",
    8 to "Avalanche",
    9 to "Vagues - Submersion",
)
