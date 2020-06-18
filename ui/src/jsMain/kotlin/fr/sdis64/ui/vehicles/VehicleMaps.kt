package fr.sdis64.ui.vehicles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.api.vehicles.Availability
import fr.sdis64.client.CtacClient
import fr.sdis64.client.vehiclesMap
import fr.sdis64.ui.maps.GenericMapContainer
import fr.sdis64.ui.maps.refreshAsset
import fr.sdis64.ui.utilities.Tile
import fr.sdis64.ui.utilities.hexCodeToRgb
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlin.time.Duration.Companion.seconds

@Composable
fun VsavVehicleMap(client: CtacClient) {
    VehicleMap(client, "VSAV")
}

@Composable
fun IncVehicleMap(client: CtacClient) {
    VehicleMap(client, "INC")
}

@Composable
fun FenVehicleMap(client: CtacClient) {
    VehicleMap(client, "FEN")
}

@Composable
fun VlsmVehicleMap(client: CtacClient, showMapName: Boolean = true) {
    VehicleMap(client, "VLSM", showMapName)
}

@Composable
fun VsrmVehicleMap(client: CtacClient) {
    VehicleMap(client, "VSRM")
}

@Composable
fun CarencesMap() {
    val map = "https://carto.sdis64.fr/waags/rest/services/Carences_ambulancieres_secteurs_CIS/MapServer/export?" +
            "dpi=180" +
            "&transparent=true" +
            "&format=png32" +
            "&bbox=-199310.524891343%2C5278095.162666428%2C3782.6592620965966%2C5403451.889053947" +
            "&bboxSR=102100" +
            "&imageSR=102100" +
            "&size=2657%2C1640" +
            "&f=image"
    val mapUrl by rememberLoadingState { refreshAsset(url = map, pollingDuration = 15.seconds) }

    Tile {
        loaderOr(mapUrl) {
            GenericMapContainer {
                Img(src = it, alt = "Carte des carences", attrs = {
                    style {
                        height(100.percent)
                    }
                })
            }
            MapName("CARENCES")
        }
    }
}

@Composable
fun VehicleMapLegend(hideLegendTileOf: Set<CisRegionAvailabilityColor>) {
    Table(attrs = {
        style {
            width(100.percent)
            textAlign("center")
        }
    }) {
        Tbody {
            Tr {
                CisRegionAvailabilityColor
                    .values()
                    .filter { !hideLegendTileOf.contains(it) }
                    .forEach {
                        Td(attrs = {
                            style {
                                minWidth(150.px)
                                height(50.px)
                                fontWeight(500)
                                color(Color.black)
                                backgroundColor(it.cssBackgroundColor)
                                border {
                                    width(1.px)
                                    style(LineStyle.Solid)
                                }
                            }
                        }) {
                            Text(it.legendName)
                        }
                    }
            }
        }
    }
}

enum class CisRegionAvailabilityColor(val svgFillColor: String, val legendName: String) {
    MOST_AVAILABLE(svgFillColor = "#00FF00", legendName = "Disponible"),
    ONE_LEFT(svgFillColor = "#FFFF00", legendName = "Reste un seul moyen disponible"),
    NONE_LEFT(svgFillColor = "#FF0000", legendName = "Engagé"),
    NONE_LEFT_BUT_DEGRADED_AVAILABLE(svgFillColor = "#00FFCC", legendName = "Disponible en fonction secondaire"),
    NONE_LEFT_AND_DEGRADED_NOT_AVAILABLE(svgFillColor = "#A6A6A6", legendName = "Indisponible"),
    NO_VEHICLE(svgFillColor = "none", legendName = "Pas de moyen");

    val cssBackgroundColor: CSSColorValue
        get() = if (this.svgFillColor == "none") Color.transparent else this.svgFillColor.hexCodeToRgb()
}

@Composable
private fun VehicleMap(
    client: CtacClient,
    mapName: String,
    showMapName: Boolean = true,
) {
    val vehicleMap by rememberLoadingState { client.vehiclesMap(mapName) }

    Tile {
        loaderOr(vehicleMap) { map ->
            val cisToColor = map.cisToAvailability.map { (cis, availability) ->
                cis to availability.toColor().svgFillColor
            }.toMap()

            VehicleMapSvg(cisToColor)
            if (showMapName) {
                MapName(map.mapName)
            }
        }
    }
}

@Composable
private fun MapName(name: String) {
    Div(
        attrs = {
            style {
                property("float", "left")
                position(Position.Relative)
                bottom(75.px)
                left(75.px)
                fontSize(35.px)
                fontWeight(500)
            }
        }
    ) {
        Text(name)
    }
}

private fun Availability.toColor(): CisRegionAvailabilityColor {
    if (this.total == 0) {
        return CisRegionAvailabilityColor.NO_VEHICLE
    }

    if (this.available == 0) {
        if (this.nonArmable > 0) {
            if (this.armableDegraded > 0) {
                return CisRegionAvailabilityColor.NONE_LEFT_BUT_DEGRADED_AVAILABLE
            } else {
                return CisRegionAvailabilityColor.NONE_LEFT_AND_DEGRADED_NOT_AVAILABLE
            }
        } else {
            return CisRegionAvailabilityColor.NONE_LEFT
        }
    }

    if (this.available == 1 && this.total > 1) {
        return CisRegionAvailabilityColor.ONE_LEFT
    }

    return CisRegionAvailabilityColor.MOST_AVAILABLE
}
