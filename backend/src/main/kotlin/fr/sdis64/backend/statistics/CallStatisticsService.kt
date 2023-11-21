package fr.sdis64.backend.statistics

import fr.sdis64.api.statistics.CallStatistic
import fr.sdis64.backend.systel.SystelClient
import fr.sdis64.backend.utilities.AbstractScheduledFetcherService
import fr.sdis64.backend.utilities.FetcherScheduler
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ConfigurationProperties(prefix = "ctac.statistics.calls")
data class CallsConfiguration(
    val cachePollingFrequencyMillis: Int,
) {
    val cachePollingPeriod: Duration
        get() = cachePollingFrequencyMillis.milliseconds
}

@Service
class CallStatisticsService(
    @Autowired private val configuration: CallsConfiguration,
    @Autowired private val systelClient: SystelClient,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    private val poller: FetcherScheduler<Map<String, CallStatistic>> = FetcherScheduler(
        name = "systel_cache_stats_call",
        fetch = ::fetchCallStatistics,
        period = configuration.cachePollingPeriod,
        registry = registry,
    )

    init {
        poller.startIn(scheduledFetcherScope)
    }

    suspend fun getCallStats(): Map<String, CallStatistic> = poller.getValue()

    private suspend fun fetchCallStatistics(): Map<String, CallStatistic> = coroutineScope {
        listOf(
            async { fetchCallEmergencyStatistic() },
            async { fetchCallOperationalStatistic() },
            async { fetchCallOutStatistic() },
            async { fetchCallInOutStatistic() },
            async { fetchCallEcobuageStatistic() },
        ).awaitAll()
            // TODO: change this as just a list instead, changing title to displayName
            .associateBy { it.label }
    }

    private suspend fun fetchCallEmergencyStatistic(): CallStatistic = coroutineScope {
        val day = async { systelClient.getCallEmergencyDayStatistic() }
        val year = async { systelClient.getCallEmergencyYearStatistic() }
        val ongoing = async { systelClient.getCallEmergencyOngoingStatistic() }
        val onhold = async { systelClient.getCallEmergencyOnholdStatistic() }

        CallStatistic(
            label = "emergency",
            title = "18",
            day = day.await(),
            year = year.await(),
            ongoing = ongoing.await(),
            onhold = onhold.await()
        )
    }

    private suspend fun fetchCallOperationalStatistic(): CallStatistic = coroutineScope {
        val day = async { systelClient.getCallOperationalDayStatistic() }
        val year = async { systelClient.getCallOperationalYearStatistic() }
        val ongoing = async { systelClient.getCallOperationalOngoingStatistic() }
        val onhold = async { systelClient.getCallOperationalOnholdStatistic() }

        CallStatistic(
            label = "operational",
            title = "Opérationnels",
            day = day.await(),
            year = year.await(),
            ongoing = ongoing.await(),
            onhold = onhold.await()
        )
    }

    private suspend fun fetchCallOutStatistic(): CallStatistic = coroutineScope {
        val day = async { systelClient.getCallOutDayStatistic() }
        val year = async { systelClient.getCallOutYearStatistic() }
        val ongoing = async { systelClient.getCallOutOngoingStatistic() }

        CallStatistic(
            label = "out",
            title = "Sortants",
            day = day.await(),
            year = year.await(),
            ongoing = ongoing.await(),
            onhold = null
        )
    }

    private suspend fun fetchCallInOutStatistic(): CallStatistic = coroutineScope {
        val day = async { systelClient.getCallInOutDayStatistic() }
        val year = async { systelClient.getCallInOutYearStatistic() }
        val ongoing = async { systelClient.getCallInOutOngoingStatistic() }

        CallStatistic(
            label = "inOut",
            title = "Entrants/Sortants",
            day = day.await(),
            year = year.await(),
            ongoing = ongoing.await(),
            onhold = null
        )
    }

    private suspend fun fetchCallEcobuageStatistic(): CallStatistic = coroutineScope {
        val day = async { systelClient.getCallEcobuageDayStatistic() }
        val year = async { systelClient.getCallEcobuageYearStatistic() }
        val ongoing = async { systelClient.getCallEcobuageOngoingStatistic() }
        val onhold = async { systelClient.getCallEcobuageOnholdStatistic() }

        CallStatistic(
            label = "ecobuage",
            title = "Ecobuages",
            day = day.await(),
            year = year.await(),
            ongoing = ongoing.await(),
            onhold = onhold.await()
        )
    }
}
