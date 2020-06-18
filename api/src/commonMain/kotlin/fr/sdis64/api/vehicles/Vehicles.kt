package fr.sdis64.api.vehicles

import fr.sdis64.api.Identified
import fr.sdis64.api.DisplayOption
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class VehicleState(
    val systelName: String,
) {
    ARMABLE("ARMABLE"),
    NON_ARMABLE("NON ARMABLE"),
    UNAVAILABLE("INDISPONIBLE");

    companion object {
        fun create(systelName: String): VehicleState? = values().find { it.systelName == systelName }
    }
}

@Serializable
data class VehicleFunction(
    val type: VehicleType,
    val status: VehicleStatus,
    val state: VehicleState,
)

@Serializable
data class Vehicle(
    val name: String,
    val order: Int,
    val cis: Cis,
    val primaryFunction: VehicleFunction,
    val secondaryFunctions: Set<VehicleFunction>,
) : Comparable<Vehicle> {
    val functions = setOf(primaryFunction) + secondaryFunctions

    val displayable: Boolean
        get() = cis.displayOption.displayable
            && primaryFunction.type.displayOption.displayable
            && primaryFunction.status.isDisplayableFor(primaryFunction.type)

    override fun compareTo(other: Vehicle): Int {
        val cisComparaison = this.cis.compareTo(other.cis)
        return when {
            cisComparaison > 0 -> 1
            cisComparaison == 0 -> {
                val statusComparaison = this.primaryFunction.status.compareTo(other.primaryFunction.status)
                when {
                    statusComparaison > 0 -> 1
                    statusComparaison == 0 -> this.primaryFunction.type.compareTo(other.primaryFunction.type)
                    else -> -1
                }
            }
            else -> -1
        }
    }
}

@Serializable
data class Cis(
    override val id: Long?,
    val name: String,
    val code: String?,
    val displayOption: DisplayOption,
    val systelId: Long?,
) : Identified, Comparable<Cis> {

    override fun compareTo(other: Cis): Int = displayOption.compareTo(other.displayOption)
}

@Serializable
data class VehicleType(
    override val id: Long?,
    val name: String,
    var displayOption: DisplayOption,
) : Identified, Comparable<VehicleType> {

    override fun compareTo(other: VehicleType): Int = displayOption.compareTo(other.displayOption)
}

@Serializable
data class VehicleStatus(
    override val id: Long?,
    val name: String,
    val category: Category,
    val mode: Mode,
    val position: Int = 999,
    val backgroundColor: String = "FFFFFF",
    val textColor: String = "000000",
    val blacklist: Set<VehicleType> = emptySet(),
    val whitelist: Set<VehicleType> = emptySet(),
) : Identified, Comparable<VehicleStatus> {

    @Serializable
    enum class Mode {
        WHITELIST, BLACKLIST
    }

    @Serializable
    enum class Category {
        UNAVAILABLE, AVAILABLE, NON_ARMABLE
    }

    fun isDisplayableFor(type: VehicleType): Boolean = when (mode) {
        Mode.WHITELIST -> type in whitelist
        Mode.BLACKLIST -> type !in blacklist
    }

    override fun compareTo(other: VehicleStatus): Int = this.position.compareTo(other.position)
}

@Serializable
data class VehicleMap(
    override val id: Long?,
    val name: String,
    val types: Set<VehicleType> = emptySet(),
    /**
     * Degraded types associated with this map, to check when a vehicle is not available if is available in a degraded function
     */
    val degradedTypes: Set<VehicleType> = emptySet(),
) : Identified

@Serializable
data class VehicleDisplayMap(
    val mapName: String,
    val cisToAvailability: Map<String, Availability>,
)

@Serializable
data class Availability(
    var available: Int = 0,
    var nonArmable: Int = 0,
    var armableDegraded: Int = 0,
    var unavailable: Int = 0,
    var total: Int = 0,
)
