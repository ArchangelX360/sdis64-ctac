package fr.sdis64.api.vehicles

import kotlinx.serialization.Serializable

@Serializable
sealed class HelicopterPosition {

    object Unknown : HelicopterPosition()

    @Serializable
    data class Known(
        val id: String,
        val alias: String,
        val latitude: Float,
        val longitude: Float,
    ) : HelicopterPosition()
}
