package fr.sdis64.ui.utilities

import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.rgb

fun String.hexCodeToRgb(): CSSColorValue {
    val r: Int = this.substring(1, 3).toInt(16) // 16 for hex
    val g: Int = this.substring(3, 5).toInt(16) // 16 for hex
    val b: Int = this.substring(5, 7).toInt(16) // 16 for hex
    return rgb(r, g, b)
}
