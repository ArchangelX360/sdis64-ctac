package fr.sdis64.ui

import org.jetbrains.compose.web.css.*

object DarkThemeStylesheet : StyleSheet() {
    val backgroundColor = rgb(66, 66, 66)

    init {
        "body" style {
            backgroundColor(backgroundColor)
            color(Color.white)
        }
    }
}
