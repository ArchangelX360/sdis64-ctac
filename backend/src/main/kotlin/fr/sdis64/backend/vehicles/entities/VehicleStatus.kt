package fr.sdis64.backend.vehicles.entities

import com.fasterxml.jackson.annotation.JsonManagedReference
import fr.sdis64.backend.utilities.entities.Identified
import jakarta.persistence.*
import fr.sdis64.api.vehicles.VehicleStatus as VehicleStatusDTO

@Entity
data class VehicleStatus(
    @Column(unique = true) val name: String,
    @Enumerated(EnumType.STRING) val category: VehicleStatusDTO.Category,
    @Enumerated(EnumType.STRING) val mode: VehicleStatusDTO.Mode,
    val position: Int = 999,
    val backgroundColor: String = "FFFFFF",
    val textColor: String = "000000",
    @ManyToMany
    @JoinTable(
        name = "vehicle_status_blacklists",
        joinColumns = [JoinColumn(name = "vehicle_status_id")],
        inverseJoinColumns = [JoinColumn(name = "vehicle_type_id")]
    )
    @JsonManagedReference val blacklist: Set<VehicleType> = emptySet(),
    @ManyToMany
    @JoinTable(
        name = "vehicle_status_whitelists",
        joinColumns = [JoinColumn(name = "vehicle_status_id")],
        inverseJoinColumns = [JoinColumn(name = "vehicle_type_id")]
    )
    @JsonManagedReference val whitelist: Set<VehicleType> = emptySet(),
) : Identified()
