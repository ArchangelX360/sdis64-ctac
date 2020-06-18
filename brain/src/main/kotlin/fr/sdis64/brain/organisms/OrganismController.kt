package fr.sdis64.brain.organisms

import fr.sdis64.api.organisms.Organism
import fr.sdis64.api.organisms.OrganismCategory
import fr.sdis64.brain.utilities.entities.withIdOf
import fr.sdis64.brain.utilities.mapToSet
import fr.sdis64.brain.utilities.orNotFound
import fr.sdis64.brain.utilities.toDTO
import fr.sdis64.brain.utilities.toEntity
import jakarta.validation.Valid
import kotlinx.datetime.Instant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@Validated
@RequestMapping("/organisms")
class OrganismController(
    @Autowired private val organismRepository: OrganismRepository,
    @Autowired private val organismCategoryRepository: OrganismCategoryRepository,
) {
    @GetMapping(value = ["/{id}"])
    fun find(@PathVariable id: Long): Organism = organismRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun save(@Valid @RequestBody organism: Organism): Organism {
        organism.activeTimeWindows.forEach {
            if (it.start > it.end) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "'organism cannot have durations where 'start' is after 'end''"
                )
            }
        }
        val organismEntity = organism.toEntity()
        val categoryWithId = organismCategoryRepository.save(organismEntity.category)
        return organismRepository.save(organismEntity.copy(category = categoryWithId).withIdOf(organismEntity)).toDTO()
    }

    @DeleteMapping(value = ["/{id}"])
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> {
        organismRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * @param activeAt if specified, will get only organisms that were active at the specified date. Respects ISO DateTime format yyyy-MM-dd'T'HH:mm:ss.SSSZ, e.g. 2000-10-31T01:30:00.000-05:00
     */
    @GetMapping
    fun findAllByCategory(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) activeAt: Instant?,
    ): Set<Organism> = findOrganismEntitiesByCategory(categoryId)
        .asSequence()
        .filter { activeAt == null || it.isActiveAt(activeAt) }
        .toSet()

    private fun findOrganismEntitiesByCategory(categoryId: Long?): Set<Organism> {
        return if (categoryId == null) {
            organismRepository.findAll().mapToSet { it.toDTO() }
        } else {
            val organismCategory = organismCategoryRepository.findById(categoryId).orElse(null)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID is invalid")
            organismRepository.findAllByCategoryId(organismCategory.id).mapToSet { it.toDTO() }
        }
    }
}

@RestController
@Validated
@RequestMapping("/organisms/categories")
class OrganismCategoryController(
    @Autowired private val organismCategoryRepository: OrganismCategoryRepository,
) {
    @GetMapping(value = ["/{id}"])
    fun findCategory(@PathVariable id: Long): OrganismCategory =
        organismCategoryRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun saveCategory(@Valid @RequestBody organismCategory: OrganismCategory): OrganismCategory =
        organismCategoryRepository.save(organismCategory.toEntity()).toDTO()

    @DeleteMapping(value = ["/{id}"])
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Unit> {
        organismCategoryRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun findAllCategories(): Set<OrganismCategory> = organismCategoryRepository.findAll().mapToSet { it.toDTO() }
}
