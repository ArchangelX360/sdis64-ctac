package fr.sdis64.backend.indicators

import fr.sdis64.api.indicators.ManualIndicatorType
import fr.sdis64.backend.indicators.entities.ManualIndicatorLevel
import fr.sdis64.backend.utilities.SetCrudRepository

interface ManualIndicatorLevelRepository : SetCrudRepository<ManualIndicatorLevel, Long> {
    fun findAllByCategoryType(type: ManualIndicatorType): Set<ManualIndicatorLevel>

    fun findAllByActive(active: Boolean): Set<ManualIndicatorLevel>

    fun findAllByActiveAndCategoryType(
        active: Boolean,
        type: ManualIndicatorType,
    ): Set<ManualIndicatorLevel>
}
