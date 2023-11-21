package fr.sdis64.backend.operators

import fr.sdis64.backend.operators.entities.OperatorStatus
import fr.sdis64.backend.utilities.SetCrudRepository

interface OperatorStatusRepository : SetCrudRepository<OperatorStatus, Long>
