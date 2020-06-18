package fr.sdis64.ui.screens

import androidx.compose.runtime.Composable
import fr.sdis64.client.CtacClient
import fr.sdis64.ui.clock.Clock
import fr.sdis64.ui.indicators.Indicators
import fr.sdis64.ui.mail.UnseenMails
import fr.sdis64.ui.maps.InterventionMap
import fr.sdis64.ui.operators.Operators
import fr.sdis64.ui.organisms.ActiveMountainRescueOrganisms
import fr.sdis64.ui.statistics.CallStatistics
import fr.sdis64.ui.statistics.InteventionStatistics
import fr.sdis64.ui.statistics.ResponseTime
import fr.sdis64.ui.utilities.GridTile
import fr.sdis64.ui.utilities.physicalTvHeight
import fr.sdis64.ui.utilities.physicalTvWidth
import fr.sdis64.ui.vehicles.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun Cta(client: CtacClient) {
    Div(attrs = {
        style {
            display(DisplayStyle.Grid)
            // each TV is divided into 30x30 small blocks
            gridAutoRows((physicalTvHeight / 30).toString())
            gridAutoColumns((physicalTvWidth / 30).toString())
        }
    }) {
        GridTile(row = "1 / span 15", column = "1 / span 15") { VsavVehicleMap(client) }
        GridTile(row = "16 / span 15", column = "1 / span 15") { CarencesMap() }
        GridTile(row = "1 / span 15", column = "16 / span 15") { IncVehicleMap(client) }
        GridTile(row = "16 / span 15", column = "16 / span 15") { FenVehicleMap(client) }
        GridTile(row = "16 / span 15", column = "31 / span 15") { VlsmVehicleMap(client) }
        GridTile(row = "1 / span 15", column = "31 / span 15") { VsrmVehicleMap(client) }
        GridTile(row = "1 / span 30", column = "46 / span 15") { Indicators(client) }
        GridTile(row = "1 / span 10", column = "61 / span 12") { ResponseTime(client) }
        GridTile(row = "1 / span 10", column = "73 / span 18") { Clock() }
        GridTile(row = "11 / span 20", column = "61 / span 18") { CallStatistics(client) }
        GridTile(row = "11 / span 20", column = "79 / span 12") { InteventionStatistics(client) }
        GridTile(row = "31 / span 30", column = "1 / span 30") { VehicleTable(client) }
        GridTile(row = "31 / span 20", column = "31 / span 30") { Operators(client) }
        GridTile(row = "51 / span 10", column = "31 / span 30") { UnseenMails(client) }
        GridTile(row = "31 / span 20", column = "61 / span 30") { InterventionMap() }
        GridTile(row = "51 / span 10", column = "76 / span 15") { ActiveMountainRescueOrganisms(client) }
        GridTile(row = "51 / span 10", column = "61 / span 15") { Helicopters(client) }
    }
}
