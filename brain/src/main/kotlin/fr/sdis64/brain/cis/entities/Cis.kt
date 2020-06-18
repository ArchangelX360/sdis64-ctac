package fr.sdis64.brain.cis.entities

import fr.sdis64.brain.utilities.entities.Identified
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Entity
data class Cis(
    @field:NotBlank val name: String,
    val code: String? = null,
    val systelId: Long? = null,
    @Column val displayToCta: Boolean = false,
    @Column val displayToCodis: Boolean = false,
    @Column @field:Min(0) val displayPosition: Int = 999,
) : Identified()
