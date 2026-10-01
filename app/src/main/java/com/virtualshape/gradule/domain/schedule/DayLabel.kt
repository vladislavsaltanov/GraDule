package com.virtualshape.gradule.domain.schedule

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RUSSIAN = Locale("ru")

/** Подпись выбранного дня: «понедельник • верхняя неделя» (docs/design/DESIGN.md, подпись навбара). */
object DayLabel {
    fun of(date: LocalDate): String = "${dayName(date.dayOfWeek)} • ${parityName(ScheduleQuery.weekParity(date))}"

    /**
     * Чип даты: «15 июня • 2026». Локаль фиксируем русскую — на устройстве с
     * другой локалью месяц уезжает в латиницу (ловили «1 October • 2026»).
     */
    fun date(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("d MMMM • yyyy", RUSSIAN))

    /** Минуты от полуночи → «ЧЧ:ММ» (дизайн: время пары). */
    fun time(minuteOfDay: Int): String = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

    fun timeRange(
        startMinute: Int,
        endMinute: Int,
    ): String = "${time(startMinute)} - ${time(endMinute)}"

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
