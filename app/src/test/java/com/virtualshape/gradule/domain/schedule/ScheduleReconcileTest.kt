package com.virtualshape.gradule.domain.schedule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * Reconcile серверного набора: incoming — полный новый набор SERVER,
 * LOCAL не трогаем, REPLACE/CASCADE запрещены (только явный план).
 */
class ScheduleReconcileTest {
    @Test
    fun `local entries survive untouched`() {
        val existing = listOf(server(1, 1, "МАТ"), local("uuid-1"))
        val incoming = listOf(server(1, 1, "МАТ"), server(2, 1, "ФИЗ"))

        val plan = ScheduleReconcile.plan(existing, incoming)

        assertEquals(listOf(EntryKey.Local("uuid-1")), plan.untouchedLocal)
        assertTrue(plan.deletions.none { it is EntryKey.Local })
        assertEquals(2, plan.upserts.size)
        assertTrue(plan.upserts.all { it.source == Source.SERVER })
        assertTrue(plan.noteRemap.isEmpty())
    }

    @Test
    fun `vanished server entry goes to deletions`() {
        val existing = listOf(server(1, 1, "МАТ"), server(7, 3, "СТАРЫЙ"))
        val incoming = listOf(server(1, 1, "МАТ"))

        val plan = ScheduleReconcile.plan(existing, incoming)

        assertEquals(listOf(EntryKey.Server(7, 3)), plan.deletions)
    }

    @Test
    fun `unchanged server key gives no remap`() {
        val existing = listOf(server(3085, 5655, "ОРГ"))
        val incoming = listOf(server(3085, 5655, "ОРГ"))

        val plan = ScheduleReconcile.plan(existing, incoming)

        assertEquals(listOf(EntryKey.Server(3085, 5655)), plan.upserts.map { it.key })
        assertTrue(plan.deletions.isEmpty())
        assertTrue(plan.noteRemap.isEmpty())
    }

    @Test
    fun `changed lesson id with same derived key remaps notes`() {
        val old = server(3085, 5655, "ОРГ")
        val new = server(9001, 5655, "ОРГ")
        val incoming = listOf(server(3085, 5655, "ФИЗ"), new)

        val plan = ScheduleReconcile.plan(listOf(old), incoming)

        assertEquals(mapOf(EntryKey.Server(3085, 5655) to EntryKey.Server(9001, 5655)), plan.noteRemap)
    }

    @Test
    fun `ambiguous derived key gives no remap`() {
        val incoming = listOf(server(9001, 7001, "ОРГ"), server(9002, 7002, "ОРГ"))
        val existing = listOf(server(3001, 7001, "ОРГ"), server(3002, 7002, "ОРГ"))

        val plan = ScheduleReconcile.plan(existing, incoming)

        assertTrue(plan.noteRemap.isEmpty())
    }

    @Test
    fun `local keys never take part in remap`() {
        val localEntry = local("uuid-1")
        val incoming = listOf(server(9001, 7001, "ДОП"))

        val plan = ScheduleReconcile.plan(listOf(localEntry), incoming)

        assertTrue(plan.noteRemap.isEmpty())
        assertEquals(listOf(EntryKey.Local("uuid-1")), plan.untouchedLocal)
    }

    private fun server(
        lessonId: Long,
        curriculumId: Long,
        abbr: String,
    ) = ScheduleEntry(
        key = EntryKey.Server(lessonId, curriculumId),
        source = Source.SERVER,
        timeslot = Timeslot(DayOfWeek.MONDAY, 8 * 60, 9 * 60 + 35, Parity.FULL),
        subgroups = listOf(Subgroup(curriculumId, 1, "Предмет $abbr", abbr, "Преп", "120")),
    )

    private fun local(uuid: String) =
        ScheduleEntry(
            key = EntryKey.Local(uuid),
            source = Source.LOCAL,
            timeslot = Timeslot(DayOfWeek.MONDAY, 8 * 60, 9 * 60 + 35, Parity.FULL),
            subgroups = listOf(Subgroup(1, 1, "Доп", "ДОП", "Преп", "10")),
        )
}
