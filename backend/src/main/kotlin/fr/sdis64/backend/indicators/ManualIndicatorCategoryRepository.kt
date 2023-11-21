package fr.sdis64.backend.indicators

import fr.sdis64.backend.indicators.entities.ManualIndicatorCategory
import fr.sdis64.backend.utilities.SetCrudRepository

interface ManualIndicatorCategoryRepository : SetCrudRepository<ManualIndicatorCategory, Long>
