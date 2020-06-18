package fr.sdis64.brain.systel

import fr.sdis64.brain.utilities.mapToSet
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger(SystelVehicle::class.java)

fun SystelResponse.toSystelVehicles(): List<SystelVehicle> {
    return this.result
        .map {
            it.toSystelRawVehicle()
        }
        .groupBy { it.uniqueKey }
        .values
        .mapNotNull { it.toSystelVehicle() }
}

data class SystelVehicleFunction(
    val type: String,
    val status: String,
    val state: String,
)

data class SystelVehicle(
    val type: String,
    val order: String,
    val cis: String,
    val primaryFunction: SystelVehicleFunction,
    val secondaryFunctions: Set<SystelVehicleFunction>,
)

private fun List<RawSystelVehicle>.toSystelVehicle(): SystelVehicle? {
    val (primaryFunctions, secondaryFunctions) = this.partition { it.type == it.secondaryFunction }
    val primaryFunction = primaryFunctions.firstOrNull()
    if (primaryFunction == null) {
        val v = this.firstOrNull()?.uniqueKey ?: "unknown vehicle"
        LOG.error("ignoring Vehicle '${v}' due to missing primary function")
        return null
    }

    return SystelVehicle(
        type = primaryFunction.type,
        order = primaryFunction.order,
        cis = primaryFunction.cis,
        primaryFunction = SystelVehicleFunction(
            type = primaryFunction.type,
            status = primaryFunction.status,
            state = primaryFunction.state,
        ),
        secondaryFunctions = secondaryFunctions.mapToSet {
            SystelVehicleFunction(
                type = it.secondaryFunction,
                status = it.status,
                state = it.state,
            )
        }
    )
}

private data class RawSystelVehicle(
    val type: String,
    val order: String,
    val cis: String,
    val status: String,
    val secondaryFunction: String,
    val state: String,
) {
    /**
     * Systel represents vehicles as a weird join between a vehicle and a vehicle function.
     *
     * `uniqueKey` identifies a SystelVehicle as a real-world vehicle, whether the Systel
     * representation of this real-world vehicle is a primary or secondary function, it will output the same unique key.
     *
     * @return key that represents a real-world vehicle
     */
    val uniqueKey: String
        get() = "${type}-${cis}-${order}"
}

private fun List<String>.toSystelRawVehicle(): RawSystelVehicle {
    return RawSystelVehicle(
        type = this[0],
        order = this[1],
        cis = this[2],
        status = this[3],
        secondaryFunction = this[6],
        state = this[8],
    )
}
