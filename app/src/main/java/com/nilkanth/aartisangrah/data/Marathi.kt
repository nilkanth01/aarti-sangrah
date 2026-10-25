package com.nilkanth.aartisangrah.data

import java.time.DayOfWeek
import java.time.LocalDate

object Marathi {
    private const val DIGITS = "०१२३४५६७८९"

    private val days = mapOf(
        DayOfWeek.MONDAY to "सोमवार",
        DayOfWeek.TUESDAY to "मंगळवार",
        DayOfWeek.WEDNESDAY to "बुधवार",
        DayOfWeek.THURSDAY to "गुरुवार",
        DayOfWeek.FRIDAY to "शुक्रवार",
        DayOfWeek.SATURDAY to "शनिवार",
        DayOfWeek.SUNDAY to "रविवार",
    )

    private val months = listOf(
        "जानेवारी", "फेब्रुवारी", "मार्च", "एप्रिल", "मे", "जून",
        "जुलै", "ऑगस्ट", "सप्टेंबर", "ऑक्टोबर", "नोव्हेंबर", "डिसेंबर",
    )

    /** The deity traditionally worshipped on each weekday in Maharashtra. */
    private val dayDeity = mapOf(
        DayOfWeek.MONDAY to "shankar",
        DayOfWeek.TUESDAY to "ganpati",
        DayOfWeek.WEDNESDAY to "vitthal",
        DayOfWeek.THURSDAY to "datta",
        DayOfWeek.FRIDAY to "devi",
        DayOfWeek.SATURDAY to "maruti",
        DayOfWeek.SUNDAY to "surya",
    )

    fun num(n: Int): String = n.toString().map { c -> if (c in '0'..'9') DIGITS[c - '0'] else c }.joinToString("")

    fun day(date: LocalDate = LocalDate.now()): String = days.getValue(date.dayOfWeek)

    fun date(date: LocalDate = LocalDate.now()): String =
        "${day(date)}, ${num(date.dayOfMonth)} ${months[date.monthValue - 1]}"

    fun deityOfDay(date: LocalDate = LocalDate.now()): String = dayDeity.getValue(date.dayOfWeek)
}
