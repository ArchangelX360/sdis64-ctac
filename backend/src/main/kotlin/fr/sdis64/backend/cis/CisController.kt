package fr.sdis64.backend.cis

import fr.sdis64.api.vehicles.Cis
import fr.sdis64.backend.utilities.mapToSet
import fr.sdis64.backend.utilities.orNotFound
import fr.sdis64.backend.utilities.toDTO
import fr.sdis64.backend.utilities.toEntity
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cis")
class CisController(
    @Autowired private val cisRepository: CisRepository,
) {
    @GetMapping(value = ["/{id}"])
    fun findCis(@PathVariable id: Long): Cis = cisRepository.findById(id).orNotFound().toDTO()

    @PostMapping
    fun saveCis(@Valid @RequestBody cis: Cis): Cis = cisRepository.save(cis.toEntity()).toDTO()

    @DeleteMapping(value = ["/{id}"])
    fun deleteCis(@PathVariable id: Long): ResponseEntity<Cis> {
        cisRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun findAllCis(): Set<Cis> = cisRepository.findAll().mapToSet { it.toDTO() }
}
