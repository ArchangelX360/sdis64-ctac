package fr.sdis64.ui.utilities

import androidx.compose.runtime.Composable
import fr.sdis64.ui.DarkThemeStylesheet
import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import kotlin.math.min

@OptIn(ExperimentalComposeWebApi::class)
@Composable
fun Gauge(current: Double, max: Double) {
    Style(GaugeStyleSheet)

    val percentageAdjustedValue = min(current * 100 / max, 100.0)

    val colorClass = when {
        percentageAdjustedValue < 5 -> GaugeStyleSheet.cyan
        percentageAdjustedValue < 22.5 -> GaugeStyleSheet.teal
        percentageAdjustedValue < 32.5 -> GaugeStyleSheet.green
        percentageAdjustedValue < 40 -> GaugeStyleSheet.yellow
        else -> GaugeStyleSheet.red
    }

    Div(
        attrs = {
            style {
                width(100.px)
                width(100.px)
            }
        }
    ) {
        Div(
            attrs = {
                classes(GaugeStyleSheet.gauge, GaugeStyleSheet.gaugeArc)
            }
        ) {
            Div(
                attrs = {
                    classes(GaugeStyleSheet.gaugeMeter, GaugeStyleSheet.gaugeArc, colorClass)
                    style {
                        transform { rotate((0.5 + (percentageAdjustedValue / 200)).turn) }
                    }
                }
            )
        }
    }
}

private object GaugeStyleSheet : StyleSheet() {
    val cyan by beforeBackgroundColor("#42f3ea".hexCodeToRgb())
    val teal by beforeBackgroundColor("#3BBFB6".hexCodeToRgb())
    val green by beforeBackgroundColor("#55BF3B".hexCodeToRgb())
    val yellow by beforeBackgroundColor("#DDDF0D".hexCodeToRgb())
    val red by beforeBackgroundColor("#DF5353".hexCodeToRgb())

    private fun beforeBackgroundColor(cssColorValue: CSSColorValue) = style {
        (self + before) {
            backgroundColor(cssColorValue)
        }
    }

    val gaugeMeter by style {
        position(Position.Absolute)
        height(100.percent)
        width(100.percent)
        overflow("hidden")
        property("transform-origin", "center bottom")
    }

    val gaugeArc by style {
        (self + before) {
            property("content", "\" \"")
            position(Position.Absolute)
            height(200.percent)
            width(100.percent)
            borderRadius(50.percent)
        }
    }

    val gauge by style {
        paddingBottom(50.percent)
        position(Position.Relative)
        width(100.percent)
        height(0.percent)
        overflow("hidden")

        (self + before) {
            backgroundColor(Color.white)
        }

        // cut inner circle
        (self + after) {
            property("content", "\" \"")
            backgroundColor(DarkThemeStylesheet.backgroundColor)
            position(Position.Absolute)
            top(60.percent)
            right(30.percent)
            bottom((-40).percent)
            left(30.percent)
            borderRadius(50.percent)
        }
    }
}
