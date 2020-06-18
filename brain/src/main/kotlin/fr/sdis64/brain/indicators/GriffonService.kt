package fr.sdis64.brain.indicators

import fr.sdis64.brain.utilities.AbstractScheduledFetcherService
import fr.sdis64.brain.utilities.FetcherScheduler
import fr.sdis64.brain.utilities.InvalidatingFetcher
import fr.sdis64.brain.utilities.browser.BrowserConfiguration
import fr.sdis64.brain.utilities.browser.ChromeBrowserProvider
import fr.sdis64.brain.utilities.browser.useWithUserAgent
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.delay
import org.hildan.chrome.devtools.domains.runtime.evaluateJs
import org.hildan.chrome.devtools.targets.ChromePageSession
import org.hildan.chrome.devtools.targets.attachToNewPage
import org.hildan.chrome.devtools.targets.navigateAndAwaitPageLoad
import org.hildan.chrome.devtools.targets.use
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ConfigurationProperties(prefix = "ctac.griffon")
data class GriffonConfiguration(
    val loginUrl: String,
    val indicatorUrl: String,
    val departement: String,
    val username: String,
    val password: String,
)

@Service
class GriffonService(
    @Autowired private val chromeBrowserProvider: ChromeBrowserProvider,
    @Autowired private val browserConfiguration: BrowserConfiguration,
    @Autowired private val griffonConfiguration: GriffonConfiguration,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    private val griffon = FetcherScheduler(
        Griffon(
            chromeBrowserProvider,
            griffonConfiguration,
            browserConfiguration,
        ),
        10.minutes,
        registry,
        5.seconds,
    )

    init {
        griffon.startIn(scheduledFetcherScope)
    }

    suspend fun getGriffonIndicator() = griffon.getValue()
}

private class Griffon(
    private val browserProvider: ChromeBrowserProvider,
    private val configuration: GriffonConfiguration,
    private val browserConfiguration: BrowserConfiguration,
) : InvalidatingFetcher<GriffonIndicator> {
    override val name = "griffon"

    override suspend fun onError() = GriffonIndicator.create(null)

    override suspend fun fetch() = browserProvider.browserSession().use { browserSession ->
        browserSession
            .attachToNewPage("about:blank")
            .useWithUserAgent(browserConfiguration.userAgent) { pageSession ->
                pageSession.login()
                pageSession.scrapeCurrentLevel()
            }
    }

    private suspend fun ChromePageSession.login() {
        navigateAndAwaitPageLoad(url = configuration.loginUrl)

        val login = """
            function login() {
                document.querySelector("[name='email']").value = "${configuration.username}";
                document.querySelector("[name='password']").value = "${configuration.password}";
                document.querySelector("[type='submit']").click()
            }
            login();
        """.trimIndent()

        runtime.evaluateJs<Unit>(login)
        delay(5.seconds)

        // CartoGrip as an auth token embedded in the <a> link, the easiest way is just to click the link
        // instead of extracting this token
        val navigate = """
            function loginToCartoGrip() {
                Array.from(document.querySelectorAll('a'))
                  .filter(a => a.innerText === " GRIFFON")[0]
                  ?.click()

            }
            loginToCartoGrip();
        """.trimIndent()
        runtime.evaluateJs<Unit>(navigate)
        delay(5.seconds)
    }

    private suspend fun ChromePageSession.scrapeCurrentLevel(): GriffonIndicator {
        navigateAndAwaitPageLoad(configuration.indicatorUrl)

        val scrapCell = """
            function scrapCell() {
                const cell = document.querySelector("#num_niveau_risque_${configuration.departement} [selected]");
                return cell ? cell.innerText : ""
            }
            scrapCell();
        """.trimIndent()

        val raw = runtime.evaluateJs<String>(scrapCell)
        return GriffonIndicator.create(raw?.trim())
    }
}
