package fr.sdis64.brain.operators

import fr.sdis64.api.operators.Operator
import fr.sdis64.api.operators.OperatorPhoneNumber
import fr.sdis64.api.operators.OperatorStatus
import fr.sdis64.brain.utilities.mapToSet
import fr.sdis64.brain.utilities.orNotFound
import fr.sdis64.brain.utilities.toDTO
import fr.sdis64.brain.utilities.toEntity
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/operators")
class OperatorController(
    @Autowired private val operatorService: OperatorService,
) {
    @GetMapping
    suspend fun getOperators(): Set<Operator> = operatorService.getOperators()
}

@RestController
@RequestMapping("/operators/statuses")
class OperatorStatusController(
    @Autowired private val operatorStatusRepository: OperatorStatusRepository,
) {
    @GetMapping
    fun findAllOperatorStatuses(): Set<OperatorStatus> =
        operatorStatusRepository.findAll().mapToSet { it.toDTO() }

    @GetMapping(value = ["/{id}"])
    fun findOperatorStatus(@PathVariable id: Long): OperatorStatus =
        operatorStatusRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun saveOperatorStatus(@Valid @RequestBody operatorStatus: OperatorStatus): OperatorStatus =
        operatorStatusRepository.save(operatorStatus.toEntity()).toDTO()

    @DeleteMapping(value = ["/{id}"])
    fun deleteOperatorStatus(@PathVariable id: Long): ResponseEntity<OperatorStatus> {
        operatorStatusRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}

@RestController
@RequestMapping("/operators/phones")
class OperatorPhoneController(
    @Autowired private val operatorPhoneNumberRepository: OperatorPhoneNumberRepository,
) {
    @GetMapping
    fun findAllOperatorPhoneNumbers(): Set<OperatorPhoneNumber> =
        operatorPhoneNumberRepository.findAll().mapToSet { it.toDTO() }

    @GetMapping(value = ["/{id}"])
    fun findOperatorPhoneNumber(@PathVariable id: Long): OperatorPhoneNumber =
        operatorPhoneNumberRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun saveOperatorPhoneNumber(@Valid @RequestBody operatorPhoneNumber: OperatorPhoneNumber): OperatorPhoneNumber =
        operatorPhoneNumberRepository.save(operatorPhoneNumber.toEntity()).toDTO()

    @DeleteMapping(value = ["/{id}"])
    fun deleteOperatorPhoneNumber(@PathVariable id: Long): ResponseEntity<OperatorPhoneNumber> {
        operatorPhoneNumberRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
