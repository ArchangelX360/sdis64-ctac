package fr.sdis64.brain.mail

import fr.sdis64.brain.utilities.AbstractScheduledFetcherService
import fr.sdis64.brain.utilities.FetcherScheduler
import fr.sdis64.brain.utilities.InvalidatingFetcher
import fr.sdis64.brain.utilities.browser.ChromeBrowserProvider
import fr.sdis64.brain.utilities.browser.pressEnterKeyOn
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.delay
import org.hildan.chrome.devtools.domains.dom.setAttributeValue
import org.hildan.chrome.devtools.domains.runtime.RuntimeDomain
import org.hildan.chrome.devtools.domains.runtime.evaluateJs
import org.hildan.chrome.devtools.targets.ChromePageSession
import org.hildan.chrome.devtools.targets.attachToNewPageAndAwaitPageLoad
import org.hildan.chrome.devtools.targets.use
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ConfigurationProperties(prefix = "ctac.webmail")
class WebmailConfiguration(
    val url: String,
    val username: String,
    val password: String,
)

@Service
class WebmailService(
    @Autowired private val chromeBrowserProvider: ChromeBrowserProvider,
    @Autowired private val webmailConfiguration: WebmailConfiguration,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    private val webmail = FetcherScheduler(
        Webmail(
            chromeBrowserProvider,
            webmailConfiguration,
        ),
        5.minutes,
        registry,
        4.seconds,
    )

    init {
        webmail.startIn(scheduledFetcherScope)
    }

    suspend fun getUnarchivedMails(): Set<String> = webmail.getValue()
}

class Webmail(
    private val browserProvider: ChromeBrowserProvider,
    private val configuration: WebmailConfiguration,
) : InvalidatingFetcher<Set<String>> {
    override val name = "webmail"

    override suspend fun fetch(): Set<String> = browserProvider.browserSession().use { browserSession ->
        browserSession
            .attachToNewPageAndAwaitPageLoad(url = configuration.url)
            .use { pageSession ->
                pageSession.login()
                pageSession.runtime.scrapeUnreadMailSubjects()
            }
    }

    override suspend fun onError(): Set<String> = emptySet()

    private suspend fun RuntimeDomain.scrapeUnreadMailSubjects(): Set<String> {
        val scrapeUnreadSubjects = """
        function sanitize(s) {
            return s.replace(/^Sujet\s/g, '').replace(/^Subject\s/g, '').replace(/,${'$'}/, '')
        }
        
        function getUnreadSubjects() {
            const unreadParents = Array.from(document.querySelectorAll("[id${'$'}='.uread']") || [], e => e.parentElement)
            const elements = unreadParents.reduce((subjects, p) => {
                const subjectElement = p.querySelector("[id${'$'}='.subject']")
                if (!subjectElement || !subjectElement.innerHTML) {
                    return subjects
                }
                return [...subjects, sanitize(subjectElement.innerHTML)]
            }, []);
            return elements
        }
        getUnreadSubjects();
    """.trimIndent()

        return evaluateJs<Set<String>>(scrapeUnreadSubjects) ?: emptySet()
    }

    private suspend fun ChromePageSession.login() {
        dom.setAttributeValue(nodeSelector = "#username", name = "value", value = configuration.username)
        dom.setAttributeValue(nodeSelector = "#password", name = "value", value = configuration.password)
        pressEnterKeyOn("#password") // sends the form
        delay(3.seconds)
    }
}
