package fr.sdis64.ui.admin

import androidx.compose.runtime.*
import fr.sdis64.api.organisms.Organism
import fr.sdis64.api.organisms.daysUntilAsSet
import fr.sdis64.api.organisms.firemanEndOfDay
import fr.sdis64.api.organisms.firemanStartOfDay
import fr.sdis64.client.CtacClient
import fr.sdis64.client.triggerablePoll
import fr.sdis64.ui.organisms.mountainRescueId
import fr.sdis64.ui.utilities.ErrorMessage
import fr.sdis64.ui.utilities.loaderOr
import fr.sdis64.ui.utilities.rememberLoadingState
import fr.sdis64.ui.utilities.toFrench
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import kotlin.math.ceil

private const val monthRowSpan = 2
private const val monthColumn = 1

@Composable
fun MountainRescueAdmin(
    client: CtacClient,
) {
    val refresh = remember { Channel<Unit>(1) }
    val mountainRescueOrganisms by rememberLoadingState { triggerablePoll(refresh) { client.findAllOrganisms(categoryId = mountainRescueId) } }

    Style(CalendarStylesheet)

    H1 { Text("Saisie des permanence des secours montagne") }
    loaderOr(mountainRescueOrganisms) { organisms ->
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        organisms.sortedByDescending { it.name }.forEach { o ->
            key(o.id) {
                H2 { Text(o.name) }
                OrganismCalendar(client, refresh, o, today.date)
            }
        }
    }
}

@Composable
private fun OrganismCalendar(
    client: CtacClient,
    refresh: SendChannel<Unit>,
    organism: Organism,
    today: LocalDate,
) {
    val scope = rememberCoroutineScope()
    val saveHandler = remember { Channel<Organism>(1) }
    var error: String? by remember { mutableStateOf(null) }

    scope.launch {
        while (true) {
            val o = saveHandler.receive()
            error = try {
                client.saveOrganism(o)
                refresh.send(Unit)
                null
            } catch (e: Exception) {
                ensureActive()
                console.log(e)
                "Erreur lors de la sauvegarde de l'organism, veuillez réessayer"
            }
        }
    }

    Div(attrs = {
        style {
            val borderGrey = rgba(118, 118, 118, 0.3)
            display(DisplayStyle.Grid)
            border {
                width(1.px)
                style(LineStyle.Solid)
                color(borderGrey)
            }
            backgroundColor(borderGrey)
            gap(1.px)
            maxWidth(1060.px)
            property("margin", "auto")
        }
    }) {
        CalendarMonths(saveHandler, organism, today)
        CalendarWeeks(saveHandler, organism, today)
        CalendarDays(saveHandler, organism, today)
    }

    error?.let {
        ErrorMessage(it)
    }
}

@Composable
private fun CalendarMonths(
    saveHandler: SendChannel<Organism>,
    organism: Organism,
    today: LocalDate,
) {
    for (month in Month.values()) {
        CalendarMonthCell(
            saveHandler = saveHandler,
            organism = organism,
            today = today,
            month = month,
        )
    }
}

@Composable
private fun CalendarMonthCell(
    saveHandler: SendChannel<Organism>,
    organism: Organism,
    today: LocalDate,
    month: Month,
) {
    val scope = rememberCoroutineScope()
    val dateFrom = LocalDate(today.year, month, 1)
    val dateTo = dateFrom.plus(1, DateTimeUnit.MONTH)
    val from = dateFrom.atTime(firemanStartOfDay).toInstant(TimeZone.currentSystemDefault())
    val to = dateTo.atTime(firemanEndOfDay).toInstant(TimeZone.currentSystemDefault())
    val isActive = organism.isActive(from, to)
    val isPast = today.month > month

    Div(attrs = {
        classes(CalendarStylesheet.cell)
        style {
            gridRow("${(month.ordinal * monthRowSpan) + 1} / span $monthRowSpan")
            gridColumn("$monthColumn")
            cellBusinessStyle(isPast, isActive)
        }
        if (!isPast) {
            onClick {
                val days = dateFrom.daysUntilAsSet(dateTo)
                val newOrganism = if (isActive) {
                    organism.copyWithout(days)
                } else {
                    organism.copyWith(days)
                }
                scope.launch { saveHandler.send(newOrganism) }
            }
        }
    }) {
        Text(month.name.toFrench())
    }
}


@Composable
private fun CalendarWeeks(
    saveHandler: SendChannel<Organism>,
    organism: Organism,
    today: LocalDate,
) {
    var columnIndex = monthColumn + 1
    var date = LocalDate(today.year, Month.JANUARY, 1)
    while (date.year == today.year) {
        val days = date.isoWeek.daysIncludedIn(date.month, date.year).size
        CalendarWeekCell(
            saveHandler = saveHandler,
            organism = organism,
            week = date.isoWeek,
            month = date.month,
            columnNumber = columnIndex,
            columnSpan = days,
            today = today,
        )
        val nextIterationDay = date.plus(days, DateTimeUnit.DAY)
        columnIndex = if (nextIterationDay.belongsTo(date.month, date.year)) {
            columnIndex + days
        } else {
            // reached the end of the month
            monthColumn + 1
        }
        date = nextIterationDay
    }
}

@Composable
private fun CalendarWeekCell(
    saveHandler: SendChannel<Organism>,
    organism: Organism,
    today: LocalDate,
    week: Week,
    month: Month,
    columnNumber: Int,
    columnSpan: Int,
) {
    val scope = rememberCoroutineScope()
    val monday = week.monday.atTime(firemanStartOfDay).toInstant(TimeZone.currentSystemDefault())
    val sunday = week.sunday.atTime(firemanStartOfDay).toInstant(TimeZone.currentSystemDefault())
    val isActive = organism.isActive(from = monday, until = sunday)
    val isPast = today.isoWeek > week

    Div(attrs = {
        classes(CalendarStylesheet.cell)
        style {
            gridRow("${(month.ordinal * monthRowSpan) + 1}")
            gridColumn("$columnNumber / span $columnSpan")
            cellBusinessStyle(isPast, isActive)
        }
        if (!isPast) {
            onClick {
                val days = week.monday.daysUntilAsSet(week.sunday.plus(1, DateTimeUnit.DAY))
                val newOrganism = if (isActive) {
                    organism.copyWithout(days)
                } else {
                    organism.copyWith(days)
                }
                scope.launch { saveHandler.send(newOrganism) }
            }
        }
    }) {
        Text("S${week.number}")
    }
}

@Composable
private fun CalendarDays(
    saveHandler: SendChannel<Organism>,
    organism: Organism,
    today: LocalDate,
) {
    var date = LocalDate(today.year, Month.JANUARY, 1)
    while (date.year == today.year) {
        CalendarDayCell(
            saveHandler = saveHandler,
            organism = organism,
            today = today,
            date = date,
        )
        date = date.plus(1, DateTimeUnit.DAY)
    }
}

@Composable
private fun CalendarDayCell(
    saveHandler: SendChannel<Organism>,
    organism: Organism,
    today: LocalDate,
    date: LocalDate,
) {
    val scope = rememberCoroutineScope()
    val from = date.atTime(firemanStartOfDay).toInstant(TimeZone.currentSystemDefault())
    val to = date.plus(1, DateTimeUnit.DAY).atTime(firemanEndOfDay).toInstant(TimeZone.currentSystemDefault())
    val isActive = organism.isActive(from, to)
    val isPast = today > date

    Div(attrs = {
        classes(CalendarStylesheet.cell, CalendarStylesheet.dayCell)
        style {
            gridRow("${((date.month.ordinal * monthRowSpan) + 2)}")
            gridColumn("${date.dayOfMonth + monthColumn}")
            cellBusinessStyle(isPast, isActive)
        }
        if (!isPast) {
            onClick {
                val days = setOf(date)
                val newOrganism = if (isActive) {
                    organism.copyWithout(days)
                } else {
                    organism.copyWith(days)
                }
                scope.launch { saveHandler.send(newOrganism) }
            }
        }
    }) {
        Text("${date.dayOfMonth}")
    }
}

private fun StyleScope.cellBusinessStyle(isPast: Boolean, isActive: Boolean) {
    backgroundColor(
        when {
            isPast -> rgba(109, 109, 109, 0.2)
            isActive -> rgba(220, 220, 0, 0.2)
            else -> Color.white
        }
    )
    if (!isPast) {
        cursor("pointer")
    }
}

private object CalendarStylesheet : StyleSheet() {
    val cell by style {
        display(DisplayStyle.Inherit)
        textAlign("center")
        justifyContent(JustifyContent.Center)
        alignContent(AlignContent.Center)
    }

    val dayCell by style {
        width(30.px)
    }
}


internal data class Week(
    val number: Int,
    val year: Int,
) : Comparable<Week> {
    override fun compareTo(other: Week) = when {
        year != other.year -> year.compareTo(other.year)
        else -> number.compareTo(other.number)
    }
}

/**
 * The ISO-8601 week number of the given date
 */
internal val LocalDate.isoWeek: Week
    get() {
        val dayOfWeekNumber = dayOfWeek.isoDayNumber
        val mondayOfThisWeek = minus(dayOfWeekNumber - 1, DateTimeUnit.DAY)
        val thursdayOfThisWeek = mondayOfThisWeek.plus(3, DateTimeUnit.DAY)
        val weekNumber = ceil(thursdayOfThisWeek.dayOfYear.toDouble() / 7).toInt()
        return Week(number = weekNumber, year = thursdayOfThisWeek.year)
    }

internal val Week.monday: LocalDate
    get() {
        val beginningOfYear = LocalDate(year, Month.JANUARY, 1)
        val aDayOfThisWeek = when {
            beginningOfYear.isThursdayOrAfter -> beginningOfYear.plus(number * 7, DateTimeUnit.DAY)
            else -> beginningOfYear.plus((number - 1) * 7, DateTimeUnit.DAY)
        }
        return aDayOfThisWeek.minus(aDayOfThisWeek.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    }

private val Week.sunday get() = monday.plus(6, DateTimeUnit.DAY)

internal fun Week.daysIncludedIn(month: Month, year: Int) =
    // TODO: could we use `(monday..sunday)` instead?
    (0..6).map { monday.plus(it, DateTimeUnit.DAY) }.filter { it.month == month && it.year == year }

private val LocalDate.isThursdayOrAfter get() = dayOfWeek.isoDayNumber >= 4
private fun LocalDate.belongsTo(month: Month, year: Int) = this.month == month && this.year == year
