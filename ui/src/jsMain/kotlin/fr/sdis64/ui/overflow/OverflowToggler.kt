package fr.sdis64.ui.overflow

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button

@Composable
fun FullscreenOverflowToggler() {
    var overflowHidden by remember { mutableStateOf(true) }

    Button(attrs = {
        attr("aria-label", "Toggle overflow display")
        style {
            position(Position.Absolute)
            height(200.px)
            width(200.px)
            border {
                style(LineStyle.None)
            }
            backgroundColor(Color.transparent) // hidden
        }
        onClick {
            overflowHidden = !overflowHidden
        }
    })

    if (overflowHidden) {
        Style(BodyOverflowHiddenStylesheet)
    }
}

private object BodyOverflowHiddenStylesheet : StyleSheet() {
    init {
        "body" style {
            overflow("hidden")
        }
    }
}
