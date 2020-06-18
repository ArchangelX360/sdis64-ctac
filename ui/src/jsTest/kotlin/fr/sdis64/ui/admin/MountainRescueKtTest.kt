package fr.sdis64.ui.admin

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MountainRescueKtTest {

    @Test
    fun getWeekNumber() {
        // usual cases
        assertEquals(
            Week(number = 1, year = 2019),
            LocalDate(2019, Month.JANUARY, 1).isoWeek,
            "The 1st of January 2019 week number is 1"
        )
        assertEquals(
            Week(number = 52, year = 2021),
            LocalDate(2021, Month.DECEMBER, 31).isoWeek,
            "The 31st of December 2021 week number is 52"
        )

        // weird cases
        assertEquals(
            Week(number = 52, year = 2011),
            LocalDate(2012, Month.JANUARY, 1).isoWeek,
            "The 1st of January 2012 week number is 52"
        )
        assertEquals(
            Week(number = 52, year = 2022),
            LocalDate(2023, Month.JANUARY, 1).isoWeek,
            "The 1st of January 2023 week number is 52"
        )
        assertEquals(
            Week(number = 53, year = 2009),
            LocalDate(2010, Month.JANUARY, 1).isoWeek,
            "The 1st of January 2010 week number is 53"
        )
        assertEquals(
            Week(number = 53, year = 2020),
            LocalDate(2020, Month.DECEMBER, 31).isoWeek,
            "The 31st of December 2020 week number is 53"
        )
        assertEquals(
            Week(number = 1, year = 2015),
            LocalDate(2014, Month.DECEMBER, 29).isoWeek,
            "The 29th of December 2014 week number is 1"
        )

        // leap year
        assertEquals(Week(number = 1, year = 1981), LocalDate(1980, Month.DECEMBER, 31).isoWeek)
    }

    @Test
    fun getWeekMonday() {
        assertEquals(LocalDate(2021, Month.DECEMBER, 27), Week(52, 2021).monday)
        assertEquals(LocalDate(2022, Month.JANUARY, 3), Week(1, 2022).monday)
        assertEquals(LocalDate(2020, Month.DECEMBER, 28), Week(53, 2020).monday)
        assertEquals(LocalDate(2019, Month.DECEMBER, 30), Week(1, 2020).monday)
    }

    @Test
    fun daysIncludedIn() {
        assertEquals(2, Week(52, 2021).daysIncludedIn(Month.JANUARY, 2022).size)
        assertEquals(7, Week(24, 2022).daysIncludedIn(Month.JUNE, 2022).size)
        assertEquals(0, Week(1, 2022).daysIncludedIn(Month.JUNE, 2022).size)
        assertEquals(6, Week(1, 2019).daysIncludedIn(Month.JANUARY, 2019).size)
        assertEquals(1, Week(1, 2019).daysIncludedIn(Month.DECEMBER, 2018).size)
    }

    @Test
    fun weekComparison() {
        assertTrue(Week(52, 2021) < Week(52, 2022))
        assertTrue(Week(51, 2021) < Week(52, 2021))
        assertTrue(Week(51, 2021) <= Week(52, 2021))
        assertEquals(Week(52, 2021), Week(52, 2021))
    }
}
