package fr.sdis64.brain.operators

import fr.sdis64.brain.operators.entities.OperatorStatus
import fr.sdis64.brain.utilities.SetCrudRepository

interface OperatorStatusRepository : SetCrudRepository<OperatorStatus, Long>
