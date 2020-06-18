package fr.sdis64.brain.vehicles

import fr.sdis64.api.vehicles.HelicopterPosition
import fr.sdis64.brain.utilities.AbstractScheduledFetcherService
import fr.sdis64.brain.utilities.FetcherScheduler
import fr.sdis64.brain.utilities.InvalidatingFetcher
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ConfigurationProperties(prefix = "ctac.dragon")
data class DragonConfiguration(
    val baseUrl: String,
    val username: String,
    val password: String,

    private val loginPage: String,

    private val devicesEndpoint: String,
    private val devicesEndpointSubstitutionPart: String,

    val deviceId: String,
) {
    val loginUrl: String
        get() = "$baseUrl$loginPage"

    fun devicesUrl(consoleId: String): String =
        "$baseUrl$devicesEndpoint".replace(devicesEndpointSubstitutionPart, consoleId)
}

@Service
class DragonService(
    @Autowired private val dragonConfiguration: DragonConfiguration,
    @Autowired private val httpClient: HttpClient,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    private val dragon = FetcherScheduler(
        Dragon(
            httpClient,
            dragonConfiguration,
        ),
        10.minutes,
        registry,
        10.seconds,
    )

    init {
        dragon.startIn(scheduledFetcherScope)
    }

    suspend fun getHelicopterPosition(): HelicopterPosition = dragon.getValue()
}

private class Dragon(
    private val httpClient: HttpClient,
    private val configuration: DragonConfiguration,
) : InvalidatingFetcher<HelicopterPosition> {
    override val name = "dragon"

    override suspend fun fetch(): HelicopterPosition {
        val dragonAuth = login()
        val devices = getDevices(dragonAuth)
        return devices
            .find { d -> d.id == configuration.deviceId }
            ?.toHelicopterPosition()
            ?: HelicopterPosition.Unknown
    }

    override suspend fun onError(): HelicopterPosition = HelicopterPosition.Unknown

    private suspend fun login(): DragonAuth {
        val loginResponse = httpClient.request(configuration.loginUrl) {
            expectSuccess = false

            method = HttpMethod.Post
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=${configuration.username}&password=${configuration.password}")
        }

        val responseBody = loginResponse.bodyAsText()

        if (!loginResponse.status.isSuccess()) {
            if (loginResponse.status == HttpStatusCode.Forbidden) {
                val backpressureDelay = 20.minutes // TODO: times out before as `InvalidatingFetcher` has a timeout logic
                LOG.warn("access forbidden, will retry in $backpressureDelay...")
                delay(backpressureDelay)
            }
            error("failed to authenticate (${loginResponse.status}) '$responseBody'")
        }

        val (token) = Regex("var token = \"([a-zA-Z0-9_\\-.]*)\"")
            .find(responseBody)
            ?.destructured ?: error("failed to extract token in '$responseBody'")

        val (consoleId) = Regex("/rest/consoles/([a-zA-Z0-9\\-]*)/")
            .find(responseBody)
            ?.destructured ?: error("failed to extract consoleId in '$responseBody'")

        return DragonAuth(
            token = token,
            consoleId = consoleId,
        )
    }

    private suspend fun getDevices(auth: DragonAuth): Set<Device> {
        val url = configuration.devicesUrl(auth.consoleId)
        return httpClient.get(url) {
            header("x-auth-token", auth.token)
        }.body()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Dragon::class.java)
    }
}

private data class DragonAuth(
    val token: String,
    val consoleId: String,
)

@Serializable
private data class Device(
    val id: String? = null,
    val alias: String? = null,
    val data: Data? = null,
) {
    @Serializable
    data class Data(
        val location: Location? = null,
    )

    @Serializable
    data class Location(
        val position: List<Float>? = null,
    )
}

private fun Device.toHelicopterPosition(): HelicopterPosition {
    if (id == null) {
        error("Device must have an 'id'")
    }
    if (alias == null) {
        error("Device must have an 'alias'")
    }
    if (data?.location?.position?.size != 2) {
        error("Device data must have exactly 2 coordinates")
    }

    return HelicopterPosition.Known(
        id = id,
        alias = alias,
        latitude = data.location.position[1],
        longitude = data.location.position[0],
    )
}
