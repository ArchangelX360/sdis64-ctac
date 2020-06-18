package fr.sdis64.ui.admin

import androidx.compose.runtime.*
import fr.sdis64.api.indicators.ManualIndicatorLevel
import fr.sdis64.api.indicators.ManualIndicatorType
import fr.sdis64.client.CtacClient
import fr.sdis64.client.triggerablePoll
import fr.sdis64.ui.indicators.toDisplayableIndicator
import fr.sdis64.ui.utilities.ErrorMessage
import fr.sdis64.ui.utilities.TableStylesheet
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun ManualIndicatorAdmin(client: CtacClient) {
    val chan = remember { Channel<Unit>(1) }
    val manualIndicatorLevels by rememberLoadingState { triggerablePoll(chan) { client.findManualIndicatorLevels() } }

    H1 { Text("Saisie des indicateurs manuels") }
    loaderOr(manualIndicatorLevels) { levels ->
        H2 { Text("Affichage sur le mur d'image") }
        ManualIndicatorEditorPanel(client, levels, chan)
        H2 { Text("Informations sur les différents niveaux") }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
            }
        }) {
            ManualIndicatorInformativeTables(levels)
        }
    }
}

@Composable
fun ManualIndicatorEditorPanel(client: CtacClient, indicators: Set<ManualIndicatorLevel>, refresh: SendChannel<Unit>) {
    val scope = rememberCoroutineScope()

    val saveHandler = remember { Channel<ManualIndicatorLevel>(1) }
    var error: String? by remember { mutableStateOf(null) }

    scope.launch {
        while (true) {
            val l = saveHandler.receive()
            error = try {
                client.saveManualIndicatorLevel(
                    l.copy(active = !l.active)
                )
                refresh.send(Unit)
                null
            } catch (e: Exception) {
                ensureActive()
                console.log(e)
                "Erreur lors de la sauvegarde de l'indicateur manuel, veuillez réessayer"
            }
        }
    }

    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            justifyContent(JustifyContent.SpaceBetween)
            maxWidth(500.px)
            flexWrap(FlexWrap.Wrap)
        }
    }) {
        indicators.sortedBy { it.id }.groupBy { it.category.type }.forEach { (type, levelList) ->
            key(type.name) {
                ManualIndicatorEditorPanelLevels(type, levelList, saveHandler)
            }
        }
    }
    error?.let {
        ErrorMessage(it)
    }
}

@Composable
fun ManualIndicatorEditorPanelLevels(
    type: ManualIndicatorType,
    levels: List<ManualIndicatorLevel>,
    saveHandler: SendChannel<ManualIndicatorLevel>,
) {
    val scope = rememberCoroutineScope()

    Div {
        H3 { Text(type.displayName) }
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
            }
        }) {
            levels.filter { it.id != null }.sortedBy { it.id }.forEach { l ->
                key(l.id) {
                    Label {
                        CheckboxInput(checked = l.active, attrs = {
                            id("${l.id}")
                            onClick { scope.launch { saveHandler.send(l) } }
                        })
                        Text(l.name)
                    }
                }
            }
        }
    }
}

@Composable
fun ManualIndicatorInformativeTables(indicators: Set<ManualIndicatorLevel>) {
    Table(attrs = {
        style {
            textAlign("center")
            flexGrow(1)
        }
        classes(TableStylesheet.lightBorderedTable, TableStylesheet.mediumRowTable)
    }) {
        Thead {
            Tr {
                Th { Text("Catégorie") }
                Th { Text("Niveau") }
                Th { Text("Image affichée sur le mur") }
                Th { Text("Descriptions") }
            }
        }
        Tbody {
            indicators.sortedWith(compareBy({ it.category.type }, ManualIndicatorLevel::id)).forEach {
                Tr {
                    Td { Text(it.category.type.displayName) }
                    Td { Text(it.name) }
                    Td { ManualIndicatorLevelImageAsIcon(it) }
                    Td(attrs = {
                        style {
                            textAlign("start")
                        }
                    }) {
                        Ul {
                            it.descriptions.forEach {
                                Li { Text(it) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManualIndicatorLevelImageAsIcon(indicator: ManualIndicatorLevel) {
    val displayable = indicator.toDisplayableIndicator()
    if (displayable != null) {
        displayable.visual()
    } else {
        Text("-")
    }
}
