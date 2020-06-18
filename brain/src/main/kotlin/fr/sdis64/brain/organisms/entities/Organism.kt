package fr.sdis64.brain.organisms.entities

import fr.sdis64.brain.utilities.entities.Identified
import jakarta.persistence.*
import java.time.Instant

@Entity
data class Organism(
    val name: String,
    @ManyToOne(cascade = [CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH]) val category: OrganismCategory,
    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    ) @JoinColumn(name = "organism_id") val activeTimeWindows: Set<OrganismDuration>,
) : Identified()

@Entity
data class OrganismCategory(
    @Column(unique = true) val name: String,
) : Identified()

@Entity
data class OrganismDuration(
    val start: Instant,
    val end: Instant,
) : Identified()
