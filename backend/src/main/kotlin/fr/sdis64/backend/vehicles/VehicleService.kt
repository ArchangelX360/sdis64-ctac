package fr.sdis64.backend.vehicles

import fr.sdis64.api.vehicles.*
import fr.sdis64.backend.cis.CisRepository
import fr.sdis64.backend.systel.SystelClient
import fr.sdis64.backend.systel.SystelVehicle
import fr.sdis64.backend.systel.SystelVehicleFunction
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

@ConfigurationProperties(prefix = "ctac.vehicles")
data class VehicleConfiguration(
    val cachePollingFrequencyMillis: Int,
    val helicopterCis: String,
) {
    val cachePollingPeriod: Duration
        get() = cachePollingFrequencyMillis.milliseconds
}

@Service
class VehicleService(
    @Autowired private val vehicleConfiguration: VehicleConfiguration,
    @Autowired private val systelClient: SystelClient,
    @Autowired private val cisRepository: CisRepository,
    @Autowired private val vehicleTypeRepository: VehicleTypeRepository,
    @Autowired private val vehicleStatusRepository: VehicleStatusRepository,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    private val vehiclePoller: FetcherScheduler<Set<Vehicle>> = FetcherScheduler(
        name = "systel_cache_vehicles",
        fetch = ::fetchVehicles,
        period = vehicleConfiguration.cachePollingPeriod,
        registry = registry,
    )

    init {
        vehiclePoller.startIn(scheduledFetcherScope)
    }

    suspend fun getHelicopters(): List<Vehicle> = getVehicles()
        .filter { it.cis.name == vehicleConfiguration.helicopterCis }

    suspend fun getDisplayableVehicles(): List<Vehicle> = getVehicles()
        .filter { it.displayable }
        .sorted()

    suspend fun getVehicles(): Set<Vehicle> = vehiclePoller.getValue()

    // each vehicle is either the vehicle or an instance of a potential secondary function
    // the same vehicle could occur twice in the list
    private suspend fun fetchVehicles(): Set<Vehicle> {
        val systelResponse = systelClient.getVehicles()

        val cisMap = cisRepository.findAll().associate { it.name to it.toDTO() }
        val statusMap = vehicleStatusRepository.findAll().associate { it.name to it.toDTO() }
        val typeMap = vehicleTypeRepository.findAll().associate { it.name to it.toDTO() }

        return systelResponse.mapNotNull { it.toVehicle(cisMap, statusMap, typeMap) }.toSet()
    }

    private fun SystelVehicle.toVehicle(
        cisMap: Map<String, Cis>,
        statusMap: Map<String, VehicleStatus>,
        typeMap: Map<String, VehicleType>,
    ): Vehicle? {
        try {
            val type = typeMap[type] ?: throw MissingTypeException("unknown vehicle type: $type")
            val cis = cisMap[cis] ?: throw ParseException("unknown vehicle CIS: $cis")
            val order = order.toIntOrNull() ?: throw ParseException("invalid vehicle order $order")
            val primaryFunction = this.primaryFunction.toVehicleFunction(statusMap, typeMap)
            val secondaryFunctions = this.secondaryFunctions.mapNotNull {
                // toVehicleFunctionOrNull swallows the eventual parsing exceptions, we still want to return a Vehicle
                // even though some of its secondary functions could not be parsed properly and thus it is not a perfect
                // representation of the vehicle that Systel has in DB.
                // Firemen are often adding vehicle types in Systel for their own needs, and these additions would
                // immediately make this routine discard some vehicles if we were not swallowing the parsing exceptions.
                it.toVehicleFunctionOrNull(statusMap, typeMap)
            }.toSet()

            return Vehicle(
                name = type.name,
                order = order,
                cis = cis,
                primaryFunction = primaryFunction,
                secondaryFunctions = secondaryFunctions,
            )
        } catch (e: MissingTypeException) {
            // non critical, we do not want to flood the DB with types that are not really important for the UI
            LOG.debug(e.message)
            return null
        } catch (e: ParseException) {
            LOG.warn(e.message)
            return null
        }
    }

    private fun SystelVehicleFunction.toVehicleFunctionOrNull(
        statusMap: Map<String, VehicleStatus>,
        typeMap: Map<String, VehicleType>,
    ): VehicleFunction? {
        return try {
            this.toVehicleFunction(statusMap, typeMap)
        } catch (e: MissingTypeException) {
            // non critical, we do not want to flood the DB with types that are not really important for the UI
            LOG.debug(e.message)
            null
        } catch (e: ParseException) {
            LOG.warn(e.message)
            null
        }
    }

    private fun SystelVehicleFunction.toVehicleFunction(
        statusMap: Map<String, VehicleStatus>,
        typeMap: Map<String, VehicleType>,
    ): VehicleFunction {
        val type = typeMap[type] ?: throw MissingTypeException("unknown vehicle type: $type")
        val status = statusMap[status] ?: throw ParseException("unknown vehicle status: $status")
        val state = state.toVehicleState() ?: throw ParseException("unknown vehicle state: $state")

        return VehicleFunction(state = state, status = status, type = type)
    }

    private fun String.toVehicleState(): VehicleState? = when (this) {
        "ARMABLE" -> VehicleState.ARMABLE
        "NON ARMABLE" -> VehicleState.NON_ARMABLE
        "INDISPONIBLE" -> VehicleState.UNAVAILABLE
        else -> null
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VehicleService::class.java)
    }
}

private open class ParseException(message: String) : Exception(message)

private class MissingTypeException(message: String) : ParseException(message)
