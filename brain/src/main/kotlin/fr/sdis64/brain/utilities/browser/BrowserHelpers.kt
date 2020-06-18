package fr.sdis64.brain.utilities.browser

import kotlinx.coroutines.delay
import org.hildan.chrome.devtools.domains.dom.CssSelector
import org.hildan.chrome.devtools.domains.dom.DOMDomain
import org.hildan.chrome.devtools.domains.dom.findNodeBySelector
import org.hildan.chrome.devtools.domains.dom.focusNodeBySelector
import org.hildan.chrome.devtools.domains.input.DispatchKeyEventRequest
import org.hildan.chrome.devtools.domains.input.KeyEventType
import org.hildan.chrome.devtools.domains.network.SetUserAgentOverrideRequest
import org.hildan.chrome.devtools.protocol.ChromeDPClient
import org.hildan.chrome.devtools.protocol.ExperimentalChromeApi
import org.hildan.chrome.devtools.targets.ChromeBrowserSession
import org.hildan.chrome.devtools.targets.ChromePageSession
import org.hildan.chrome.devtools.targets.use
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ChromeBrowserProviderException(e: Exception) : Exception(e)

@Component
class ChromeBrowserProvider(
    @Autowired private val browserConfiguration: BrowserConfiguration,
) {
    suspend fun browserSession(remoteDebugUrl: String = browserConfiguration.remoteDebugUrl): ChromeBrowserSession =
        createBrowserSession(remoteDebugUrl)

    private suspend fun createBrowserSession(remoteDebugUrl: String): ChromeBrowserSession {
        try {
            val client = ChromeDPClient(remoteDebugUrl)
            return client.webSocket()
        } catch (e: Exception) {
            LOG.error("failed to connect to Chrome browser at $remoteDebugUrl: ${e.message}")
            throw ChromeBrowserProviderException(e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ChromeBrowserProvider::class.java)
    }
}

suspend fun ChromePageSession.pressKeyOn(selector: String, key: String) {
    dom.focusNodeBySelector(selector)
    input.dispatchKeyEvent(DispatchKeyEventRequest(KeyEventType.keyDown, text = key))
}

suspend fun ChromePageSession.pressEnterKeyOn(selector: String) {
    val enterKey = "\r"
    pressKeyOn(selector, enterKey)
}

@OptIn(ExperimentalChromeApi::class)
suspend inline fun <R> ChromePageSession.useWithUserAgent(userAgent: String, block: (ChromePageSession) -> R): R = use {
    it.network.setUserAgentOverride(SetUserAgentOverrideRequest(userAgent = userAgent))
    block(this)
}

/**
 * Waits until the given [selector] matches no node in the DOM. Inspects the DOM every [pollingPeriod].
 *
 * This method may suspend forever if the [selector] keeps matching at least one node.
 * The caller is responsible for using [withTimeout][kotlinx.coroutines.withTimeout] or similar cancellation mechanisms
 * around calls to this method if handling this case is necessary.
 */
suspend fun DOMDomain.awaitNodeAbsentBySelector(selector: CssSelector, pollingPeriod: Duration = 200.milliseconds) {
    // FIXME: replaces by https://github.com/joffrey-bion/chrome-devtools-kotlin implementation when released
    while (true) {
        // it looks like we do need to get a new document at each poll otherwise we may not see the new nodes
        findNodeBySelector(selector) ?: return
        delay(pollingPeriod)
    }
}
