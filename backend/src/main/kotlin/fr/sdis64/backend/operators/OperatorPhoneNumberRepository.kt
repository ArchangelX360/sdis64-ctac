package fr.sdis64.backend.operators

import fr.sdis64.backend.operators.entities.OperatorPhoneNumber
import fr.sdis64.backend.utilities.SetCrudRepository

interface OperatorPhoneNumberRepository : SetCrudRepository<OperatorPhoneNumber, Long>
