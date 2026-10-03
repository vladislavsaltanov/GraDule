package com.virtualshape.gradule.domain.schedule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/** Записи на дату: WEEKLY (день + чётность) и ONE_OFF (только своя дата). */
class ScheduleQueryTest {
    @Test
    fun `one-off entry lands only on its own date`() {
        val monday = weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "МАТ")
        val oneOff = oneOff(LocalDate.of(2026, 6, 16), 10 * 60, "СР")
        val entries = listOf(oneOff, monday)

        assertEquals(listOf("СР"), abbrs(ScheduleQuery.entriesOn(LocalDate.of(2026, 6, 16), entries, Anchor)))
        assertEquals(listOf("МАТ"), abbrs(ScheduleQuery.entriesOn(LocalDate.of(2026, 6, 15), entries, Anchor)))
        assertEquals(emptyList<String>(), abbrs(ScheduleQuery.entriesOn(LocalDate.of(2026, 6, 17), entries, Anchor)))
    }

    @Test
    fun `weekly parity follows the date`() {
        val entries =
            listOf(
                weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "FULL"),
                weekly(Parity.UPPER, DayOfWeek.MONDAY, 9 * 60, "UPPER"),
                weekly(Parity.LOWER, DayOfWeek.MONDAY, 10 * 60, "LOWER"),
            )

        assertEquals(listOf("FULL", "UPPER"), abbrs(ScheduleQuery.entriesOn(UpperMonday, entries, June)))
        assertEquals(listOf("FULL", "LOWER"), abbrs(ScheduleQuery.entriesOn(LowerMonday, entries, June)))
    }

    @Test
    fun `parity follows the server anchor not the iso week number`() {
        // 2026-10-02 — ISO-неделя 40, чётная: старая гипотеза дала бы LOWER. Сервер говорит UPPER.
        assertEquals(Parity.UPPER, ScheduleQuery.weekParity(Fri, Anchor))
        assertEquals(Parity.UPPER, ScheduleQuery.weekParity(Fri.minusDays(2), Anchor))
        assertEquals(Parity.LOWER, ScheduleQuery.weekParity(Fri.plusDays(3), Anchor))
        assertEquals(Parity.LOWER, ScheduleQuery.weekParity(Fri.plusDays(7), Anchor))
        assertEquals(Parity.UPPER, ScheduleQuery.weekParity(Fri.plusDays(10), Anchor))
    }

    @Test
    fun `parity flips across the new year`() {
        val newYear = WeekAnchor(LocalDate.of(2026, 12, 31), Parity.UPPER)

        assertEquals(Parity.UPPER, ScheduleQuery.weekParity(LocalDate.of(2026, 12, 31), newYear))
        assertEquals(Parity.LOWER, ScheduleQuery.weekParity(LocalDate.of(2027, 1, 4), newYear))
        assertEquals(Parity.UPPER, ScheduleQuery.weekParity(LocalDate.of(2027, 1, 11), newYear))
    }

    @Test
    fun `guessed anchor is only a fallback and matches the old iso rule`() {
        assertEquals(Parity.LOWER, WeekAnchor.guessed(LocalDate.of(2026, 10, 2)).parity)
        assertEquals(Parity.UPPER, WeekAnchor.guessed(LocalDate.of(2026, 10, 5)).parity)
    }

    @Test
    fun `sorted by start minute then subject abbr`() {
        val entries =
            listOf(
                weekly(Parity.FULL, DayOfWeek.MONDAY, 10 * 60, "З"),
                weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "Я"),
                weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "А"),
            )

        assertEquals(listOf("А", "Я", "З"), abbrs(ScheduleQuery.entriesOn(UpperMonday, entries, June)))
    }

    @Test
    fun `subgroups stay inside one entry`() {
        val subgroups = (1..5).map { Subgroup(it.toLong(), it, "ИнЯз $it", "ИНЯЗ$it", "Преп $it", "120") }
        val entry = weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "ИНЯЗ1", subgroups = subgroups)

        val onDate = ScheduleQuery.entriesOn(UpperMonday, listOf(entry), June)

        assertEquals(1, onDate.size)
        assertEquals(5, onDate[0].subgroups.size)
        assertEquals("Преп 3", onDate[0].subgroups[2].teacherName)
    }

    @Test
    fun `info is delivered with the time`() {
        val entry = weekly(Parity.UPPER, DayOfWeek.MONDAY, 8 * 60, "МАТ", info = "с 01.11.2026")

        val onDate = ScheduleQuery.entriesOn(UpperMonday, listOf(entry), June)

        assertEquals(1, onDate.size)
        assertEquals("с 01.11.2026", onDate[0].info)
        assertTrue(onDate[0].info.isNotBlank())
    }

    @Test
    fun `local entries are queryable like server ones`() {
        val local =
            ScheduleEntry(
                key = EntryKey.Local("uuid-1"),
                source = Source.LOCAL,
                timeslot = Timeslot(DayOfWeek.MONDAY, 8 * 60, 9 * 60 + 35, Parity.FULL),
                subgroups = listOf(Subgroup(1, 1, "Доп", "ДОП", "Преп", "10")),
            )

        val onDate = ScheduleQuery.entriesOn(UpperMonday, listOf(local), June)

        assertEquals(1, onDate.size)
        assertEquals("Доп", onDate[0].subgroups.first().subjectName)
        assertFalse(ScheduleQuery.entriesOn(LowerMonday, listOf(local), June).isEmpty())
    }

    private fun abbrs(entries: List<ScheduleEntry>) = entries.map { it.subgroups.first().subjectAbbr }

    private fun weekly(
        parity: Parity,
        day: DayOfWeek,
        startMinute: Int,
        abbr: String,
        info: String = "",
        subgroups: List<Subgroup> = listOf(Subgroup(1, 1, "Предмет $abbr", abbr, "Преп", "120")),
    ) = ScheduleEntry(
        key = EntryKey.Server(lessonId = startMinute.toLong(), curriculumId = 1),
        source = Source.SERVER,
        timeslot = Timeslot(day, startMinute, startMinute + 60, parity),
        subgroups = subgroups,
        info = info,
    )

    private fun oneOff(
        date: LocalDate,
        startMinute: Int,
        abbr: String,
    ) = ScheduleEntry(
        key = EntryKey.Local("uuid-$date"),
        source = Source.LOCAL,
        timeslot = Timeslot(DayOfWeek.SUNDAY, startMinute, startMinute + 60, Parity.LOWER),
        subgroups = listOf(Subgroup(1, 1, "Предмет $abbr", abbr, "Преп", "120")),
        onceDate = date,
    )

    private companion object {
        val UpperMonday = LocalDate.of(2026, 6, 15)
        val LowerMonday = LocalDate.of(2026, 6, 22)
        val Fri = LocalDate.of(2026, 10, 2)
        val Anchor = WeekAnchor(Fri, Parity.UPPER)
        val June = WeekAnchor(UpperMonday, Parity.UPPER)
    }
}
