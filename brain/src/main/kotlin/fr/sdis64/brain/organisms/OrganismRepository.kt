package fr.sdis64.brain.organisms

import fr.sdis64.brain.organisms.entities.Organism
import fr.sdis64.brain.utilities.SetCrudRepository

interface OrganismRepository : SetCrudRepository<Organism, Long> {
    fun findAllByCategoryId(categoryId: Long?): Set<Organism>
}
