package fr.sdis64.backend.statistics

import fr.sdis64.api.statistics.CallStatistic
import fr.sdis64.api.statistics.InterventionStatistic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stats", produces = [MediaType.APPLICATION_JSON_VALUE])
class StatisticsController(
    @Autowired private val callStatisticsService: CallStatisticsService,
    @Autowired private val interventionStatisticsService: InterventionStatisticsService,
    @Autowired private val responseTimeStatisticService: ResponseTimeStatisticService,
) {
    @GetMapping(value = ["/interventions"])
    suspend fun getInterventionStats(): Map<String, InterventionStatistic> =
        interventionStatisticsService.getInterventionStats()

    @GetMapping(value = ["/calls"])
    suspend fun getCallsStats(): Map<String, CallStatistic> = callStatisticsService.getCallStats()

    @GetMapping(value = ["/response-time"])
    suspend fun getResponseTime(): Int = responseTimeStatisticService.getResponseTime()
}
