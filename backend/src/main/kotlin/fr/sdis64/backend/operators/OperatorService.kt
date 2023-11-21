package fr.sdis64.backend.operators

import fr.sdis64.api.operators.Operator
import fr.sdis64.backend.operators.entities.OperatorPhoneNumber
import fr.sdis64.backend.operators.entities.OperatorStatus
import fr.sdis64.backend.systel.SystelClient
import fr.sdis64.backend.systel.SystelOperator
import fr.sdis64.backend.utilities.AbstractScheduledFetcherService
import fr.sdis64.backend.utilities.FetcherScheduler
import fr.sdis64.backend.utilities.toDTO
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ConfigurationProperties(prefix = "ctac.operators")
data class OperatorsConfiguration(
    private val cachePollingFrequencyMillis: Int,
) {
    val cachePollingPeriod: Duration
        get() = cachePollingFrequencyMillis.milliseconds
}

@Service
class OperatorService(
    @Autowired private val systelClient: SystelClient,
    @Autowired private val operatorStatusRepository: OperatorStatusRepository,
    @Autowired private val operatorPhoneNumberRepository: OperatorPhoneNumberRepository,
    @Autowired private val operatorsConfiguration: OperatorsConfiguration,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    private val operatorsPoller: FetcherScheduler<Set<Operator>> = FetcherScheduler(
        name = "systel_cache_operators",
        fetch = ::fetchOperators,
        period = operatorsConfiguration.cachePollingPeriod,
        registry = registry,
    )

    init {
        operatorsPoller.startIn(scheduledFetcherScope)
    }

    suspend fun getOperators(): Set<Operator> = operatorsPoller.getValue()

    private suspend fun fetchOperators(): Set<Operator> {
        val operators = systelClient.getOperators()

        val operatorPhoneNumberMap = operatorPhoneNumberRepository.findAll().associateBy { it.systelNumber }
        val operatorStatusMap = operatorStatusRepository.findAll().associateBy { it.name }

        return operators
            .map { o -> createOperator(o, operatorPhoneNumberMap, operatorStatusMap) }
            .toSet()
    }

    private fun createOperator(
        systelOperator: SystelOperator,
        phoneNumberMap: Map<String, OperatorPhoneNumber>,
        statusMap: Map<String, OperatorStatus>
    ): Operator {
        val defaultPhoneNumber = OperatorPhoneNumber(
            systelNumber = systelOperator.phoneNumber,
            realNumber = systelOperator.phoneNumber,
        )

        val defaultStatus = OperatorStatus(
            name = systelOperator.status,
            displayed = false,
        )

        return Operator(
            post = systelOperator.post,
            name = systelOperator.name,
            function = systelOperator.function,
            status = statusMap.getOrDefault(systelOperator.status, defaultStatus).toDTO(),
            phoneNumber = phoneNumberMap.getOrDefault(systelOperator.phoneNumber, defaultPhoneNumber).toDTO(),
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OperatorService::class.java)
    }
}
