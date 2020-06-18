package fr.sdis64.brain.statistics

import fr.sdis64.brain.systel.SystelClient
import fr.sdis64.brain.utilities.AbstractScheduledFetcherService
import fr.sdis64.brain.utilities.FetcherScheduler
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ConfigurationProperties(prefix = "ctac.statistics.response-time")
data class ResponseTimeStatisticConfiguration(
    val cachePollingFrequencyMillis: Int,
) {
    val cachePollingPeriod: Duration
        get() = cachePollingFrequencyMillis.milliseconds
}

@Service
class ResponseTimeStatisticService(
    @Autowired private val configuration: ResponseTimeStatisticConfiguration,
    @Autowired private val systelClient: SystelClient,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    private val poller: FetcherScheduler<Int> = FetcherScheduler(
        name = "systel_cache_stats_response_time",
        fetch = ::fetchResponseTime,
        period = configuration.cachePollingPeriod,
        registry = registry,
    )

    init {
        poller.startIn(scheduledFetcherScope)
    }

    suspend fun getResponseTime(): Int = poller.getValue()

    private suspend fun fetchResponseTime(): Int = systelClient.getCallAverageResponseTimeStatistic()
}
