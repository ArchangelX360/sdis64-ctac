package fr.sdis64.ui.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.api.statistics.CallStatistic
import fr.sdis64.api.statistics.InterventionStatistic
import fr.sdis64.client.CtacClient
import fr.sdis64.client.callsStats
import fr.sdis64.client.interventionStats
import fr.sdis64.client.responseTime
import fr.sdis64.ui.utilities.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun ResponseTime(client: CtacClient) {
    val responseTime by rememberLoadingState { client.responseTime() }

    Tile("Temps de réponse moyen") {
        loaderOr(responseTime) {
            ResponseTimeGauge(it)
        }
    }
}

@Composable
fun CallStatistics(client: CtacClient) {
    val callStatistics by rememberLoadingState { client.callsStats() }
    // TODO: we might want to return an ordered array in the API instead
    // This is a tradeoff between:
    //  - Frontend needing to know labels
    //  - or, Backend needing to know an order that only make sense in the Frontend
    val order = listOf("emergency", "operational", "ecobuage", "out", "inOut")

    Tile("Statistiques d'appels", padded = true) {
        loaderOr(callStatistics) {
            val sortedStatistics = it.values.sortedBy { stat ->
                order.indexOf(stat.label)
            }
            CallStatisticsTable(sortedStatistics)
        }
    }
}

@Composable
fun InteventionStatistics(client: CtacClient) {
    val interventionStatistics by rememberLoadingState { client.interventionStats() }
    // TODO: we might want to return an ordered array in the API instead
    // This is a tradeoff between:
    //  - Frontend needing to know labels
    //  - or, Backend needing to know an order that only make sense in the Frontend
    val order = listOf("sap", "avp", "inc", "div", "rt")

    Tile("Statistiques d'intervention", padded = true) {
        loaderOr(interventionStatistics) {
            val sortedStatistics = it.values.sortedBy { stat ->
                order.indexOf(stat.label)
            }
            InteventionStatisticsTable(sortedStatistics)
        }
    }
}

@Composable
private fun ResponseTimeGauge(responseTime: Int) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            justifyContent(JustifyContent.Center)
            alignItems(AlignItems.Center)
            height(100.percent)
        }
    }) {
        Gauge(current = responseTime.toDouble(), max = 9.0) // Gauge is from 0 to 9 seconds
        Span(attrs = {
            style { marginTop(10.px) }
        }) { Text("${responseTime}s") }
    }
}

@Composable
private fun CallStatisticsTable(callStatistics: Collection<CallStatistic>) {
    Table(attrs = {
        style {
            textAlign("center")
            width(100.percent)
        }
        classes(TableStylesheet.darkBorderedTable, TableStylesheet.largeRowTable)
    }) {
        Thead {
            Tr {
                Th { Text("Type") }
                Th { MultilineText("En\ncours") }
                Th { MultilineText("Jour\n(7h)") }
                Th { MultilineText("Total\n(Année)") }
                Th { MultilineText("En\nattente") }
            }
        }
        Tbody {
            callStatistics.forEach {
                Tr {
                    Td { Text(it.title ?: "-") }
                    Td { Text("${it.ongoing ?: "-"}") }
                    Td { Text("${it.day ?: "-"}") }
                    Td { Text("${it.year ?: "-"}") }
                    Td { Text("${it.onhold ?: "-"}") }
                }
            }
        }
    }
}

@Composable
private fun InteventionStatisticsTable(interventionStatistics: Collection<InterventionStatistic>) {
    Table(attrs = {
        style {
            textAlign("center")
            width(100.percent)
        }
        classes(TableStylesheet.darkBorderedTable, TableStylesheet.largeRowTable)
    }) {
        Thead {
            Tr {
                Th { Text("Type") }
                Th { MultilineText("En\ncours") }
                Th { MultilineText("Jour\n(7h)") }
                Th { MultilineText("Total\n(Année)") }
            }
        }
        Tbody {
            interventionStatistics.forEach {
                Tr {
                    Td { Text(it.label.uppercase()) }
                    Td { Text("${it.ongoing ?: 0}") }
                    Td { Text("${it.day ?: 0}") }
                    Td { Text("${it.year ?: 0}") }
                }
            }
            Tr {
                Td { Text("TOTAL") }
                Td { Text("${interventionStatistics.sumOf { it.ongoing ?: 0 }}") }
                Td { Text("${interventionStatistics.sumOf { it.day ?: 0 }}") }
                Td { Text("${interventionStatistics.sumOf { it.year ?: 0 }}") }
            }
        }
    }
}

@Composable
private fun MultilineText(value: String) {
    val lines = value.lines()
    lines.forEachIndexed { index, line ->
        Text(line)
        if (index != lines.lastIndex) {
            Br()
        }
    }
}
