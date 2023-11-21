package fr.sdis64.backend.vehicles

import fr.sdis64.api.vehicles.*
import fr.sdis64.backend.utilities.toDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@ConfigurationProperties(prefix = "ctac.vehicles.maps")
data class VehicleMapsConfiguration(
    val statusNameBlacklist: List<String>
)

@Service
class VehicleDisplayMapService(
    @Autowired private val vehicleMapRepository: VehicleMapRepository,
    @Autowired private val vehicleService: VehicleService,
    @Autowired private val vehicleConfiguration: VehicleMapsConfiguration,
) {
    suspend fun getVehicleDisplayMaps(): Set<VehicleDisplayMap> {
        val maps = vehicleMapRepository.findAll()

        val relevantVehicles = vehicleService
            .getVehicles()
            .filterNot { it.primaryFunction.status.name in vehicleConfiguration.statusNameBlacklist }
            .toSet()

        return maps.mapNotNull {
            try {
                VehicleDisplayMapBuilder().build(it.toDTO(), relevantVehicles)
            } catch (e: IllegalArgumentException) {
                LOG.error("could not construct map ${it.name}: ${e.message}")
                null
            }
        }.toSet()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(VehicleDisplayMapService::class.java)
    }
}

private class VehicleDisplayMapBuilder {
    private val cisToAvailability: MutableMap<String, Availability> = mutableMapOf()

    fun build(map: VehicleMap, vehicles: Set<Vehicle>): VehicleDisplayMap {
        val relevantVehicles = vehicles.filter { it.isRelevantTo(map) }

        require(relevantVehicles.isNotEmpty()) { "at least one of the list of vehicle specified should be relevant to the constructed map" }

        relevantVehicles.forEach { this.add(it, map) }
        return VehicleDisplayMap(
            mapName = map.name,
            cisToAvailability = cisToAvailability,
        )
    }

    private fun add(v: Vehicle, map: VehicleMap) {
        val type = v.getRelevantFunctionTo(map)
            ?: throw IllegalArgumentException("vehicle should have at least one function in map types")

        when (type.status.category) {
            VehicleStatus.Category.AVAILABLE -> incrementAvailableOf(v.cis)
            VehicleStatus.Category.UNAVAILABLE -> incrementUnavailableOf(v.cis)
            VehicleStatus.Category.NON_ARMABLE -> {
                incrementNonArmablesOf(v.cis)

                val armableInDegradedFunction = v.getRelevantDegradedFunctionsTo(map)
                    .any { it.status.category == VehicleStatus.Category.AVAILABLE && it.state == VehicleState.ARMABLE }

                if (armableInDegradedFunction) {
                    incrementArmablesInSecondaryFunctionOf(v.cis)
                }
            }
        }
    }

    private fun incrementAvailableOf(cis: Cis) {
        val avail = cisToAvailability.getOrPut(cis.name) { Availability() }
        avail.available++
        avail.total++
    }

    private fun incrementNonArmablesOf(cis: Cis) {
        val avail = cisToAvailability.getOrPut(cis.name) { Availability() }
        avail.nonArmable++
        avail.total++
    }

    private fun incrementUnavailableOf(cis: Cis) {
        val avail = cisToAvailability.getOrPut(cis.name) { Availability() }
        avail.unavailable++
        avail.total++
    }

    private fun incrementArmablesInSecondaryFunctionOf(cis: Cis) {
        val avail = cisToAvailability.getOrPut(cis.name) { Availability() }
        avail.armableDegraded++
    }

    private fun Vehicle.isRelevantTo(map: VehicleMap): Boolean {
        return this.getRelevantFunctionTo(map) != null
    }

    private fun Vehicle.getRelevantFunctionTo(map: VehicleMap): VehicleFunction? {
        return this.functions.find { it.type in map.types }
    }

    private fun Vehicle.getRelevantDegradedFunctionsTo(map: VehicleMap): List<VehicleFunction> {
        return this.functions.filter { it.type in map.degradedTypes }
    }
}
