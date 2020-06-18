package fr.sdis64.ui.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.client.poll
import fr.sdis64.ui.utilities.Tile
import fr.sdis64.ui.utilities.hexCodeToRgb
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun RainMap() {
    val mapUrl by rememberLoadingState { refreshedMapAsset("ventusky_rain.webp") }

    Tile("Précipitations") {
        GenericMapContainer {
            loaderOr(mapUrl) {
                Img(src = it, alt = "Carte des précipitations")
                RainMapScale()
            }
        }
    }
}

@Composable
fun RainMapScale() {
    MapScale {
        MapScaleBlock(text = "mm", color = Color.black, backgroundColor = Color.white)
        MapScaleBlock(text = "50", color = Color.white, backgroundColor = "#541229".hexCodeToRgb())
        MapScaleBlock(text = "40", color = Color.white, backgroundColor = "#93174e".hexCodeToRgb())
        MapScaleBlock(text = "30", color = Color.white, backgroundColor = "#b53b66".hexCodeToRgb())
        MapScaleBlock(text = "20", color = Color.white, backgroundColor = "#d25f5e".hexCodeToRgb())
        MapScaleBlock(text = "15", color = Color.black, backgroundColor = "#d9784c".hexCodeToRgb())
        MapScaleBlock(text = "10", color = Color.black, backgroundColor = "#da9d45".hexCodeToRgb())
        MapScaleBlock(text = "8", color = Color.black, backgroundColor = "#dbb441".hexCodeToRgb())
        MapScaleBlock(text = "6", color = Color.black, backgroundColor = "#ced943".hexCodeToRgb())
        MapScaleBlock(text = "4", color = Color.black, backgroundColor = "#92cd4e".hexCodeToRgb())
        MapScaleBlock(text = "2", color = Color.black, backgroundColor = "#54b562".hexCodeToRgb())
        MapScaleBlock(text = "1", color = Color.white, backgroundColor = "#499bab".hexCodeToRgb())
        MapScaleBlock(text = "0.5", color = Color.white, backgroundColor = "#4a67ab".hexCodeToRgb())
        MapScaleBlock(text = "0.2", color = Color.white, backgroundColor = "#5d5c8e".hexCodeToRgb())
        MapScaleBlock(text = "0.1", color = Color.white, backgroundColor = "#5c5973".hexCodeToRgb())
        MapScaleBlock(text = "0", color = Color.white, backgroundColor = "#7d7d7d".hexCodeToRgb())
    }
}

@Composable
fun WindMap() {
    val mapUrl by rememberLoadingState { refreshedMapAsset("ventusky_wind.webp") }

    Tile("Vents") {
        GenericMapContainer {
            loaderOr(mapUrl) {
                Img(src = it, alt = "Carte des vents")
                WindMapScale()
            }
        }
    }
}

@Composable
fun WindMapScale() {
    MapScale {
        MapScaleBlock(text = "km/h", color = Color.black, backgroundColor = Color.white)
        MapScaleBlock(text = "140", color = Color.white, backgroundColor = "#2A0000".hexCodeToRgb())
        MapScaleBlock(text = "130", color = Color.white, backgroundColor = "#5F1017".hexCodeToRgb())
        MapScaleBlock(text = "120", color = Color.white, backgroundColor = "#8F0545".hexCodeToRgb())
        MapScaleBlock(text = "110", color = Color.white, backgroundColor = "#A62351".hexCodeToRgb())
        MapScaleBlock(text = "100", color = Color.white, backgroundColor = "#D02E65".hexCodeToRgb())
        MapScaleBlock(text = "90", color = Color.black, backgroundColor = "#E16639".hexCodeToRgb())
        MapScaleBlock(text = "80", color = Color.black, backgroundColor = "#DC8C21".hexCodeToRgb())
        MapScaleBlock(text = "70", color = Color.black, backgroundColor = "#D8B700".hexCodeToRgb())
        MapScaleBlock(text = "60", color = Color.black, backgroundColor = "#C2D602".hexCodeToRgb())
        MapScaleBlock(text = "50", color = Color.black, backgroundColor = "#6FC51A".hexCodeToRgb())
        MapScaleBlock(text = "40", color = Color.black, backgroundColor = "#03BA20".hexCodeToRgb())
        MapScaleBlock(text = "30", color = Color.white, backgroundColor = "#00A383".hexCodeToRgb())
        MapScaleBlock(text = "20", color = Color.white, backgroundColor = "#1978B5".hexCodeToRgb())
        MapScaleBlock(text = "10", color = Color.white, backgroundColor = "#4A4EA9".hexCodeToRgb())
        MapScaleBlock(text = "0", color = Color.white, backgroundColor = "#554E6F".hexCodeToRgb())
    }
}

@Composable
fun MapScale(block: @Composable () -> Unit) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            width(40.px)
            height(294.px)
        }
    }) {
        block()
    }
}

@Composable
fun MapScaleBlock(text: String, color: CSSColorValue, backgroundColor: CSSColorValue) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            flexGrow(1)
            backgroundColor(backgroundColor)
            color(color)
            fontSize(15.px)
            fontWeight(600)
        }
    }) { Text(text) }
}

@Composable
fun VigicruesMap() {
    val mapUrl by rememberLoadingState { refreshedMapAsset("vigicrues.png") }

    Tile("Vigicrues") {
        GenericMapContainer {
            loaderOr(mapUrl) {
                Img(src = it, alt = "Carte des crues")
            }
        }
    }
}

@Composable
fun InterventionMap(alignedRight: Boolean = false) {
    val map =
        "https://carto.sdis64.fr/waags/rest/services/Interne_SIG/Interventions_en_cours_SDIS64/MapServer/export?" +
                "dpi=180" +
                "&transparent=true" +
                "&format=png32" +
                // '&layers=show%3A0' +
                "&bbox=-199310.524891343%2C5278095.162666428%2C3782.6592620965966%2C5403451.889053947" +
                "&bboxSR=102100" +
                "&imageSR=102100" +
                "&size=2049%2C1265" +
                "&f=image"
    val mapUrl by rememberLoadingState { refreshAsset(map) }

    Tile {
        Div(attrs = {
            style {
                position(Position.Relative)
                display(DisplayStyle.Flex)
                justifyContent(if (alignedRight) JustifyContent.Right else JustifyContent.Center)
                height(100.percent)
                width(100.percent)
            }
        }) {
            Img(src = staticMapsAssetUrl("interventions_map_background.webp"),
                alt = "Fond de carte des interventions",
                attrs = {
                    style {
                        position(Position.Absolute)
                        maxWidth(100.percent)
                        maxHeight(100.percent)
                        property("z-index", "0")
                    }
                })
            loaderOr(mapUrl) {
                Img(src = it, alt = "Carte des interventions", attrs = {
                    style {
                        position(Position.Absolute)
                        maxWidth(100.percent)
                        maxHeight(100.percent)
                        property("z-index", "1")
                    }
                })
            }
        }
    }
}

@Composable
fun MaregrammeMap() {
    val mapUrl by rememberLoadingState { refreshedMapAsset("maregramme.svg") }

    Tile("Marégramme") {
        GenericMapContainer {
            loaderOr(mapUrl) {
                Img(src = it, alt = "Maregramme")
            }
        }
    }
}

@Composable
fun GenericMapContainer(block: @Composable () -> Unit) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.Center)
            height(100.percent)
            width(100.percent)
        }
    }) {
        block()
    }
}

fun refreshAsset(url: String, pollingDuration: Duration = 30.seconds) = poll(pollingDuration) {
    val rand = Random.nextInt(0, 10000)
    if (url.contains("?")) {
        "${url}&rand=${rand}"
    } else {
        "${url}?rand=${rand}"
    }
}

private fun refreshedMapAsset(name: String, pollingDuration: Duration = 30.seconds) =
    refreshAsset("/maps/${name}", pollingDuration)

private fun staticMapsAssetUrl(name: String) = "/maps-static/${name}"
