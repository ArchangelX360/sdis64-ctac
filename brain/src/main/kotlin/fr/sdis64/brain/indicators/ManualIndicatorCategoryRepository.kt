package fr.sdis64.brain.indicators

import fr.sdis64.brain.indicators.entities.ManualIndicatorCategory
import fr.sdis64.brain.utilities.SetCrudRepository

interface ManualIndicatorCategoryRepository : SetCrudRepository<ManualIndicatorCategory, Long>
