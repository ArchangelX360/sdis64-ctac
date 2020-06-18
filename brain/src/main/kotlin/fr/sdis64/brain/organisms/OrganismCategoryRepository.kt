package fr.sdis64.brain.organisms

import fr.sdis64.brain.organisms.entities.OrganismCategory
import fr.sdis64.brain.utilities.SetCrudRepository

interface OrganismCategoryRepository : SetCrudRepository<OrganismCategory, Long>
