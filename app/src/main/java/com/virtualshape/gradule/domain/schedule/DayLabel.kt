package com.virtualshape.gradule.domain.schedule

import java.time.DayOfWeek
import java.time.LocalDate

/** Подпись выбранного дня: «понедельник • верхняя неделя» (docs/design/DESIGN.md, подпись навбара). */
object DayLabel {
    fun of(date: LocalDate): String = "${dayName(date.dayOfWeek)} • ${parityName(ScheduleQuery.weekParity(date))}"

    private fun dayName(day: DayOfWeek): String =
        when (day) {
            DayOfWeek.MONDAY -> "понедельник"
            DayOfWeek.TUESDAY -> "вторник"
            DayOfWeek.WEDNESDAY -> "среда"
            DayOfWeek.THURSDAY -> "четверг"
            DayOfWeek.FRIDAY -> "пятница"
            DayOfWeek.SATURDAY -> "суббота"
            DayOfWeek.SUNDAY -> "воскресенье"
        }

    private fun parityName(parity: Parity): String =
        when (parity) {
            Parity.UPPER -> "верхняя неделя"
            else -> "нижняя неделя"
        }
}
