package fr.sdis64.backend.utilities.browser

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ctac.browser")
data class BrowserConfiguration(
    val remoteDebugUrl: String,
    val userAgent: String,
    val viewport: Viewport,
) {
    data class Viewport(
        val width: Int,
        val height: Int,
    )
}
