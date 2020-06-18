package fr.sdis64.brain.indicators

import fr.sdis64.api.indicators.ManualIndicatorCategory
import fr.sdis64.api.indicators.ManualIndicatorLevel
import fr.sdis64.api.indicators.ManualIndicatorType
import fr.sdis64.api.indicators.WeatherIndicator
import fr.sdis64.brain.utilities.entities.withIdOf
import fr.sdis64.brain.utilities.mapToSet
import fr.sdis64.brain.utilities.orNotFound
import fr.sdis64.brain.utilities.toDTO
import fr.sdis64.brain.utilities.toEntity
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import fr.sdis64.api.indicators.GriffonIndicator as GriffonIndicatorDto

@RestController
@RequestMapping("/indicators/weather")
class WeatherIndicatorController(
    @Autowired private val weatherService: WeatherService,
) {
    @GetMapping
    suspend fun getWeatherIndicators(): Set<WeatherIndicator> = weatherService.getIndicators()
}

@RestController
@RequestMapping("/indicators/griffon")
class GriffonIndicatorController(
    @Autowired private val griffonService: GriffonService,
) {
    @GetMapping
    suspend fun getGriffonIndicator(): GriffonIndicatorDto = griffonService.getGriffonIndicator().toDTO()
}

@RestController
@RequestMapping("/indicators/manual/levels")
class ManualIndicatorLevelController(
    @Autowired private val levelRepository: ManualIndicatorLevelRepository,
    @Autowired private val categoryRepository: ManualIndicatorCategoryRepository,
) {
    @GetMapping
    fun findAllLevels(
        @RequestParam(required = false)
        active: Boolean?,
        @RequestParam(required = false)
        type: ManualIndicatorType?,
    ): Set<ManualIndicatorLevel> {
        val entities = when (type) {
            null -> when (active) {
                null -> levelRepository.findAll()
                else -> levelRepository.findAllByActive(active)
            }

            else -> when (active) {
                null -> levelRepository.findAllByCategoryType(type)
                else -> levelRepository.findAllByActiveAndCategoryType(active, type)
            }
        }
        return entities.mapToSet { it.toDTO() }
    }

    @GetMapping(value = ["/{id}"])
    fun findLevel(@PathVariable(value = "id") id: Long): ManualIndicatorLevel =
        levelRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun saveLevel(@Valid @RequestBody manualIndicatorLevel: ManualIndicatorLevel): ManualIndicatorLevel {
        val manualIndicatorLevel1 = manualIndicatorLevel.toEntity()
        val manualIndicatorCategoryWithId = categoryRepository.save(manualIndicatorLevel1.category)
        return levelRepository.save(
            manualIndicatorLevel1
                .copy(category = manualIndicatorCategoryWithId)
                .withIdOf(manualIndicatorLevel1)
        ).toDTO()
    }

    @DeleteMapping(value = ["/{id}"])
    fun deleteLevel(@PathVariable id: Long): ResponseEntity<Unit> {
        levelRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

}

@RestController
@RequestMapping("/indicators/manual/categories")
class ManualIndicatorCategoryController(
    @Autowired private val categoryRepository: ManualIndicatorCategoryRepository,
) {
    @GetMapping(value = ["/{id}"])
    fun findCategory(@PathVariable id: Long): ManualIndicatorCategory =
        categoryRepository.findById(id).map { it.toDTO() }.orNotFound()

    @PostMapping
    fun saveCategory(@Valid @RequestBody manualIndicatorCategory: ManualIndicatorCategory): ManualIndicatorCategory =
        categoryRepository.save(manualIndicatorCategory.toEntity()).toDTO()

    @DeleteMapping(value = ["/{id}"])
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Unit> {
        categoryRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun findAllCategories(): Set<ManualIndicatorCategory> = categoryRepository.findAll().mapToSet { it.toDTO() }
}
