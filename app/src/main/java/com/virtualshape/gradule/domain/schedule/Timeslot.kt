package com.virtualshape.gradule.domain.schedule

import java.time.DayOfWeek

enum class Parity { FULL, UPPER, LOWER }

/** Занятие в сетке: день недели + минуты от полуночи + чётность недели. */
data class Timeslot(
    val day: DayOfWeek,
    val startMinute: Int,
    val endMinute: Int,
    val parity: Parity,
)

/**
 * Грамматика `(D,ЧЧ:ММ:СС,ЧЧ:ММ:СС,чётность)`, D = 0..5 (0 = понедельник).
 * Всё, что не соответствует грамматике, — `null`, а не исключение.
 */
object TimeslotParser {
    fun parse(raw: String): Timeslot? {
        if (!raw.startsWith('(') || !raw.endsWith(')')) return null
        val tokens = raw.substring(1, raw.length - 1).split(',')
        if (tokens.size != 4) return null
        val day =
            tokens[0]
                .singleOrNull()
                ?.digitToIntOrNull()
                ?.takeIf { it in 0..5 }
                ?.let { DayOfWeek.of(it + 1) } ?: return null
        val start = minuteOfDay(tokens[1]) ?: return null
        val end = minuteOfDay(tokens[2]) ?: return null
        if (end <= start) return null
        val parity =
            when (tokens[3]) {
                "full" -> Parity.FULL
                "upper" -> Parity.UPPER
                "lower" -> Parity.LOWER
                else -> return null
            }
        return Timeslot(day, start, end, parity)
    }

    /** `ЧЧ:ММ:СС` ровно (секунды отбрасываются: в сетке точность до минуты). */
    private fun minuteOfDay(token: String): Int? {
        val parts = token.split(':')
        if (parts.size != 3 || parts.any { it.length != 2 }) return null
        val (hour, minute, second) = parts.map { it.toIntOrNull() ?: return null }
        if (hour !in 0..23 || minute !in 0..59 || second !in 0..59) return null
        return hour * 60 + minute
    }
}
