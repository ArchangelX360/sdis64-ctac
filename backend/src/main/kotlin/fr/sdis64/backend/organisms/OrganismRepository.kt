package fr.sdis64.backend.organisms

import fr.sdis64.backend.organisms.entities.Organism
import fr.sdis64.backend.utilities.SetCrudRepository

interface OrganismRepository : SetCrudRepository<Organism, Long> {
    fun findAllByCategoryId(categoryId: Long?): Set<Organism>
}
