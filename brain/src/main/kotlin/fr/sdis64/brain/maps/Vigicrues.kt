package fr.sdis64.brain.maps

import fr.sdis64.brain.maps.utilities.StorageConfiguration
import fr.sdis64.brain.maps.utilities.savePlaceholderImage
import fr.sdis64.brain.utilities.AbstractScheduledFetcherService
import fr.sdis64.brain.utilities.FetcherScheduler
import fr.sdis64.brain.utilities.InvalidatingFetcher
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.jvm.javaio.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hildan.chrome.devtools.domains.page.Viewport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.seconds

@ConfigurationProperties(prefix = "ctac.maps.vigicrues")
data class VigicruesConfiguration(
    val url: String,
    val snapshotBoundaries: Viewport,

    private val filename: String,
) {
    fun filepath(storageConfiguration: StorageConfiguration) = storageConfiguration.pathToMapFile(filename)
}

@Component
class VigicruesRoutine(
    @Autowired private val storageConfiguration: StorageConfiguration,
    @Autowired private val vigicruesConfiguration: VigicruesConfiguration,
    @Autowired private val registry: MeterRegistry,
    @Autowired private val httpClient: HttpClient,
) : AbstractScheduledFetcherService() {
    init {
        val scheduler = FetcherScheduler(
            Vigicrues(
                httpClient,
                vigicruesConfiguration,
                storageConfiguration,
            ),
            30.seconds,
            registry,
            1.seconds,
        )
        scheduler.startIn(scheduledFetcherScope)
    }
}

private class Vigicrues(
    private val httpClient: HttpClient,
    private val configuration: VigicruesConfiguration,
    private val storageConfiguration: StorageConfiguration,
) : InvalidatingFetcher<Unit> {
    override val name = "maps_vigicrues"

    override suspend fun fetch() {
        httpClient.prepareGet(configuration.url).execute { httpResponse ->
            val channel: ByteReadChannel = httpResponse.body()
            withContext(Dispatchers.IO) {
                val dest = channel.toInputStream().use { imageInputStream ->
                    ImageIO.read(imageInputStream)
                        .getSubimage(
                            configuration.snapshotBoundaries.x.toInt(),
                            configuration.snapshotBoundaries.y.toInt(),
                            configuration.snapshotBoundaries.width.toInt(),
                            configuration.snapshotBoundaries.height.toInt(),
                        )
                }
                ImageIO.write(dest, "PNG", configuration.filepath(storageConfiguration).toFile())
            }
        }
    }

    override suspend fun onError() = savePlaceholderImage(
        filepath = configuration.filepath(storageConfiguration),
        width = configuration.snapshotBoundaries.width.toInt(),
        height = configuration.snapshotBoundaries.height.toInt(),
    )
}
