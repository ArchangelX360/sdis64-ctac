package fr.sdis64.api.organisms

import fr.sdis64.api.Identified
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

private const val firemanHour = 7
val firemanStartOfDay = LocalTime(firemanHour, 0, 0)
val firemanEndOfDay = LocalTime(firemanHour - 1, 59, 59)

@Serializable
data class Organism(
    override val id: Long?,
    val name: String,
    val category: OrganismCategory,
    val activeTimeWindows: Set<OrganismTimeWindow>,
) : Identified {
    fun isActiveAt(time: Instant): Boolean = activeTimeWindows.any { time in it }
    fun isActive(from: Instant, until: Instant): Boolean = activeTimeWindows.any { from in it && until in it }

    fun copyWithout(date: Set<LocalDate>): Organism = copy(
        activeTimeWindows = activeTimeWindows.flatMap { it.asFiremanDays() }.toSet().minus(date).asFiremanTimeWindows()
    )

    fun copyWith(date: Set<LocalDate>): Organism = copy(
        activeTimeWindows = activeTimeWindows.flatMap { it.asFiremanDays() }.toSet().plus(date).asFiremanTimeWindows()
    )
}

@Serializable
data class OrganismCategory(
    override val id: Long?,
    val name: String,
) : Identified

@Serializable
data class OrganismTimeWindow(
    override val id: Long?,
    val start: Instant,
    val end: Instant,
) : Identified {

    operator fun contains(time: Instant) = time in start..end

    fun asFiremanDays(): Set<LocalDate> {
        val startDateTime = start.toLocalDateTime(TimeZone.currentSystemDefault())
        if (startDateTime.time != firemanStartOfDay) {
            error("cannot translate to fireman days, the period is not starting at $firemanStartOfDay")
        }
        val endDateTime = end.toLocalDateTime(TimeZone.currentSystemDefault())
        if (endDateTime.time != firemanEndOfDay) {
            error("cannot translate to fireman days, the period is not ending at $firemanEndOfDay")
        }
        return startDateTime.date.daysUntilAsSet(endDateTime.date)
    }
}

fun LocalDate.daysUntilAsSet(to: LocalDate): Set<LocalDate> {
    return (0 until daysUntil(to)).map {
        plus(it, DateTimeUnit.DAY)
    }.toSet()
}

fun Iterable<LocalDate>.asFiremanTimeWindows(): Set<OrganismTimeWindow> = map {
    OrganismTimeWindow(
        id = null,
        start = it.atTime(firemanStartOfDay).toInstant(TimeZone.currentSystemDefault()),
        end = it.plus(1, DateTimeUnit.DAY).atTime(firemanEndOfDay).toInstant(TimeZone.currentSystemDefault()),
    )
}.merged().toSet()

private fun List<OrganismTimeWindow>.merged() = sortedBy { it.start }.fold(listOf<OrganismTimeWindow>()) { acc, tw ->
    val mutableAcc = acc.toMutableList()
    val last = mutableAcc.removeLastOrNull()
    if (last == null) {
        listOf(tw)
    } else {
        val merge = merge(last, tw)
        mutableAcc.plus(merge)
    }
}

private fun merge(o1: OrganismTimeWindow, o2: OrganismTimeWindow): List<OrganismTimeWindow> = when {
    o2.start !in o1.start..o1.endPlusOneSecond && o1.start !in o2.start..o2.endPlusOneSecond -> listOf(o1, o2).sortedBy { it.start }
    o1.start in o2.start..o2.endPlusOneSecond && o1.endPlusOneSecond in o2.start..o2.endPlusOneSecond -> listOf(o2)
    o2.start in o1.start..o1.endPlusOneSecond && o2.endPlusOneSecond in o1.start..o1.endPlusOneSecond -> listOf(o1)
    else -> {
        val (first, second) = listOf(o1, o2).sortedBy { it.start }
        listOf(
            OrganismTimeWindow(
                id = null,
                start = first.start,
                end = second.end,
            )
        )
    }
}

private val OrganismTimeWindow.endPlusOneSecond get() = end.plus(1.seconds)
