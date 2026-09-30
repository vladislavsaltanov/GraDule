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

        assertEquals(listOf("СР"), abbrs(ScheduleQuery.entriesOn(LocalDate.of(2026, 6, 16), entries)))
        assertEquals(listOf("МАТ"), abbrs(ScheduleQuery.entriesOn(LocalDate.of(2026, 6, 15), entries)))
        assertEquals(listOf("МАТ"), abbrs(ScheduleQuery.entriesOn(LocalDate.of(2026, 6, 17), entries)))
    }

    @Test
    fun `weekly parity follows the date`() {
        val entries =
            listOf(
                weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "FULL"),
                weekly(Parity.UPPER, DayOfWeek.MONDAY, 9 * 60, "UPPER"),
                weekly(Parity.LOWER, DayOfWeek.MONDAY, 10 * 60, "LOWER"),
            )

        assertEquals(listOf("FULL", "UPPER"), abbrs(ScheduleQuery.entriesOn(UpperMonday, entries)))
        assertEquals(listOf("FULL", "LOWER"), abbrs(ScheduleQuery.entriesOn(LowerMonday, entries)))
    }

    @Test
    fun `parity anchor is odd iso week upper even lower`() {
        assertEquals(Parity.UPPER, ScheduleQuery.weekParity(UpperMonday))
        assertEquals(Parity.LOWER, ScheduleQuery.weekParity(LowerMonday))
        assertEquals(Parity.UPPER, ScheduleQuery.weekParity(LocalDate.of(2026, 9, 28)))
        assertEquals(Parity.LOWER, ScheduleQuery.weekParity(LocalDate.of(2026, 10, 5)))
    }

    @Test
    fun `sorted by start minute then subject abbr`() {
        val entries =
            listOf(
                weekly(Parity.FULL, DayOfWeek.MONDAY, 10 * 60, "З"),
                weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "Я"),
                weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "А"),
            )

        assertEquals(listOf("А", "Я", "З"), abbrs(ScheduleQuery.entriesOn(UpperMonday, entries)))
    }

    @Test
    fun `subgroups stay inside one entry`() {
        val subgroups = (1..5).map { Subgroup(it, it, "ИнЯз $it", "ИНЯЗ$it", "Преп $it", "120") }
        val entry = weekly(Parity.FULL, DayOfWeek.MONDAY, 8 * 60, "ИНЯЗ1", subgroups = subgroups)

        val onDate = ScheduleQuery.entriesOn(UpperMonday, listOf(entry))

        assertEquals(1, onDate.size)
        assertEquals(5, onDate[0].subgroups.size)
        assertEquals("Преп 3", onDate[0].subgroups[2].teacherName)
    }

    @Test
    fun `info is delivered with the time`() {
        val entry = weekly(Parity.UPPER, DayOfWeek.MONDAY, 8 * 60, "МАТ", info = "с 01.11.2026")

        val onDate = ScheduleQuery.entriesOn(UpperMonday, listOf(entry))

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

        val onDate = ScheduleQuery.entriesOn(UpperMonday, listOf(local))

        assertEquals(1, onDate.size)
        assertEquals("Доп", onDate[0].subgroups.first().subjectName)
        assertFalse(ScheduleQuery.entriesOn(LowerMonday, listOf(local)).isEmpty())
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
    }
}
