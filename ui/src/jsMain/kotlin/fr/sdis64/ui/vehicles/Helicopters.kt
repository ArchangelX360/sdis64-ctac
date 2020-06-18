package fr.sdis64.ui.vehicles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.client.CtacClient
import fr.sdis64.client.helicopters
import fr.sdis64.ui.utilities.Tile
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Helicopters(client: CtacClient) {
    val helicopters by rememberLoadingState { client.helicopters() }

    Tile("Hélicoptères") {
        loaderOr(helicopters) { allHelicopters ->
            Div(attrs = {
                style {
                    display(DisplayStyle.Grid)
                    // Helicopters are organised in a grid tied to the geographical position of their landing pad
                    gridTemplateAreas(
                        "HSA DRAGON",
                        "ECU CHOUCA"
                    )
                    gridAutoColumns(50.percent.toString())
                    gridAutoRows(50.percent.toString())
                    height(100.percent)
                    justifyContent(JustifyContent.Center)
                    paddingRight(10.px)
                    paddingLeft(10.px)
                }
            }) {
                allHelicopters.forEach {
                    VehicleChip(it) {
                        gridArea(it.name)
                        margin(5.px)
                    }
                }
            }
        }
    }
}
