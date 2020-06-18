package fr.sdis64.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.client.CtacClient
import fr.sdis64.client.latestCriticalChange
import fr.sdis64.ui.utilities.errorOr
import fr.sdis64.ui.utilities.rememberLoadingState
import kotlinx.browser.window
import kotlinx.datetime.internal.JSJoda.Instant

@Composable
fun ScreenRefresher(client: CtacClient, uiSpawnedAt: Instant) {
    val latestCriticalChange by rememberLoadingState { client.latestCriticalChange() }

    errorOr(latestCriticalChange) {
        if (uiSpawnedAt.toEpochMilli().toLong() < it.toEpochMilliseconds()) {
            window.location.reload()
        }
    }
}
