package com.virtualshape.gradule.domain.schedule

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

/**
 * Якорь чётности недели: серверный `/APIv1/week` плюс дата, к которой он относится.
 * Без даты выбранный день не с чем сравнить — «сейчас верхняя» само по себе ничего не говорит о соседних неделях.
 */
data class WeekAnchor(
    val date: LocalDate,
    val parity: Parity,
) {
    companion object {
        /**
         * Фолбэк на случай недоступной сети. Правило ISO оставлено как запасное, но якорём оно не
         * подтверждено: 2026-10-02 сервер отдал `{"week":0}` (верхняя), а ISO-неделя 40 чётная.
         * Значит при живом `/week` решает сервер; фолбэк включается только когда спросить некого.
         */
        fun guessed(today: LocalDate): WeekAnchor =
            WeekAnchor(
                today,
                if (today.get(WeekFields.ISO.weekOfWeekBasedYear()) % 2 == 1) Parity.UPPER else Parity.LOWER,
            )
    }
}