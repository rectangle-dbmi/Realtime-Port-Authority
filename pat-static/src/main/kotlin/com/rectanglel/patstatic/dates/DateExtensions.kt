package com.rectanglel.patstatic.dates

import java.util.*

/*
 * There's no notion of HTTP-caching schemes on the Port Authority server, so I had to create my own.
 * @author Jeremy Jao
 */

private fun getQuarterOfMonth(month: Int) = when {
    month >= Calendar.JANUARY && month <= Calendar.MARCH  -> 0
    month >= Calendar.APRIL && month <= Calendar.JUNE     -> 1
    month >= Calendar.JULY && month <= Calendar.SEPTEMBER -> 2
    else                                                  -> 3
}

private val hoursInMilliseconds = 1000 * 60 * 60 * 60

/**
 * Check if the quarter of the current datetime of the device is greater than the date to compare.
 * @param other the date to compare (in the app, this is the modified date of the file saved on the device)
 * @return true if the date's quarter > comparedDate's quarter especially by year
 */
fun Date.afterQuarter(other: Date) : Boolean {
    val currentCalendar = Calendar.getInstance(Locale.US)
    currentCalendar.time = this
    val currentYear = currentCalendar.get(Calendar.YEAR)

    val otherCalendar = Calendar.getInstance(Locale.US)
    otherCalendar.time = other
    val comparedYear = otherCalendar.get(Calendar.YEAR)


    if (currentYear != comparedYear) {
        return currentYear > comparedYear
    }

    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val currentQuarter = getQuarterOfMonth(currentMonth)
    val comparedMonth = otherCalendar.get(Calendar.MONTH)
    val comparedQuarter = getQuarterOfMonth(comparedMonth)



    return currentQuarter > comparedQuarter
}

/**
 * Check if the current date is 1 day after the other date.
 * @param other the date to test (usually the modified date of the cached data)
 * @return whether or not the current date is 24 hours ahead of the other date.
 */
fun Date.afterDay(other: Date) : Boolean {
    // get the current date as a `Calendar`
    val currentCalendar = Calendar.getInstance()
    currentCalendar.time = this

    // get the date to compare, subtract the day by one
    val otherCalendar = Calendar.getInstance()
    otherCalendar.time = other
    otherCalendar.add(Calendar.DAY_OF_MONTH, 1)
//    otherCalendar.add(Calendar.MILLISECOND, 1)

    return (currentCalendar.timeInMillis - otherCalendar.timeInMillis) >= 0
}
