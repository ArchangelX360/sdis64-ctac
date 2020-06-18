package fr.sdis64.brain.vehicles

import fr.sdis64.api.vehicles.*
import fr.sdis64.brain.utilities.mapToSet
import fr.sdis64.brain.utilities.orNotFound
import fr.sdis64.brain.utilities.toDTO
import fr.sdis64.brain.utilities.toEntity
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/vehicles", produces = [MediaType.APPLICATION_JSON_VALUE])
class VehicleController(
    @Autowired private val vehicleService: VehicleService,
    @Autowired private val vehicleDisplayMapService: VehicleDisplayMapService,
    @Autowired private val dragonService: DragonService,
) {
    @GetMapping
    suspend fun getVehicles(): Set<Vehicle> = vehicleService.getVehicles()

    /**
     * Get all vehicles that could be shown at least one UI, they are *sorted* according to positioning parameter of vehicle and CIS
     */
    @GetMapping(value = ["/displayable"])
    suspend fun getDisplayableVehicles(): List<Vehicle> = vehicleService.getDisplayableVehicles()

    @GetMapping(value = ["/helicopters"])
    suspend fun getHelicopters(): List<Vehicle> = vehicleService.getHelicopters()

    @GetMapping(value = ["/helicopters-positions/dragon64"])
    suspend fun getHelicopterPosition(): HelicopterPosition =
        when (val h = dragonService.getHelicopterPosition()) {
            is HelicopterPosition.Unknown -> throw ResponseStatusException(HttpStatus.NOT_FOUND)
            is HelicopterPosition.Known -> h
        }

    @GetMapping(value = ["/display-maps"])
    suspend fun getMaps(): Set<VehicleDisplayMap> = vehicleDisplayMapService.getVehicleDisplayMaps()

    @GetMapping(value = ["/display-maps/{name}"])
    suspend fun getMap(@PathVariable name: String): VehicleDisplayMap =
        vehicleDisplayMapService.getVehicleDisplayMaps().find { it.mapName == name }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
}

@RestController
@RequestMapping("/vehicles/statuses", produces = [MediaType.APPLICATION_JSON_VALUE])
class VehicleStatusController(
    @Autowired private val vehicleStatusRepository: VehicleStatusRepository,
) {
    @GetMapping(value = ["/{id}"])
    fun findVehicleStatus(@PathVariable id: Long): VehicleStatus =
        vehicleStatusRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun saveVehicleStatus(@Valid @RequestBody status: VehicleStatus): VehicleStatus =
        vehicleStatusRepository.save(status.toEntity()).toDTO()

    @DeleteMapping(value = ["/{id}"])
    fun deleteVehicleStatus(@PathVariable id: Long): ResponseEntity<VehicleStatus> {
        vehicleStatusRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun findAllVehicleStatuses(): Set<VehicleStatus> = vehicleStatusRepository.findAll().mapTo(HashSet()) { it.toDTO() }
}

@RestController
@RequestMapping("/vehicles/types")
class VehicleTypeController(
    @Autowired private val vehicleTypeRepository: VehicleTypeRepository,
) {
    @GetMapping(value = ["/{id}"])
    fun findVehicleType(@PathVariable id: Long): VehicleType =
        vehicleTypeRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun saveVehicleType(@Valid @RequestBody type: VehicleType): VehicleType =
        vehicleTypeRepository.save(type.toEntity()).toDTO()

    @DeleteMapping(value = ["/{id}"])
    fun deleteVehicleType(@PathVariable id: Long): ResponseEntity<VehicleType> {
        vehicleTypeRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun findAllVehicleTypes(): Set<VehicleType> = vehicleTypeRepository.findAll().mapTo(HashSet()) { it.toDTO() }
}

@RestController
@RequestMapping("/vehicles/maps")
class VehicleMapController(
    @Autowired private val vehicleMapRepository: VehicleMapRepository,
) {
    @GetMapping(value = ["/{id}"])
    fun findVehicleMap(@PathVariable id: Long): VehicleMap =
        vehicleMapRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun saveVehicleMap(@Valid @RequestBody map: VehicleMap): VehicleMap =
        vehicleMapRepository.save(map.toEntity()).toDTO()

    @DeleteMapping(value = ["/{id}"])
    fun deleteVehicleMap(@PathVariable id: Long): ResponseEntity<Unit> {
        vehicleMapRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun findAllVehicleMaps(): Set<VehicleMap> = vehicleMapRepository.findAll().mapToSet { it.toDTO() }
}
