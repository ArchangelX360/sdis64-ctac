package fr.sdis64.backend.vehicles.entities

import fr.sdis64.backend.utilities.entities.Identified
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
data class VehicleMap(
    @Column(unique = true) val name: String,
    @OneToMany(cascade = [CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH]) val types: Set<VehicleType> = emptySet(),
    /**
     * Degraded types associated with this map, to check when a vehicle is not available if is available in a degraded function
     */
    @OneToMany(cascade = [CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH]) val degradedTypes: Set<VehicleType> = emptySet(),
) : Identified()
