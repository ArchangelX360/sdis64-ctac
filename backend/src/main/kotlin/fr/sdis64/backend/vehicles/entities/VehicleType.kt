package fr.sdis64.backend.vehicles.entities

import fr.sdis64.backend.utilities.entities.Identified
import jakarta.persistence.*
import jakarta.validation.constraints.Min

@Entity
data class VehicleType(
    @Column(unique = true) val name: String,
    @Column val displayToCta: Boolean = false,
    @Column val displayToCodis: Boolean = false,
    @Column @field:Min(0) val displayPosition: Int = 999,
) : Identified() {

    @ManyToMany(mappedBy = "blacklist")
    @Transient
    var blacklistedFor: Set<VehicleStatus> = emptySet()

    @ManyToMany(mappedBy = "whitelist")
    @Transient
    var whitelistedFor: Set<VehicleStatus> = emptySet()
}
