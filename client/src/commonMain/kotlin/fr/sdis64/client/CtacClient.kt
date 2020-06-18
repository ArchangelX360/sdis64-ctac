package fr.sdis64.client

import fr.sdis64.api.Session
import fr.sdis64.api.indicators.*
import fr.sdis64.api.operators.Operator
import fr.sdis64.api.operators.OperatorPhoneNumber
import fr.sdis64.api.operators.OperatorStatus
import fr.sdis64.api.organisms.Organism
import fr.sdis64.api.organisms.OrganismCategory
import fr.sdis64.api.statistics.CallStatistic
import fr.sdis64.api.statistics.InterventionStatistic
import fr.sdis64.api.vehicles.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json as KxJson

class CtacClient(
    private val baseUrl: String,
) {
    @OptIn(ExperimentalSerializationApi::class)
    private val http = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(KxJson {
                encodeDefaults = true
                explicitNulls = false
                ignoreUnknownKeys = true
            })
        }
        install(HttpCookies)
    }

    private val session = MutableStateFlow<Session?>(null)

    suspend fun login(username: String, password: String) {
        http.submitForm(
            formParameters = Parameters.build {
                append("username", username)
                append("password", password)
            },
        ) {
            url("${baseUrl}/login")
        }
        session.value = getSession()
    }

    suspend fun logout() {
        http.post("${baseUrl}/logout")
        session.value = null
    }

    fun currentSession(): StateFlow<Session?> = session

    suspend fun recoverSession() {
        this.session.value = getSession()
    }

    private suspend fun getSession(): Session? = try {
        http.get("${baseUrl}/session").body()
    } catch (e: ClientRequestException) {
        null
    }

    suspend fun getLatestCriticalChange(): Instant = http.get("${baseUrl}/update/latest-critical-change").body()

    suspend fun getWeatherIndicators(): Set<WeatherIndicator> = http.get("${baseUrl}/indicators/weather").body()

    suspend fun getGriffonIndicator(): GriffonIndicator = http.get("${baseUrl}/indicators/griffon").body()

    suspend fun findManualIndicatorLevels(
        active: Boolean? = null,
        type: ManualIndicatorType? = null,
    ): Set<ManualIndicatorLevel> = http.get("${baseUrl}/indicators/manual/levels") {
        parameter("active", active)
        parameter("type", type)
    }.body()

    suspend fun findManualIndicatorLevel(id: Long): ManualIndicatorLevel =
        http.get("${baseUrl}/indicators/manual/levels/$id").body()

    suspend fun saveManualIndicatorLevel(level: ManualIndicatorLevel): ManualIndicatorLevel {
        return http.post("${baseUrl}/indicators/manual/levels") {
            contentType(ContentType.Application.Json)
            setBody(body = level)
        }.body()
    }

    suspend fun deleteManualIndicatorLevel(id: Long): Unit =
        http.delete("${baseUrl}/indicators/manual/levels/$id").body()

    suspend fun findManualIndicatorCategories(): Set<ManualIndicatorCategory> =
        http.get("${baseUrl}/indicators/manual/categories").body()

    suspend fun findManualIndicatorCategory(id: Long): ManualIndicatorCategory =
        http.get("${baseUrl}/indicators/manual/categories/$id").body()

    suspend fun saveManualIndicatorCategory(category: ManualIndicatorCategory): ManualIndicatorCategory {
        return http.post("${baseUrl}/indicators/manual/categories") {
            contentType(ContentType.Application.Json)
            setBody(body = category)
        }.body()
    }

    suspend fun deleteManualIndicatorCategory(id: Long): Unit =
        http.delete("${baseUrl}/indicators/manual/categories/$id").body()

    suspend fun getUnseenMailSubjects(): Set<String> =
        http.get("${baseUrl}/mailer/unseen").body()

    suspend fun findOperators(): Set<Operator> = http.get("${baseUrl}/operators").body()

    suspend fun findAllOperatorStatuses(): Set<OperatorStatus> =
        http.get("${baseUrl}/operators/statuses").body()

    suspend fun findOperatorStatus(id: Long): OperatorStatus =
        http.get("${baseUrl}/operators/statuses/$id").body()

    suspend fun saveOperatorStatus(status: OperatorStatus): OperatorStatus =
        http.post("${baseUrl}/operators/statuses") {
            setBody(status)
        }.body()

    suspend fun deleteOperatorStatus(id: Long): Unit =
        http.delete("${baseUrl}/operators/statuses/$id").body()

    suspend fun findAllOperatorPhoneNumbers(): Set<OperatorPhoneNumber> =
        http.get("${baseUrl}/operators/phones").body()

    suspend fun findOperatorPhoneNumber(id: Long): OperatorPhoneNumber =
        http.get("${baseUrl}/operators/phones/$id").body()

    suspend fun saveOperatorPhoneNumber(status: OperatorPhoneNumber): OperatorPhoneNumber =
        http.post("${baseUrl}/operators/phones") {
            setBody(status)
        }.body()

    suspend fun deleteOperatorPhoneNumber(id: Long): Unit =
        http.delete("${baseUrl}/operators/phones/$id").body()

    suspend fun findAllOrganisms(categoryId: Long? = null, activeAt: Instant? = null): Set<Organism> =
        http.get("${baseUrl}/organisms") {
            parameter("categoryId", categoryId)
            parameter("activeAt", activeAt)
        }.body()

    suspend fun findOrganism(id: Long): Organism =
        http.get("${baseUrl}/organisms/$id").body()

    suspend fun saveOrganism(organism: Organism): Organism {
        return http.post("${baseUrl}/organisms") {
            contentType(ContentType.Application.Json)
            setBody(body = organism)
        }.body()
    }

    suspend fun deleteOrganism(id: Long): Unit =
        http.delete("${baseUrl}/organisms/$id").body()

    suspend fun findAllOrganismCategories(): Set<OrganismCategory> =
        http.get("${baseUrl}/organisms/categories").body()

    suspend fun findOrganismCategory(id: Long): OrganismCategory =
        http.get("${baseUrl}/organisms/categories/$id").body()

    suspend fun saveOrganismCategory(category: OrganismCategory): OrganismCategory {
        return http.post("${baseUrl}/organisms/categories") {
            contentType(ContentType.Application.Json)
            setBody(body = category)
        }.body()
    }

    suspend fun deleteOrganismCategory(id: Long): Unit =
        http.delete("${baseUrl}/organisms/categories/$id").body()

    suspend fun getInterventionStats(): Map<String, InterventionStatistic> =
        http.get("${baseUrl}/stats/interventions").body()

    suspend fun getCallsStats(): Map<String, CallStatistic> =
        http.get("${baseUrl}/stats/calls").body()

    suspend fun getResponseTime(): Int =
        http.get("${baseUrl}/stats/response-time").body()

    suspend fun getVehicles(): Set<Vehicle> =
        http.get("${baseUrl}/vehicles").body()

    /**
     * Get all vehicles that could be shown at least one UI, they are *sorted* according to positioning parameter of vehicle and CIS
     */
    suspend fun getDisplayableVehicles(): List<Vehicle> =
        http.get("${baseUrl}/vehicles/displayable").body()

    suspend fun getHelicopters(): List<Vehicle> =
        http.get("${baseUrl}/vehicles/helicopters").body()

    suspend fun getHelicopterPosition(): HelicopterPosition =
        http.get("${baseUrl}/vehicles/helicopters-positions/dragon64").body()

    suspend fun getVehicleMaps(): Set<VehicleDisplayMap> =
        http.get("${baseUrl}/vehicles/display-maps").body()

    suspend fun getVehicleMap(name: String): VehicleDisplayMap =
        http.get("${baseUrl}/vehicles/display-maps/$name").body()

    suspend fun findAllVehicleStatuses(): Set<VehicleStatus> =
        http.get("${baseUrl}/vehicles/statuses").body()

    suspend fun findVehicleStatus(id: Long): VehicleStatus =
        http.get("${baseUrl}/vehicles/statuses/$id").body()

    suspend fun saveVehicleStatus(status: VehicleStatus): VehicleStatus {
        return http.post("${baseUrl}/vehicles/statuses") {
            contentType(ContentType.Application.Json)
            setBody(body = status)
        }.body()
    }

    suspend fun deleteVehicleStatus(id: Long): VehicleStatus =
        http.delete("${baseUrl}/vehicles/statuses/$id").body()

    suspend fun findAllVehicleTypes(): Set<VehicleType> =
        http.get("${baseUrl}/vehicles/types").body()

    suspend fun findVehicleType(id: Long): VehicleType =
        http.get("${baseUrl}/vehicles/types/$id").body()

    suspend fun saveVehicleType(type: VehicleType): VehicleType {
        return http.post("${baseUrl}/vehicles/types") {
            contentType(ContentType.Application.Json)
            setBody(body = type)
        }.body()
    }

    suspend fun deleteVehicleType(id: Long): VehicleType =
        http.delete("${baseUrl}/vehicles/types/$id").body()

    suspend fun findAllVehicleMaps(): Set<VehicleMap> =
        http.get("${baseUrl}/vehicles/maps").body()

    suspend fun findVehicleMap(id: Long): VehicleMap =
        http.get("${baseUrl}/vehicles/maps/$id").body()

    suspend fun saveVehicleMap(map: VehicleMap): VehicleMap {
        return http.post("${baseUrl}/vehicles/maps") {
            contentType(ContentType.Application.Json)
            setBody(body = map)
        }.body()
    }

    suspend fun deleteVehicleMap(id: Long): VehicleMap =
        http.delete("${baseUrl}/vehicles/maps/$id").body()

    suspend fun findAllCis(): Set<Cis> =
        http.get("${baseUrl}/cis").body()

    suspend fun findCis(id: Long): Cis =
        http.get("${baseUrl}/cis/$id").body()

    suspend fun saveCis(cis: Cis): Cis {
        return http.post("${baseUrl}/cis") {
            contentType(ContentType.Application.Json)
            setBody(body = cis)
        }.body()
    }

    suspend fun deleteCis(id: Long): Unit =
        http.delete("${baseUrl}/cis/$id").body()
}
