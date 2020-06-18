package fr.sdis64.api.organisms

import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OrganismTest {
    @Test
    fun shouldBeActiveWhenInWindow() {
        val o = Organism(
            id = 1L,
            name = "test",
            category = OrganismCategory(
                id = 1L,
                name = "test",
            ),
            activeTimeWindows = setOf(
                OrganismTimeWindow(
                    id = 1L,
                    start = Instant.parse("2018-02-01T10:10:00.000Z"),
                    end = Instant.parse("2018-03-01T10:10:00.000Z"),
                ),
                OrganismTimeWindow(
                    id = 2L,
                    start = Instant.parse("2017-02-01T10:10:00.000Z"),
                    end = Instant.parse("2017-03-01T10:10:00.000Z"),
                )
            ),
        )

        assertTrue(o.isActiveAt(Instant.parse("2017-02-01T10:10:00.000Z")))
        assertTrue(o.isActiveAt(Instant.parse("2017-03-01T10:10:00.000Z")))
        assertTrue(o.isActiveAt(Instant.parse("2017-02-02T10:10:00.000Z")))
        assertTrue(o.isActiveAt(Instant.parse("2018-02-02T10:10:00.000Z")))

        assertFalse(o.isActiveAt(Instant.parse("2017-02-01T01:10:00.000Z")))
        assertFalse(o.isActiveAt(Instant.parse("2019-02-02T10:10:00.000Z")))
        assertFalse(o.isActiveAt(Instant.parse("2017-10-02T10:10:00.000Z")))
    }

    @Test
    fun shouldReturnFiremenDays() {
        val actual = OrganismTimeWindow(
            id = 1L,
            start = LocalDateTime.parse("2018-01-30T07:00:00").toInstant(TimeZone.currentSystemDefault()),
            end = LocalDateTime.parse("2018-02-06T06:59:59").toInstant(TimeZone.currentSystemDefault()),
        ).asFiremanDays()
        assertEquals(
            setOf(
                LocalDate(2018, Month.JANUARY, 30),
                LocalDate(2018, Month.JANUARY, 31),
                LocalDate(2018, Month.FEBRUARY, 1),
                LocalDate(2018, Month.FEBRUARY, 2),
                LocalDate(2018, Month.FEBRUARY, 3),
                LocalDate(2018, Month.FEBRUARY, 4),
                LocalDate(2018, Month.FEBRUARY, 5),
            ), actual
        )
    }

    @Test
    fun shouldReturnSingleDay() {
        val actual = OrganismTimeWindow(
            id = 1L,
            start = LocalDateTime.parse("2018-01-30T07:00:00").toInstant(TimeZone.currentSystemDefault()),
            end = LocalDateTime.parse("2018-01-31T06:59:59").toInstant(TimeZone.currentSystemDefault()),
        ).asFiremanDays()
        assertEquals(
            setOf(
                LocalDate(2018, Month.JANUARY, 30),
            ), actual
        )
    }

    @Test
    fun shouldConvertAndMergeTimeWindows() {
        val actual = setOf(
            LocalDate(2018, Month.JANUARY, 30),
            LocalDate(2018, Month.JANUARY, 31),
            LocalDate(2018, Month.FEBRUARY, 1),
            LocalDate(2018, Month.FEBRUARY, 2),
            LocalDate(2018, Month.FEBRUARY, 4),
            LocalDate(2018, Month.FEBRUARY, 5),
            LocalDate(2018, Month.FEBRUARY, 6),
        ).asFiremanTimeWindows()

        val expected = setOf(
            OrganismTimeWindow(
                id = null,
                start = LocalDate(2018, Month.JANUARY, 30).atTime(firemanStartOfDay)
                    .toInstant(TimeZone.currentSystemDefault()),
                end = LocalDate(2018, Month.FEBRUARY, 3).atTime(firemanEndOfDay)
                    .toInstant(TimeZone.currentSystemDefault()),
            ),
            OrganismTimeWindow(
                id = null,
                start = LocalDate(2018, Month.FEBRUARY, 4).atTime(firemanStartOfDay)
                    .toInstant(TimeZone.currentSystemDefault()),
                end = LocalDate(2018, Month.FEBRUARY, 7).atTime(firemanEndOfDay)
                    .toInstant(TimeZone.currentSystemDefault()),
            ),
        )

        assertEquals(expected, actual)
    }
}
