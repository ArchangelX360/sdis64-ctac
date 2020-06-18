package fr.sdis64.ui.indicators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.api.indicators.GriffonIndicator
import fr.sdis64.api.indicators.ManualIndicatorLevel
import fr.sdis64.api.indicators.WeatherIndicator
import fr.sdis64.api.indicators.isAlerting
import fr.sdis64.client.CtacClient
import fr.sdis64.client.activeManualIndicators
import fr.sdis64.client.griffonIndicator
import fr.sdis64.client.weatherIndicators
import fr.sdis64.ui.utilities.Tile
import fr.sdis64.ui.utilities.hexCodeToRgb
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import org.jetbrains.compose.web.ExperimentalComposeWebSvgApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.svg.*

@Composable
fun Indicators(client: CtacClient) {
    val manualIndicators by rememberLoadingState { client.activeManualIndicators() }
    val weatherIndicator by rememberLoadingState { client.weatherIndicators() }
    val griffonIndicator by rememberLoadingState { client.griffonIndicator() }

    Tile("Indicateurs") {
        IndicatorList {
            loaderOr(griffonIndicator) {
                IndicatorItem(it.toDisplayableIndicator())
            }
            loaderOr(weatherIndicator) { indicators ->
                indicators.filter { it.isAlerting }.map { it.toDisplayableIndicator() }.forEach {
                    IndicatorItem(it)
                }
            }
            loaderOr(manualIndicators) { indicators ->
                indicators.mapNotNull { it.toDisplayableIndicator() }.forEach {
                    IndicatorItem(it)
                }
            }
        }
    }
}

private val maxIndicatorRowHeight = 48.px

@Composable
private fun IndicatorItem(entity: DisplayableIndicator) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            height(maxIndicatorRowHeight)
            alignItems(AlignItems.Center)
            padding(5.px)
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                alignItems(AlignItems.Center)
                width(190.px)
                height(maxIndicatorRowHeight)
            }
        }) {
            entity.visual()
        }
        Div(attrs = {
            style {
                paddingLeft(10.px)
            }
        }) { Text(entity.legend) }
    }
}

@Composable
private fun IndicatorList(block: @Composable () -> Unit) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
        }
    }) {
        block()
    }
}

data class DisplayableIndicator(
    val legend: String,
    val visual: @Composable () -> Unit,
)

fun ManualIndicatorLevel.toDisplayableIndicator(): DisplayableIndicator? {
    val visual = toVisual() ?: return null
    return DisplayableIndicator(
        legend = category.type.displayName,
        visual = visual,
    )
}

private fun ManualIndicatorLevel.toVisual(): (@Composable () -> Unit)? = when (id) {
    1L -> null
    2L -> {
        { VigipirateVigilance() }
    }

    3L -> {
        { VigipirateAlerteAttentat() }
    }

    4L -> null
    5L -> {
        { Circle(color = Color.orange, diameter = maxIndicatorRowHeight) }
    }

    6L -> {
        { Circle(color = Color.red, diameter = maxIndicatorRowHeight) }
    }

    else -> error("unexpected manual indicator id '$id'")
}

@OptIn(ExperimentalComposeWebSvgApi::class)
@Composable
private fun Circle(color: CSSColorValue, diameter: CSSpxValue) {
    Svg(attrs = {
        width(diameter.value)
        height(diameter.value)
    }) {
        val half = diameter.value / 2
        Circle(half, half, half, attrs = {
            fill(color.toString())
        })
    }
}

private fun WeatherIndicator.toDisplayableIndicator() = DisplayableIndicator(
    legend = type.label,
    visual = { WeatherIndicatorVisual(this) },
)

@Composable
private fun WeatherIndicatorVisual(i: WeatherIndicator) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            backgroundColor(Color(i.level.color))
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(Color.black)
            }
        }
    }) {
        weatherPictogram(i.type.value)
    }
}

private fun GriffonIndicator.toDisplayableIndicator() = DisplayableIndicator(
    legend = "Feux de végétaux",
    visual = { GriffonIndicatorChip(this) },
)

@Composable
private fun GriffonIndicatorChip(i: GriffonIndicator) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            width(100.percent)
            borderRadius(5.px)
            margin(5.px)
            padding(5.px)
            backgroundColor(i.backgroundColor.hexCodeToRgb())
            color(i.textColor.hexCodeToRgb())
        }
    }) {
        Text(i.level)
    }
}
