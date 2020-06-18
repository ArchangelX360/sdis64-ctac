package fr.sdis64.brain.indicators.entities

import fr.sdis64.api.indicators.ManualIndicatorType
import fr.sdis64.brain.utilities.entities.Identified
import jakarta.persistence.*

@Entity
data class ManualIndicatorLevel(
    val name: String,
    @ManyToOne(cascade = [CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH]) val category: ManualIndicatorCategory,
    @ElementCollection(fetch = FetchType.EAGER) val descriptions: Set<String> = emptySet(),
    val active: Boolean = false,
) : Identified()

@Entity
data class ManualIndicatorCategory(
    val name: String,
    @Enumerated(EnumType.STRING) val type: ManualIndicatorType,
) : Identified()
