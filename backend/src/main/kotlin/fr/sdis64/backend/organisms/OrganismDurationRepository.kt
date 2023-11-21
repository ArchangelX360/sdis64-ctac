package fr.sdis64.backend.organisms

import fr.sdis64.backend.organisms.entities.OrganismDuration
import fr.sdis64.backend.utilities.SetCrudRepository

interface OrganismDurationRepository : SetCrudRepository<OrganismDuration, Long>
