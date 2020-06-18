package fr.sdis64.ui.organisms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.client.CtacClient
import fr.sdis64.client.activeOrganisms
import fr.sdis64.ui.utilities.Tile
import fr.sdis64.ui.utilities.hexCodeToRgb
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

const val mountainRescueId = 2L

@Composable
fun ActiveMountainRescueOrganisms(client: CtacClient) {
    val organims by rememberLoadingState { client.activeOrganisms(categoryId = mountainRescueId) }

    Tile("Secours Montagne") {
        loaderOr(organims) { orgs ->
            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        if (orgs.size > 1) justifyContent(JustifyContent.Center)
                        height(100.percent)
                    }
                }
            ) {
                orgs.forEach { o ->
                    // TODO: this should probably be part of the DB, like the other ones
                    val (backgroundColor, color) = when {
                        o.name.contains("PGHM") -> "#1f497d".hexCodeToRgb() to Color.white
                        o.name.contains("GSMSP") -> Color.red to Color.black
                        else -> Color.black to Color.white
                    }
                    Chip(o.name, backgroundColor, color)
                }
            }
        }
    }
}

@Composable
private fun Chip(value: String, backgroundColor: CSSColorValue, color: CSSColorValue) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                alignItems(AlignItems.Center)
                height(100.percent)
                backgroundColor(backgroundColor)
                color(color)
                margin(5.px)
                padding(5.px)
                borderRadius(5.px)
            }
        }
    ) {
        Text(value)
    }
}
