package fr.sdis64.ui.screens

import androidx.compose.runtime.Composable
import fr.sdis64.ui.maps.InterventionMap
import fr.sdis64.ui.utilities.GridTile
import fr.sdis64.ui.utilities.physicalTvHeight
import fr.sdis64.ui.utilities.physicalTvWidth
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.gridAutoColumns
import org.jetbrains.compose.web.css.gridAutoRows
import org.jetbrains.compose.web.dom.Div

@Composable
fun Crisis() {
    Div(attrs = {
        style {
            display(DisplayStyle.Grid)
            gridAutoRows(physicalTvHeight.toString())
            gridAutoColumns(physicalTvWidth.toString())
        }
    }) {
        GridTile(row = "1 / 1", column = "1 / 1") { InterventionMap() }
    }
}
