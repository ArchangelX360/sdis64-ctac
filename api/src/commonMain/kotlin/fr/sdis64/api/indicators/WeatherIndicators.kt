package fr.sdis64.api.indicators

import kotlinx.serialization.Serializable

@Serializable
data class WeatherIndicator(
    val type: WeatherIndicatorType,
    val level: WeatherIndicatorColor,
)

val WeatherIndicator.isAlerting: Boolean
    get() = level.value > 1

@Serializable
data class WeatherIndicatorColor(
    val color: String,
    val value: Int,
)

@Serializable
data class WeatherIndicatorType(
    val label: String,
    val value: Int,
)
