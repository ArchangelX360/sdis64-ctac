package fr.sdis64.api.indicators

import kotlinx.serialization.Serializable

@Serializable
data class GriffonIndicator(
    val level: String,
    val backgroundColor: String,
    val textColor: String,
)
