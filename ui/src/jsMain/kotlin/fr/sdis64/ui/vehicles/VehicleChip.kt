package fr.sdis64.ui.vehicles

import androidx.compose.runtime.Composable
import fr.sdis64.api.vehicles.Vehicle
import fr.sdis64.ui.utilities.hexCodeToRgb
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun VehicleChip(v: Vehicle, additionalStyle: StyleScope.() -> Unit = {}) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                alignItems(AlignItems.Center)
                color(v.primaryFunction.status.textColor.hexCodeToRgb())
                backgroundColor(v.primaryFunction.status.backgroundColor.hexCodeToRgb())
                minWidth(150.px)
                borderRadius(5.px)
                paddingLeft(5.px)
                paddingRight(5.px)
                additionalStyle.invoke(this)
            }
        }
    ) {
        Text("${v.name} ${v.order}")
    }
}
