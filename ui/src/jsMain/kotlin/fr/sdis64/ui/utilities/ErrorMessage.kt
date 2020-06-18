package fr.sdis64.ui.utilities

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun ErrorMessage(value: String) {
    P(attrs = {
        style {
            color(Color.red)
            fontWeight("bold")
        }
    }) {
        Text(value)
    }
}