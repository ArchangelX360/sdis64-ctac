package fr.sdis64.backend.systel

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.time.Duration.Companion.seconds

@ConfigurationProperties(prefix = "ctac.systel")
data class SystelConfiguration(
    val baseUrl: String
)

private const val responseTimeOverestimationSeconds = 3

class SystelClient(
    private val httpClient: HttpClient,
    private val systelConfiguration: SystelConfiguration,
) {
    private val json = Json { isLenient = true }

    private val baseUrl
        get() = URLBuilder(systelConfiguration.baseUrl).also {
            it.path("/ServPA/rest")
            it.parameters.append("format", "json")
        }

    suspend fun getOperators() =
        callSystel(baseUrl.appendDataSource("operateur.connecte.telephonie").buildString()).toSystelOperators()

    suspend fun getVehicles() =
        callSystel(baseUrl.appendDataSource("materiel.departement").buildString()).toSystelVehicles()

    suspend fun getCallAverageResponseTimeStatistic() =
        getStatistic("appel.appel_urgent.sdis.attente_moyenne_sec") - responseTimeOverestimationSeconds

    suspend fun getCallEmergencyDayStatistic() = getStatistic("cta1.appel.18.plancher_7h")
    suspend fun getCallEmergencyYearStatistic() = getStatistic("cta1.appel.18.annee")
    suspend fun getCallEmergencyOngoingStatistic() = getStatistic("cta1.appel.18.en_cours")
    suspend fun getCallEmergencyOnholdStatistic() = getStatistic("cta1.appel.18.entrants")

    suspend fun getCallOperationalDayStatistic() = getStatistic("cta1.appel.operationnel.jour")
    suspend fun getCallOperationalYearStatistic() = getStatistic("cta1.appel.operationnel.annee")
    suspend fun getCallOperationalOngoingStatistic() = getStatistic("cta1.appel.operationnel.en_cours")
    suspend fun getCallOperationalOnholdStatistic() = getStatistic("cta1.appel.operationnel.en_attente")

    suspend fun getCallOutDayStatistic() = getStatistic("cta1.appel.sortant.jour")
    suspend fun getCallOutYearStatistic() = getStatistic("cta1.appel.sortant.annee")
    suspend fun getCallOutOngoingStatistic() = getStatistic("cta1.appel.sortant.en_cours")

    suspend fun getCallInOutDayStatistic() = getStatistic("cta1.appel.entrant.sortant.jour")
    suspend fun getCallInOutYearStatistic() = getStatistic("cta1.appel.entrant.sortant.annee")
    suspend fun getCallInOutOngoingStatistic() = getStatistic("cta1.appel.entrant.sortant.en_cours")

    suspend fun getCallEcobuageDayStatistic() = getStatistic("cta1.appel.ecobuage.jour")
    suspend fun getCallEcobuageYearStatistic() = getStatistic("cta1.appel.ecobuage.annee")
    suspend fun getCallEcobuageOngoingStatistic() = getStatistic("cta1.appel.ecobuage.en_cours")
    suspend fun getCallEcobuageOnholdStatistic() = getStatistic("cta1.appel.ecobuage.en_attente")

    suspend fun getInterventionDayStatisticByTypes() =
        getInterventionStatisticByType(baseUrl.appendDataSource("cta1.sinistre.categorie.jour").buildString())

    suspend fun getInterventionOngoingStatisticByTypes() =
        getInterventionStatisticByType(baseUrl.appendDataSource("cta1.inter.sinistre.categorie.en_cours").buildString())

    suspend fun getInterventionYearStatisticByTypes() = coroutineScope {
        InterventionType.values()
            .map { async { it to getInterventionYearStatistic(it.systelKey) } }
            .awaitAll()
            .toMap()
    }

    private suspend fun getInterventionYearStatistic(interventionCategory: String): Int {
        val url = baseUrl
            .appendDataSource("cta1.inter.annee.choix_categories")
            .appendInterventionCategory(interventionCategory)
            .buildString()
        return callSystel(url).toInteger()
    }

    /**
     * Intervention statistics of the current year are not directly served by Systel as day and ongoing statistics are.
     * Instead, you need to specify which type you want to include in the response.
     */
    private fun URLBuilder.appendInterventionCategory(systelCategoryKey: String): URLBuilder {
        this.parameters.append("p1", systelCategoryKey)
        return this
    }

    private suspend fun getStatistic(ds: String): Int {
        return callSystel(baseUrl.appendDataSource(ds).buildString()).toInteger()
    }

    private suspend fun getInterventionStatisticByType(uri: String): Map<InterventionType, Int?> {
        return callSystel(uri)
            .toMap()
            .mapNotNull {
                try {
                    InterventionType.create(it.key) to it.value
                } catch (e: Exception) {
                    LOG.debug("ignoring value of intervention type ${it.key}: ${e.message}")
                    null
                }
            }
            .toMap()
    }

    private fun URLBuilder.appendDataSource(name: String): URLBuilder {
        if (name.isBlank()) {
            error("ds should not be empty")
        }
        this.parameters.append("ds", name)
        return this
    }

    private suspend fun callSystel(uri: String): SystelResponse {
        LOG.trace("calling Systel with $uri")

        val rawResponse: ByteArray = httpClient.get(uri) {
            timeout {
                connectTimeoutMillis = 30.seconds.inWholeMilliseconds
                requestTimeoutMillis = 30.seconds.inWholeMilliseconds
                socketTimeoutMillis = 30.seconds.inWholeMilliseconds
            }
        }.body()
        val response =
            json.decodeFromString(JsonResponseData.serializer(), rawResponse.toString(Charsets.ISO_8859_1))

        if (response.errors.isNotEmpty()) {
            error("Systel response got errors: ${response.errors}")
        }

        return SystelResponse(response.result)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SystelClient::class.java)
    }
}

@Serializable
private data class JsonResponseData(
    val errors: List<String> = emptyList(),
    val result: List<List<String>> = listOf(listOf()),
)
