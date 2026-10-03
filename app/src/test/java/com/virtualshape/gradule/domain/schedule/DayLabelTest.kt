package com.virtualshape.gradule.domain.schedule

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

/** Подпись экрана дня: день недели + чётность по серверному якорю ([WeekAnchor]). */
class DayLabelTest {
    private val defaultLocale = Locale.getDefault()

    @After
    fun `restore locale`() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `label names the parity of the anchor week and the one after it`() {
        assertEquals("понедельник • верхняя неделя", DayLabel.of(LocalDate.of(2026, 10, 5), Anchor))
        assertEquals("понедельник • нижняя неделя", DayLabel.of(LocalDate.of(2026, 10, 12), Anchor))
    }

    @Test
    fun `day name is nominative and russian`() {
        assertEquals("вторник", DayLabel.of(LocalDate.of(2026, 6, 16), Anchor).substringBefore(" • "))
        assertEquals("воскресенье", DayLabel.of(LocalDate.of(2026, 6, 21), Anchor).substringBefore(" • "))
    }

    @Test
    fun `label agrees with schedule query parity`() {
        listOf(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 22), LocalDate.of(2026, 10, 5)).forEach { date ->
            val expected =
                when (ScheduleQuery.weekParity(date, Anchor)) {
                    Parity.UPPER -> "верхняя неделя"
                    Parity.LOWER -> "нижняя неделя"
                    Parity.FULL -> error("у даты нет FULL-недели")
                }
            assertEquals(expected, DayLabel.of(date, Anchor).substringAfter("• "))
        }
    }

    private companion object {
        val Anchor = WeekAnchor(LocalDate.of(2026, 6, 15), Parity.UPPER)
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

    @Test
    fun `date chip is russian regardless of device locale`() {
        Locale.setDefault(Locale.US) // на эмуляторе было «1 October • 2026»
        assertEquals("1 октября • 2026", DayLabel.date(LocalDate.of(2026, 10, 1)))
        assertEquals("15 июня • 2026", DayLabel.date(LocalDate.of(2026, 6, 15)))
        assertEquals("5 января • 2026", DayLabel.date(LocalDate.of(2026, 1, 5)))
    }
}
