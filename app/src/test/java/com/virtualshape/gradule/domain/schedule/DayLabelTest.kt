package com.virtualshape.gradule.domain.schedule

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Подпись экрана дня: день недели + чётность, якорь — [ScheduleQuery.weekParity]. */
class DayLabelTest {
    @Test
    fun `upper week monday`() {
        assertEquals("понедельник • верхняя неделя", DayLabel.of(LocalDate.of(2026, 6, 15)))
    }

    @Test
    fun `lower week monday`() {
        assertEquals("понедельник • нижняя неделя", DayLabel.of(LocalDate.of(2026, 6, 22)))
    }

    @Test
    fun `label follows parity anchor across the year`() {
        assertEquals("верхняя неделя", DayLabel.of(LocalDate.of(2026, 10, 5)).substringAfter("• "))
        assertEquals("нижняя неделя", DayLabel.of(LocalDate.of(2026, 9, 28)).substringAfter("• "))
    }

    @Test
    fun `day name is nominative and russian`() {
        assertEquals("вторник", DayLabel.of(LocalDate.of(2026, 6, 16)).substringBefore(" • "))
        assertEquals("воскресенье", DayLabel.of(LocalDate.of(2026, 6, 21)).substringBefore(" • "))
    }

    @Test
    fun `label agrees with schedule query parity`() {
        listOf(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 22), LocalDate.of(2026, 10, 5)).forEach { date ->
            val expected =
                when (ScheduleQuery.weekParity(date)) {
                    Parity.UPPER -> "верхняя неделя"
                    Parity.LOWER -> "нижняя неделя"
                    Parity.FULL -> error("у даты нет FULL-недели")
                }
            assertEquals(expected, DayLabel.of(date).substringAfter("• "))
        }
    }

    @Test
    fun `time renders minutes of day as hh mm`() {
        assertEquals("00:00", DayLabel.time(0))
        assertEquals("08:15", DayLabel.time(8 * 60 + 15))
        assertEquals("23:59", DayLabel.time(23 * 60 + 59))
    }

    @Test
    fun `time range keeps the dash of the design`() {
        assertEquals("11:55 - 13:30", DayLabel.timeRange(11 * 60 + 55, 13 * 60 + 30))
    }
}
