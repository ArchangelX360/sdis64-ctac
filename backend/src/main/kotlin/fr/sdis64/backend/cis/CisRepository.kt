package fr.sdis64.backend.cis

import fr.sdis64.backend.cis.entities.Cis
import fr.sdis64.backend.utilities.SetCrudRepository

interface CisRepository : SetCrudRepository<Cis, Long>
