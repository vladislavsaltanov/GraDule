package com.virtualshape.gradule.data

import com.virtualshape.gradule.domain.schedule.EntryKey
import com.virtualshape.gradule.domain.schedule.Parity
import com.virtualshape.gradule.domain.schedule.ScheduleEntry
import com.virtualshape.gradule.domain.schedule.Source
import com.virtualshape.gradule.domain.schedule.Subgroup
import com.virtualshape.gradule.domain.schedule.Timeslot
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * Замена SERVER-набора по плану ScheduleReconcile: точечный upsert по ключу (без REPLACE/CASCADE),
 * пропавшие ключи удаляются, LOCAL не трогаем, повторная загрузка идемпотентна.
 * Хранилище — in-memory фейк, сеть — фикстуры с classpath.
 */
class ScheduleServerSetReplacementTest {
    private val groupUrl = ScheduleApi.groupsForGrade(1)
    private val groupsJson = FixtureHttpGet.fixture("group_for_grade_1.json")
    private val group185Json = FixtureHttpGet.fixture("schedule_group_185.json")
    private val groupKey = GroupKey(gradeId = 1, num = 7, name = "ММ и ИИ")

    @Test
    fun `load stores server entries and repeated load does not duplicate`() {
        val store = InMemoryScheduleStore()
        val repo = repository(store, group185Json)

        val first = repo.load(groupKey)
        val second = repo.load(groupKey)

        assertEquals(GroupLoad.Loaded(185L, entries = 40, skippedLessons = 0, deleted = 0), first)
        assertEquals(first, second)
        assertEquals(40, store.entries().size)
        assertTrue(store.entries().all { it.source == Source.SERVER })
    }

    @Test
    fun `vanished server entry is deleted`() {
        val store = InMemoryScheduleStore()
        repository(store, json(payload(lesson(1, LOW_MONDAY), lesson(2, TUESDAY_FULL), curriculum(21, 1)))).load(groupKey)
        assertEquals(3, store.entries().size)

        val result = repository(store, json(payload(lesson(1, LOW_MONDAY), curriculum(11, 1), curriculum(12, 1)))).load(groupKey)

        assertEquals(GroupLoad.Loaded(185L, entries = 2, skippedLessons = 0, deleted = 1), result)
        assertEquals(listOf(EntryKey.Server(1, 11), EntryKey.Server(1, 12)), store.entries().map(ScheduleEntry::key))
    }

    @Test
    fun `local entries survive server set replacement`() {
        val store = InMemoryScheduleStore()
        store.upsert(listOf(localEntry("uuid-1")))

        repository(store, json(payload(lesson(1, LOW_MONDAY), curriculum(11, 1)))).load(groupKey)

        assertEquals(
            listOf(EntryKey.Local("uuid-1"), EntryKey.Server(1, 11)),
            store.entries().map(ScheduleEntry::key),
        )
    }

    @Test
    fun `note remap applied when lesson id changed under same human key`() {
        val store = InMemoryScheduleStore()
        store.upsert(ScheduleMapper.map(payload(lesson(3085, MONDAY_UPPER), curriculum(5679, 3085, "ИнЯз"))).entries)

        repository(store, json(payload(lesson(9001, MONDAY_UPPER), curriculum(5679, 9001, "ИнЯз")))).load(groupKey)

        assertEquals(listOf(mapOf(EntryKey.Server(3085, 5679) to EntryKey.Server(9001, 5679))), store.appliedNoteRemaps)
        assertEquals(listOf(EntryKey.Server(9001, 5679)), store.entries().map(ScheduleEntry::key))
    }

    @Test
    fun `unresolved group changes nothing in store`() {
        val store = InMemoryScheduleStore()
        val repo = repository(store, group185Json)

        val result = repo.load(GroupKey(gradeId = 1, num = 9, name = "ММ и ИИ"))

        assertEquals(GroupLoad.Unresolved(GroupKey(gradeId = 1, num = 9, name = "ММ и ИИ")), result)
        assertTrue(store.entries().isEmpty())
    }

    @Test
    fun `broken schedule response fails before touching store`() {
        val store = InMemoryScheduleStore()
        store.upsert(listOf(localEntry("uuid-1")))
        val repo = repository(store, "{не json")

        val error = runCatching { repo.load(groupKey) }.exceptionOrNull()

        assertTrue("ожидалась ScheduleDataException, а не $error", error is ScheduleDataException)
        assertEquals(ScheduleApi.scheduleForGroup(185), (error as ScheduleDataException).url)
        assertEquals(listOf(EntryKey.Local("uuid-1")), store.entries().map(ScheduleEntry::key))
    }

    private fun repository(
        store: ScheduleStore,
        scheduleJson: String,
    ) = ScheduleRepository(
        FixtureHttpGet(mapOf(groupUrl to groupsJson, ScheduleApi.scheduleForGroup(185) to scheduleJson)),
        store,
    )

    private fun lesson(
        id: Long,
        timeslot: String,
    ) = LessonDto(id = id, timeslot = timeslot)

    private fun curriculum(
        id: Long,
        lessonId: Long,
        subjectAbbr: String = "МАТ",
    ) = CurriculumDto(id = id, lessonid = lessonId, subnum = 1, subjectname = "Математика", subjectabbr = subjectAbbr, roomname = "101")

    private fun payload(
        vararg lessons: LessonDto,
        vararg curricula: CurriculumDto,
    ) = SchedulePayload(lessons.toList(), curricula.toList())

    private fun json(payload: SchedulePayload) = Json.encodeToString(payload)

    private fun localEntry(uuid: String) =
        ScheduleEntry(
            key = EntryKey.Local(uuid),
            source = Source.LOCAL,
            timeslot = Timeslot(DayOfWeek.MONDAY, 8 * 60, 9 * 60 + 35, Parity.FULL),
            subgroups = listOf(Subgroup(1, 1, "Доп", "ДОП", "Преп", "10")),
        )

    private companion object {
        const val LOW_MONDAY = "(0,08:00:00,09:35:00,lower)"
        const val MONDAY_UPPER = "(0,13:45:00,15:20:00,upper)"
        const val TUESDAY_FULL = "(1,08:00:00,09:35:00,full)"
    }
}
