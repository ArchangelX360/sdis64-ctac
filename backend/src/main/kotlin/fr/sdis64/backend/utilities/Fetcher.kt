package fr.sdis64.backend.utilities

import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.coroutineContext
import kotlin.text.Typography.ellipsis
import kotlin.time.Duration

interface Fetcher<T : Any> { // : Any to prevent T to be nullable
    val name: String

    suspend fun fetch(): T
}

interface InvalidatingFetcher<T : Any> : Fetcher<T> { // : Any to prevent T to be nullable
    suspend fun onError(): T
}

class PollerFetcher<T : Any>(
    override val name: String,
    private val fetching: suspend () -> T,
) : Fetcher<T> {
    override suspend fun fetch() = fetching()
}

class FetcherScheduler<T : Any>(
    // : Any to prevent T to be nullable
    private val fetcher: Fetcher<T>,
    private val period: Duration,
    registry: MeterRegistry,
    private val initialDelay: Duration = Duration.ZERO,
    private val maxRetries: Int = 5,
) {
    private val lastSuccessfulRefresh: AtomicLong = registry.gauge(
        "backend_fetcher_last_successful_refresh",
        mutableListOf(ImmutableTag("fetcher_name", fetcher.name)),
        AtomicLong()
    )!!

    private val state: MutableStateFlow<T?> = MutableStateFlow(null)

    constructor(
        name: String,
        fetch: suspend () -> T,
        period: Duration,
        registry: MeterRegistry,
        initialDelay: Duration = Duration.ZERO,
        maxRetry: Int = 5,
    ) : this(PollerFetcher(name, fetch), period, registry, initialDelay, maxRetry)

    fun startIn(scope: CoroutineScope): Job = scope.launch(CoroutineName(fetcher.name)) { runFetchLoop() }

    suspend fun runOnce() {
        LOG.info("[${fetcher.name}] Running once unscheduled")
        updateStateWithRetries()
    }

    private suspend fun runFetchLoop() {
        LOG.info("[${fetcher.name}] [period: $period] [initial delay: $initialDelay] Starting fetcher scheduler")
        delay(initialDelay)
        while (true) {
            val v = withTimeoutOrNull(period) {
                updateStateWithRetries()
            }
            if (v == null) {
                LOG.warn("[${fetcher.name}] timed out fetching, will retry")
                continue // skip delay
            }
            delay(period)
        }
    }

    private suspend fun updateStateWithRetries() {
        repeat(maxRetries) { attemptIndex ->
            try {
                updateState()
                return
            } catch (e: Exception) {
                // In case we got cancelled by our parent, we ensure good behaviour with regard to the control flow
                coroutineContext.ensureActive()

                val remainingRetries = maxRetries - 1 - attemptIndex
                if (remainingRetries > 0) {
                    LOG.warn("[${fetcher.name}] failed to fetch (${e::class.simpleName}) (${e.message?.take(50)}[$ellipsis]), will retry at most $remainingRetries more time(s)")
                    LOG.debug("[${fetcher.name}] failed to fetch with", e)
                } else {
                    LOG.error("[${fetcher.name}] failed to fetch with: ${e.message}")
                }
                if (fetcher is InvalidatingFetcher) {
                    try {
                        state.value = fetcher.onError()
                    } catch (e: Exception) {
                        coroutineContext.ensureActive()
                        LOG.error("[${fetcher.name}] failed to execute onError with:", e)
                    }
                }
            }
        }
    }

    private suspend fun updateState() {
        LOG.debug("[${fetcher.name}] Fetching...")
        state.value = fetcher.fetch()
        lastSuccessfulRefresh.set(Instant.now().epochSecond)
        LOG.debug("[${fetcher.name}] successfully fetched, will run again in $period")
    }

    suspend fun getValue(): T = state.filterNotNull().first()

    companion object {
        private val LOG = LoggerFactory.getLogger(FetcherScheduler::class.java)
    }
}
