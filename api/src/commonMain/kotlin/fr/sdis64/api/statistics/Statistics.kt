package fr.sdis64.api.statistics

import kotlinx.serialization.Serializable

@Serializable
data class CallStatistic(
    val label: String,
    val title: String?,
    val ongoing: Int?,
    val onhold: Int?,
    /**
     * Number of call this day from 7:00 to 7:00 the next day
     */
    val day: Int?,
    val year: Int?,
)

// FIXME: maybe this should be a regular data class, and we could access the keys manually when creating the instance
//  on the server
@Serializable
class CallStatistics(private val map: Map<String, CallStatistic>) {
    val emergency: CallStatistic by map
    val operational: CallStatistic by map
    val ecobuage: CallStatistic by map
    val out: CallStatistic by map
    val inOut: CallStatistic by map
}

@Serializable
data class InterventionStatistic(
    val label: String,
    val title: String,
    val ongoing: Int?,
    /**
     * Number of intervention this day from 7:00 to 7:00 the next day
     */
    val day: Int?,
    val year: Int?,
)

// FIXME: maybe this should be a regular data class, and we could access the keys manually when creating the instance
//  on the server
@Serializable
class InterventionStatistics(private val map: Map<String, InterventionStatistic>) {
    val sap: InterventionStatistic by map
    val avp: InterventionStatistic by map
    val inc: InterventionStatistic by map
    val div: InterventionStatistic by map
    val rt: InterventionStatistic by map
}
