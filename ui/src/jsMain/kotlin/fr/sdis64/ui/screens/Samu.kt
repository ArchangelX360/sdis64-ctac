package fr.sdis64.ui.screens

import androidx.compose.runtime.Composable
import fr.sdis64.client.CtacClient
import fr.sdis64.ui.vehicles.CisRegionAvailabilityColor
import fr.sdis64.ui.vehicles.VehicleMapLegend
import fr.sdis64.ui.vehicles.VlsmVehicleMap
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun Samu(client: CtacClient) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
        }
    }) {
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                alignItems(AlignItems.Center)
            }
        }) {
            Img(
                attrs = {
                    style {
                        maxWidth(218.px)
                    }
                },
                src = "/logo-sdis64.png",
                alt = "SDIS64 Logo",
            )
            H1(attrs = {
                style {
                    fontSize(40.px)
                }
            }) {
                Text("Disponibilité en temps réel des moyens SSSM du SDIS64")
            }
        }
        Div(attrs = {
            style {
                width(800.px)
                alignItems(AlignItems.FlexStart)
            }
        }) {
            VlsmVehicleMap(client, showMapName = false)
            VehicleMapLegend(hideLegendTileOf = setOf(CisRegionAvailabilityColor.NONE_LEFT_BUT_DEGRADED_AVAILABLE))
        }
    }
}
