package com.virtualshape.gradule.domain.schedule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek

/** Грамматика timeslot по specs/fixtures/schedule/grammar-note.md: все 60 строк фикстур обязаны разбираться. */
class TimeslotParserTest {
    @Test
    fun `parses every timeslot of all fixtures`() {
        for ((fixture, expected) in FIXTURES) {
            val raws = timeslotsOf(fixture)
            assertEquals("размер фикстуры $fixture", expected.lessons, raws.size)
            for (raw in raws) {
                assertNotNull("$fixture: не распарсилось '$raw'", TimeslotParser.parse(raw))
            }
        }
    }

    @Test
    fun `fixture fields match expectation`() {
        for ((fixture, expected) in FIXTURES) {
            val slots =
                timeslotsOf(fixture).map { raw ->
                    assertNotNull("$fixture: не распарсилось '$raw'", TimeslotParser.parse(raw))
                    raw to TimeslotParser.parse(raw)!!
                }
            assertEquals(
                "$fixture: распределение чётности",
                expected.parities,
                slots.groupingBy { it.second.parity }.eachCount(),
            )
            assertEquals(
                "$fixture: распределение дней",
                expected.days,
                slots.groupingBy { it.second.day }.eachCount(),
            )
            // день в строке = day.value - 1 (0 = понедельник)
            for ((raw, slot) in slots) {
                assertEquals("$fixture: день '$raw'", raw[1] - '0' + 1, slot.day.value)
            }
            assertEquals(
                "$fixture: диапазон времени",
                expected.minutes,
                slots.indices.minOf { slots[it].second.startMinute }..slots.indices.maxOf { slots[it].second.endMinute },
            )
        }
    }

    @Test
    fun `known slots`() {
        assertEquals(
            Timeslot(DayOfWeek.THURSDAY, 13 * 60 + 45, 15 * 60 + 20, Parity.UPPER),
            TimeslotParser.parse("(3,13:45:00,15:20:00,upper)"),
        )
        assertEquals(
            Timeslot(DayOfWeek.MONDAY, 8 * 60, 9 * 60 + 35, Parity.LOWER),
            TimeslotParser.parse("(0,08:00:00,09:35:00,lower)"),
        )
        assertEquals(
            Timeslot(DayOfWeek.WEDNESDAY, 11 * 60 + 55, 13 * 60 + 30, Parity.FULL),
            TimeslotParser.parse("(2,11:55:00,13:30:00,full)"),
        )
        assertEquals(
            Timeslot(DayOfWeek.SATURDAY, 19 * 60 + 30, 21 * 60, Parity.FULL),
            TimeslotParser.parse("(5,19:30:00,21:00:00,full)"),
        )
    }

    @Test
    fun `rejects malformed`() {
        for (raw in listOf(
            "",
            " ",
            "0,08:00:00,09:35:00,full",
            "(0,08:00:00,09:35:00,full",
            "0,08:00:00,09:35:00,full)",
            "(0,08:00:00,09:35:00,full,extra)",
            "(0,08:00:00,full)",
            "(x,08:00:00,09:35:00,full)",
            "(6,08:00:00,09:35:00,full)",
            "(7,08:00:00,09:35:00,full)",
            "(-1,08:00:00,09:35:00,full)",
            "(0,8:00,09:35:00,full)",
            "(0,08:00:00,9:35:00,full)",
            "(0,25:00:00,26:00:00,full)",
            "(0,08:00:00,24:00:00,full)",
            "(0,08:00:00,08:60:00,full)",
            "(0,08:00:00,09:35:00,middle)",
            "(0,08:00:00,09:35:00,FULL)",
            "(00,08:00:00,09:35:00,full)",
            "(0,09:35:00,08:00:00,full)",
            "(0,08:00:00,08:00:00,full)",
            "()",
        )) {
            assertNull("ожидался отказ: '$raw'", TimeslotParser.parse(raw))
        }
    }

    /** Читает все значения `timeslot` из classpath-фикстуры (сырой JSON, без библиотеки). */
    private fun timeslotsOf(fixture: String): List<String> {
        val stream = requireNotNull(javaClass.getResourceAsStream("/fixtures/$fixture")) { "нет фикстуры $fixture" }
        val json = stream.bufferedReader().use { it.readText() }
        return Regex(""""timeslot"\s*:\s*"([^"]*)"""").findAll(json).map { it.groupValues[1] }.toList()
    }

    private class FixtureExpectation(
        val lessons: Int,
        val parities: Map<Parity, Int>,
        val days: Map<DayOfWeek, Int>,
        val minutes: IntRange,
    )

    private companion object {
        val FIXTURES =
            mapOf(
                "schedule_group_116.json" to
                    FixtureExpectation(
                        lessons = 15,
                        parities = mapOf(Parity.FULL to 10, Parity.UPPER to 3, Parity.LOWER to 2),
                        days =
                            mapOf(
                                DayOfWeek.MONDAY to 2,
                                DayOfWeek.TUESDAY to 3,
                                DayOfWeek.WEDNESDAY to 1,
                                DayOfWeek.THURSDAY to 4,
                                DayOfWeek.FRIDAY to 4,
                                DayOfWeek.SATURDAY to 1,
                            ),
                        minutes = 9 * 60 + 50..19 * 60 + 15,
                    ),
                "schedule_group_117.json" to
                    FixtureExpectation(
                        lessons = 15,
                        parities = mapOf(Parity.FULL to 15),
                        days =
                            mapOf(
                                DayOfWeek.MONDAY to 4,
                                DayOfWeek.TUESDAY to 2,
                                DayOfWeek.WEDNESDAY to 3,
                                DayOfWeek.THURSDAY to 3,
                                DayOfWeek.SATURDAY to 3,
                            ),
                        minutes = 8 * 60..17 * 60 + 25,
                    ),
                "schedule_group_185.json" to
                    FixtureExpectation(
                        lessons = 30,
                        parities = mapOf(Parity.FULL to 16, Parity.UPPER to 8, Parity.LOWER to 6),
                        days =
                            mapOf(
                                DayOfWeek.MONDAY to 6,
                                DayOfWeek.TUESDAY to 5,
                                DayOfWeek.WEDNESDAY to 5,
                                DayOfWeek.THURSDAY to 5,
                                DayOfWeek.FRIDAY to 4,
                                DayOfWeek.SATURDAY to 5,
                            ),
                        minutes = 8 * 60..19 * 60 + 15,
                    ),
            )
    }
}
