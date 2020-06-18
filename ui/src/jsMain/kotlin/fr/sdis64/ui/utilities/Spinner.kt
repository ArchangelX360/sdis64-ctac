package fr.sdis64.ui.utilities

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Spinner() {
    Style(SpinnerStyleSheet)
    Div(
        attrs = {
            classes(SpinnerStyleSheet.spinner)
        }
    )
}

object SpinnerStyleSheet : StyleSheet() {
    private val spinnerFrames by keyframes {
        0.percent {
            property("transform", "rotate(0deg)")
        }
        100.percent {
            property("transform", "rotate(360deg)")
        }
    }

    val spinner by style {
        display(DisplayStyle.InlineBlock)
        width(80.px)
        height(80.px)
        property("margin", "auto")
        (self + after) {
            property("content", "\" \"")
            display(DisplayStyle.Block)
            width(64.px)
            height(64.px)
            margin(8.px)
            borderRadius(50.percent)
            border {
                color = Color.white
                width = 6.px
                style = LineStyle.Solid
            }
            property("border-color", "#fff transparent #fff transparent")
            animation(spinnerFrames) {
                duration = listOf(1.2.s)
                iterationCount = listOf(null)
                timingFunction = listOf(AnimationTimingFunction.Linear)
            }
        }
    }
}
