package fr.sdis64.backend.maps

import fr.sdis64.backend.maps.utilities.StorageConfiguration
import fr.sdis64.backend.maps.utilities.savePlaceholderImage
import fr.sdis64.backend.utilities.AbstractScheduledFetcherService
import fr.sdis64.backend.utilities.FetcherScheduler
import fr.sdis64.backend.utilities.InvalidatingFetcher
import fr.sdis64.backend.utilities.browser.BrowserConfiguration
import fr.sdis64.backend.utilities.browser.ChromeBrowserProvider
import fr.sdis64.backend.utilities.browser.useWithUserAgent
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.delay
import org.hildan.chrome.devtools.domains.dom.findNodeBySelector
import org.hildan.chrome.devtools.domains.page.ReloadRequest
import org.hildan.chrome.devtools.domains.page.ScreenshotFormat
import org.hildan.chrome.devtools.domains.page.Viewport
import org.hildan.chrome.devtools.domains.page.captureScreenshotToFile
import org.hildan.chrome.devtools.domains.runtime.evaluateJs
import org.hildan.chrome.devtools.targets.ChromePageSession
import org.hildan.chrome.devtools.targets.attachToNewPage
import org.hildan.chrome.devtools.targets.navigateAndAwaitPageLoad
import org.hildan.chrome.devtools.targets.use
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ConfigurationProperties(prefix = "ctac.maps.ventusky")
data class VentuskyMapsConfiguration(
    val wind: Map,
    val rain: Map,
) {
    data class Map(
        val url: String,
        val snapshotBoundaries: Viewport,
        private val filename: String,
    ) {
        fun filepath(storageConfiguration: StorageConfiguration) = storageConfiguration.pathToMapFile(filename)
    }
}

@Component
class VentuskyRoutine(
    @Autowired private val chromeBrowserProvider: ChromeBrowserProvider,
    @Autowired private val browserConfiguration: BrowserConfiguration,
    @Autowired private val storageConfiguration: StorageConfiguration,
    @Autowired private val ventuskyMapsConfiguration: VentuskyMapsConfiguration,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    init {
        val rainScheduler = FetcherScheduler(
            VentuskyRain(
                chromeBrowserProvider,
                ventuskyMapsConfiguration,
                browserConfiguration,
                storageConfiguration,
            ),
            10.minutes,
            registry,
            2.seconds,
        )
        rainScheduler.startIn(scheduledFetcherScope)

        val windScheduler = FetcherScheduler(
            VentuskyWind(
                chromeBrowserProvider,
                ventuskyMapsConfiguration,
                browserConfiguration,
                storageConfiguration,
            ),
            10.minutes,
            registry,
            2.seconds,
        )
        windScheduler.startIn(scheduledFetcherScope)
    }
}

private class VentuskyRain(
    browserProvider: ChromeBrowserProvider,
    configuration: VentuskyMapsConfiguration,
    browserConfiguration: BrowserConfiguration,
    storageConfiguration: StorageConfiguration,
) : Ventusky(
    browserProvider,
    configuration.rain,
    browserConfiguration,
    storageConfiguration,
) {
    override val name = "maps_ventusky_rain"
}

private class VentuskyWind(
    browserProvider: ChromeBrowserProvider,
    configuration: VentuskyMapsConfiguration,
    browserConfiguration: BrowserConfiguration,
    storageConfiguration: StorageConfiguration,
) : Ventusky(
    browserProvider,
    configuration.wind,
    browserConfiguration,
    storageConfiguration,
) {
    override val name = "maps_ventusky_wind"
}

private abstract class Ventusky(
    private val browserProvider: ChromeBrowserProvider,
    private val map: VentuskyMapsConfiguration.Map,
    private val browserConfiguration: BrowserConfiguration,
    private val storageConfiguration: StorageConfiguration,
) : InvalidatingFetcher<Unit> {

    override suspend fun fetch() = browserProvider.browserSession().use { browserSession ->
        browserSession
            .attachToNewPage(
                url = "about:blank",
                width = browserConfiguration.viewport.width,
                height = browserConfiguration.viewport.height,
            )
            .useWithUserAgent(browserConfiguration.userAgent) { pageSession ->
                pageSession.navigateAndAwaitPageLoad(url = map.url)
                pageSession.runtime.evaluateJs<Unit>("window.localStorage.setItem('grid', 1)")
                pageSession.page.reload(ReloadRequest())
                delay(25.seconds)
                pageSession.hideNewsNode()
                pageSession.page.captureScreenshotToFile(outputImagePath = map.filepath(storageConfiguration)) {
                    clip = map.snapshotBoundaries
                    format = ScreenshotFormat.webp
                }
            }
    }

    private suspend fun ChromePageSession.hideNewsNode() {
        val newsNode = dom.findNodeBySelector(selector = "#news")
        if (newsNode != null) {
            dom.setAttributeValue(
                nodeId = newsNode,
                name = "style",
                value = "display: none;",
            )
        }
    }

    override suspend fun onError() = savePlaceholderImage(
        filepath = map.filepath(storageConfiguration),
        width = map.snapshotBoundaries.width.toInt(),
        height = map.snapshotBoundaries.height.toInt(),
    )
}
