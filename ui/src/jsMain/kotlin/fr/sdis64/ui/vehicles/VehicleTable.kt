package fr.sdis64.ui.vehicles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import fr.sdis64.api.vehicles.Vehicle
import fr.sdis64.client.CtacClient
import fr.sdis64.client.displayableVehicles
import fr.sdis64.ui.utilities.TableStylesheet
import fr.sdis64.ui.utilities.Tile
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun VehicleTable(client: CtacClient, showHeaders: Boolean = false) {
    val vehicles by rememberLoadingState { client.displayableVehicles() }

    Tile("Véhicules", padded = true) {
        loaderOr(vehicles) {
            Table(
                attrs = {
                    style {
                        width(100.percent)
                    }
                    classes(TableStylesheet.darkBorderedTable, TableStylesheet.mediumRowTable)
                }
            ) {
                Thead(
                    attrs = {
                        style {
                            if (!showHeaders) {
                                display(DisplayStyle.None)
                            }
                        }
                    },
                ) {
                    Tr {
                        Th { Text("CIS") }
                        Th { Text("Véhicules en intervention") }
                    }
                }
                Tbody {
                    it
                        .groupBy { it.cis.name }
                        .forEach { (cis, vehicles) ->
                            key(cis) {
                                Tr {
                                    Td(
                                        attrs = {
                                            style {
                                                textAlign("center")
                                                paddingLeft(10.px)
                                                paddingRight(10.px)
                                                property("white-space", "nowrap")
                                            }
                                        }
                                    ) { Text(cis) }
                                    Td(attrs = {
                                        style {
                                            width(100.percent) // take as much space as possible in the table
                                        }
                                    }) {
                                        Div(
                                            attrs = {
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    flexDirection(FlexDirection.Row)
                                                    justifyContent(JustifyContent.FlexStart)
                                                    flexWrap(FlexWrap.Wrap)
                                                }
                                            }
                                        ) {
                                            vehicles.forEach {
                                                key(it.key) {
                                                    VehicleChip(it) {
                                                        margin(1.px)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }
    }
}

private val Vehicle.key: String
    get() = "${this.cis.id}-${this.name}-${this.order}"
