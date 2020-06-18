package fr.sdis64.ui.screens

import androidx.compose.runtime.Composable
import fr.sdis64.client.CtacClient
import fr.sdis64.ui.clock.Clock
import fr.sdis64.ui.indicators.Indicators
import fr.sdis64.ui.mail.UnseenMails
import fr.sdis64.ui.maps.*
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
fun Codis(client: CtacClient) {
    Div(attrs = {
        style {
            display(DisplayStyle.Grid)
            // each TV is divided into 30x30 small blocks
            gridAutoRows((physicalTvHeight / 30).toString())
            gridAutoColumns((physicalTvWidth / 30).toString())
        }
    }) {
        GridTile(row = "1 / span 10", column = "1 / span 18") { Clock() }
        GridTile(row = "1 / span 10", column = "19 / span 12") { ResponseTime(client) }
        GridTile(row = "11 / span 20", column = "1 / span 18") { CallStatistics(client) }
        GridTile(row = "11 / span 20", column = "19 / span 12") { InteventionStatistics(client) }
        GridTile(row = "31 / span 30", column = "1 / span 12") { Indicators(client) }
        GridTile(row = "31 / span 30", column = "13 / span 18") { Operators(client) }
        GridTile(row = "61 / span 15", column = "1 / span 15") { IncVehicleMap(client) }
        GridTile(row = "61 / span 15", column = "16 / span 15") { FenVehicleMap(client) }
        GridTile(row = "61 / span 15", column = "31 / span 15") { VsavVehicleMap(client) }
        GridTile(row = "76 / span 15", column = "1 / span 15") { VsrmVehicleMap(client) }
        GridTile(row = "76 / span 15", column = "16 / span 15") { VlsmVehicleMap(client) }
        GridTile(row = "76 / span 15", column = "31 / span 15") { CarencesMap() }
        GridTile(row = "1 / span 15", column = "31 / span 15") { RainMap() }
        GridTile(row = "16 / span 15", column = "31 / span 15") { WindMap() }
        GridTile(row = "31 / span 15", column = "31 / span 15") { VigicruesMap() }
        GridTile(row = "46 / span 15", column = "31 / span 15") { MaregrammeMap() }
        GridTile(row = "1 / span 50", column = "46 / span 45") { InterventionMap(alignedRight = true) }
        GridTile(row = "51 / span 10", column = "46 / span 27") { UnseenMails(client) }
        GridTile(row = "51 / span 10", column = "73 / span 9") { Helicopters(client) }
        GridTile(row = "51 / span 10", column = "82 / span 9") { ActiveMountainRescueOrganisms(client) }
        GridTile(row = "61 / span 30", column = "61 / span 30") { VehicleTable(client) }
    }
}
