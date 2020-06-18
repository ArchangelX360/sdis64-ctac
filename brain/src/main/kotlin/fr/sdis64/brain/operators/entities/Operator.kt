package fr.sdis64.brain.operators.entities

import fr.sdis64.brain.utilities.entities.Identified
import jakarta.persistence.Entity

@Entity
data class OperatorPhoneNumber(
    val systelNumber: String,
    val realNumber: String,
) : Identified()

@Entity
data class OperatorStatus(
    val name: String,
    val backgroundColor: String? = "#000000",
    val textColor: String? = "#FFFFFF",
    val displayed: Boolean = false,
) : Identified()
