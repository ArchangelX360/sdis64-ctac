package fr.sdis64.ui.operators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.api.operators.Operator
import fr.sdis64.api.operators.OperatorStatus
import fr.sdis64.client.CtacClient
import fr.sdis64.client.operators
import fr.sdis64.ui.utilities.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun Operators(client: CtacClient) {
    val operators by rememberLoadingState { client.operators() }

    Tile("Opérateurs connectés", padded = true) {
        loaderOr(operators) {
            OperatorTable(operators = it)
        }
    }
}

@Composable
private fun OperatorTable(operators: Set<Operator>, showHeaders: Boolean = false) {
    val relevantOperator = operators
        .filter { it.isRelevantToDisplay() }
        .sortedBy { it.phoneNumber.realNumber }

    Table(
        attrs = {
            style {
                textAlign("center")
                width(100.percent)
                if (relevantOperator.size >= 8) {
                    fontSize(25.px)
                }
            }
            classes(TableStylesheet.darkBorderedTable, TableStylesheet.largeRowTable)
        }
    ) {
        Thead(
            attrs = {
                style {
                    if (!showHeaders) {
                        display(DisplayStyle.None)
                    }
                }
            }
        ) {
            Tr {
                Th { Text("Num.") }
                Th { Text("Fonction") }
                Th { Text("Nom") }
                Th { Text("État") }
            }
        }
        Tbody {
            relevantOperator.forEach {
                Tr {
                    Td { Text(it.phoneNumber.realNumber) }
                    Td { Text(it.function) }
                    Td { Text(it.name) }
                    Td { OperatorStatusChip(it.status) }
                }
            }
        }
    }
}

@Composable
private fun OperatorStatusChip(status: OperatorStatus) {
    Div(
        attrs = {
            style {
                status.backgroundColor?.let { backgroundColor(it.hexCodeToRgb()) }
                status.textColor?.let { color(it.hexCodeToRgb()) }
                margin(5.px)
                padding(5.px)
                borderRadius(5.px)
            }
        }
    ) {
        Text(status.name)
    }
}

private fun Operator.isRelevantToDisplay(): Boolean =
    name != "CTAC" && phoneNumber.realNumber.startsWith('9') && status.displayed
