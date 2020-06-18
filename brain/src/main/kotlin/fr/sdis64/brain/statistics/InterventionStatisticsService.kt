package fr.sdis64.brain.statistics

import fr.sdis64.api.statistics.InterventionStatistic
import fr.sdis64.brain.systel.SystelClient
import fr.sdis64.brain.utilities.AbstractScheduledFetcherService
import fr.sdis64.brain.utilities.FetcherScheduler
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ConfigurationProperties(prefix = "ctac.statistics.interventions")
data class InterventionStatisticsConfiguration(
    val cachePollingFrequencyMillis: Int,
) {
    val cachePollingPeriod: Duration
        get() = cachePollingFrequencyMillis.milliseconds
}

@Service
class InterventionStatisticsService(
    @Autowired private val configuration: InterventionStatisticsConfiguration,
    @Autowired private val systelClient: SystelClient,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {

    private val poller: FetcherScheduler<Map<String, InterventionStatistic>> = FetcherScheduler(
        name = "systel_cache_stats_intervention",
        fetch = ::fetchInterventionStats,
        period = configuration.cachePollingPeriod,
        registry = registry,
    )

    init {
        poller.startIn(scheduledFetcherScope)
    }

    suspend fun getInterventionStats(): Map<String, InterventionStatistic> = poller.getValue()

    private suspend fun fetchInterventionStats(): Map<String, InterventionStatistic> = coroutineScope {
        val dayStatDeferred = async { systelClient.getInterventionDayStatisticByTypes() }
        val ongoingStatDeferred = async { systelClient.getInterventionOngoingStatisticByTypes() }
        val yearStatDeferred = async { systelClient.getInterventionYearStatisticByTypes() }
        val dayStat = dayStatDeferred.await()
        val ongoingStat = ongoingStatDeferred.await()
        val yearStat = yearStatDeferred.await()

        val labels = dayStat.keys + ongoingStat.keys + yearStat.keys
        labels.associate {
            // TODO: change this as just a list instead, removing even the title that is never used
            val label = it.name.lowercase()
            label to InterventionStatistic(
                label = label,
                title = it.systelKey,
                day = dayStat[it],
                ongoing = ongoingStat[it],
                year = yearStat[it]
            )
        }
    }

}
