package com.rectanglel.patstatic.dates

import org.junit.Assert
import org.junit.Test
import java.util.*

@Suppress("RedundantVisibilityModifier")
public class DateExtensionsTest {

    private fun createCalendar(year: Int, month: Int, day: Int, hour: Int) : Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun createDate(year: Int, month: Int, day: Int, hour: Int) : Date = createCalendar(year, month, day, hour)

    /**
     * "Current date of time" for unit testing. Logic to do things against...
     *
     * Take note that the `Date` class works in a 0 scale, so that means January = 0, 1st day of the month = 0
     *
     * @return 2018-08-30T15:00 as a `Date`
     */
    private fun currentDate() : Date {
        return createDate(
                year = 2018,
                month = 7,
                day = 30,
                hour = 0
        )
    }

    private val pastOneDayTestData = listOf(
            PastOneDayDatum(
                    dateToTest = createDate(
                            year = 2018,
                            month = 7,
                            day = 29,
                            hour = 12
                    ),
                    currentDate = currentDate(),
                    isPastOneDay = false
            ),
            PastOneDayDatum(
                    dateToTest = createDate(
                           year = 2018,
                            month = 7,
                            day = 29,
                            hour = 23
                    ),
                    currentDate = currentDate(),
                    isPastOneDay = false
            ),
            PastOneDayDatum(
                    dateToTest = createDate(
                            year = 2018,
                            month = 7,
                            day = 28,
                            hour = 0
                    ),
                    currentDate = currentDate(),
                    isPastOneDay = true
            ),
            PastOneDayDatum(
                    dateToTest = createDate(
                            year = 2018,
                            month = 7,
                            day = 29,
                            hour = 0
                    ),
                    currentDate = currentDate(),
                    isPastOneDay = true
            ),
            PastOneDayDatum(
                    dateToTest = createDate(
                            year = 2018,
                            month = 8,
                            day = 1,
                            hour = 1
                    ),
                    currentDate = currentDate(),
                    isPastOneDay = false
            ),
            PastOneDayDatum(
                    dateToTest = createDate(
                            year = 2017,
                            month = 1,
                            day = 1,
                            hour = 0
                    ),
                    currentDate = currentDate(),
                    isPastOneDay = true
            ),
            PastOneDayDatum(
                    dateToTest = createDate(
                            year = 2017,
                            month = 1,
                            day = 1,
                            hour = 0
                    ),
                    currentDate = currentDate(),
                    isPastOneDay = true
            ),
            PastOneDayDatum(
                    dateToTest = currentDate(),
                    currentDate = currentDate(),
                    isPastOneDay = false
            )
    )

    @Test
    public fun `test afterDay - shouldAllPass`() {
        pastOneDayTestData.forEach {
            Assert.assertEquals(it.isPastOneDay, it.currentDate.afterDay(it.dateToTest))
        }
    }


    private val firstQuarter = createCalendar(year = 2018, month = 0, day = 1, hour = 0)
    private val secondQuarter = createCalendar(year = 2018, month = 3, day = 1, hour = 0)
    private val thirdQuarter = createCalendar(year = 2018, month = 6, day = 1, hour = 0)
    private val fourthQuarter = createCalendar(year = 2018, month = 9, day = 1, hour = 0)
    private val fifthQuarter = createCalendar(year = 2019, month = 0, day = 1, hour = 0)
//
    private val quarterData = listOf(
        QuarterDatum(dateToTest = firstQuarter, currentDate = secondQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = firstQuarter, currentDate = firstQuarter, isCurrentDatePassedComparedDateQuarter = false),
        QuarterDatum(dateToTest = secondQuarter, currentDate = thirdQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = secondQuarter, currentDate = secondQuarter, isCurrentDatePassedComparedDateQuarter = false),
        QuarterDatum(dateToTest = thirdQuarter, currentDate = fourthQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = thirdQuarter, currentDate = thirdQuarter, isCurrentDatePassedComparedDateQuarter = false),
        QuarterDatum(dateToTest = fourthQuarter, currentDate = fifthQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = fourthQuarter, currentDate = fourthQuarter, isCurrentDatePassedComparedDateQuarter = false),
        QuarterDatum(dateToTest = fourthQuarter, currentDate = secondQuarter, isCurrentDatePassedComparedDateQuarter = false),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 0, day = 2, hour = 5), currentDate = secondQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 1, day = 2, hour = 5), currentDate = secondQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 2, day = 2, hour = 5), currentDate = secondQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 3, day = 2, hour = 5), currentDate = secondQuarter, isCurrentDatePassedComparedDateQuarter = false),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 3, day = 2, hour = 5), currentDate = thirdQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 4, day = 2, hour = 5), currentDate = thirdQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 5, day = 2, hour = 5), currentDate = thirdQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 6, day = 2, hour = 5), currentDate = thirdQuarter, isCurrentDatePassedComparedDateQuarter = false),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 6, day = 2, hour = 5), currentDate = fourthQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 7, day = 2, hour = 5), currentDate = fourthQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 8, day = 2, hour = 5), currentDate = fourthQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 9, day = 2, hour = 5), currentDate = fourthQuarter, isCurrentDatePassedComparedDateQuarter = false),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 9, day = 2, hour = 5), currentDate = fifthQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 10, day = 2, hour = 5), currentDate = fifthQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 11, day = 2, hour = 5), currentDate = fifthQuarter, isCurrentDatePassedComparedDateQuarter = true),
        QuarterDatum(dateToTest = createCalendar(year = 2018, month = 12, day = 2, hour = 5), currentDate = fifthQuarter, isCurrentDatePassedComparedDateQuarter = false)

    )

    @Test
    public fun `test isCurrentDatePastComparedDateQuarter - shouldAllPass`() {
        quarterData.forEach {
            Assert.assertEquals(it.isCurrentDatePassedComparedDateQuarter, it.currentDate.afterQuarter(it.dateToTest))
        }
    }
}

// region data classes for unit tests
private data class PastOneDayDatum(val dateToTest : Date, val currentDate: Date, val isPastOneDay: Boolean)
private data class QuarterDatum(val dateToTest: Date, val currentDate : Date, val isCurrentDatePassedComparedDateQuarter: Boolean)
// endregion