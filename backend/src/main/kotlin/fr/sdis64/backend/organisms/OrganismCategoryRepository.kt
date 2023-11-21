package fr.sdis64.backend.organisms

import fr.sdis64.backend.organisms.entities.OrganismCategory
import fr.sdis64.backend.utilities.SetCrudRepository

interface OrganismCategoryRepository : SetCrudRepository<OrganismCategory, Long>
