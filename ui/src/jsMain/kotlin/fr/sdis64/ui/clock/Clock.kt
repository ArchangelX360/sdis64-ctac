package fr.sdis64.ui.clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import fr.sdis64.client.poll
import fr.sdis64.ui.utilities.Tile
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import fr.sdis64.ui.utilities.toFrench
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Section
import org.jetbrains.compose.web.dom.Text
import kotlin.time.Duration.Companion.seconds

@Composable
fun Clock() {
    val now by rememberLoadingState { tick() }

    Tile {
        loaderOr(now) {
            ClockBlock(datetime = it)
        }
    }
}

@Composable
private fun ClockBlock(datetime: LocalDateTime) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            flexDirection(FlexDirection.Column)
            height(100.percent)
        }
    }) {
        Section(attrs = {
            style {
                fontSize(130.px)
            }
        }) {
            Text("${datetime.hour.padZero()}:${datetime.minute.padZero()}")
        }
        Section(attrs = {
            style {
                fontSize(50.px)
            }
        }) {
            Text("${datetime.dayOfWeek.name.toFrench()} ${datetime.dayOfMonth} ${datetime.month.name.toFrench()} ${datetime.year}")
        }
    }
}

private fun tick() = poll(1.seconds) { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }

private fun Int.padZero(): String = this.toString().padStart(2, '0')
