package com.virtualshape.gradule.domain.schedule

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

/** Склейка подгрупп одного занятия в один визуальный слот + фильтр по подгруппе. */
class ScheduleSlotsTest {
    @Test
    fun `five subgroups of one lesson make one slot`() {
        val entries = (1..5).reversed().map { entry(subnum = it) }

        val slots = ScheduleSlots.group(entries)

        assertEquals(1, slots.size)
        assertEquals(listOf(1, 2, 3, 4, 5), slots[0].entries.map { it.subgroups.first().subnum })
    }

    @Test
    fun `slots sorted by start minute`() {
        val entries =
            listOf(
                entry(subnum = 1, start = 15 * 60 + 30),
                entry(subnum = 1, start = 9 * 60 + 55),
                entry(subnum = 1, start = 13 * 60 + 45),
            )

        val slots = ScheduleSlots.group(entries)

        assertEquals(listOf(9 * 60 + 55, 13 * 60 + 45, 15 * 60 + 30), slots.map { it.timeslot.startMinute })
    }

    @Test
    fun `different parity or room-clock is a different slot`() {
        val entries =
            listOf(
                entry(subnum = 1),
                entry(subnum = 1, parity = Parity.LOWER),
                entry(subnum = 1, end = 9 * 60 + 35),
            )

        assertEquals(3, ScheduleSlots.group(entries).size)
    }

    @Test
    fun `subnum filter keeps only that subgroup`() {
        val entries = (1..5).map { entry(subnum = it) }

        val slots = ScheduleSlots.group(entries, subnum = 2)

        assertEquals(1, slots.size)
        assertEquals(listOf(2), slots[0].entries.map { it.subgroups.first().subnum })
    }

    @Test
    fun `null subnum keeps everything`() {
        val entries = (1..5).map { entry(subnum = it) }

        assertEquals(5, ScheduleSlots.group(entries, subnum = null)[0].entries.size)
    }

    @Test
    fun `slot keeps timeslot of its entries`() {
        val slot = ScheduleSlots.group(listOf(entry(subnum = 1, start = 9 * 60 + 55, end = 13 * 60 + 30)))[0]

        assertEquals(Timeslot(DayOfWeek.MONDAY, 9 * 60 + 55, 13 * 60 + 30, Parity.FULL), slot.timeslot)
    }

    private fun entry(
        subnum: Int,
        start: Int = 8 * 60,
        end: Int = 9 * 60 + 35,
        parity: Parity = Parity.FULL,
    ) = ScheduleEntry(
        key = EntryKey.Server(subnum.toLong(), subnum.toLong()),
        source = Source.SERVER,
        timeslot = Timeslot(DayOfWeek.MONDAY, start, end, parity),
        subgroups = listOf(Subgroup(subnum.toLong(), subnum, "Алгоритмы $subnum", "АСД$subnum", "Преп $subnum", "317")),
    )
}
