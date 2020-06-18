package fr.sdis64.brain.cis

import fr.sdis64.brain.cis.entities.Cis
import fr.sdis64.brain.utilities.SetCrudRepository

interface CisRepository : SetCrudRepository<Cis, Long>
