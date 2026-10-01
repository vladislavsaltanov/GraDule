package com.virtualshape.gradule.data

import com.virtualshape.gradule.domain.schedule.EntryKey
import com.virtualshape.gradule.domain.schedule.Parity
import com.virtualshape.gradule.domain.schedule.ScheduleEntry
import com.virtualshape.gradule.domain.schedule.Source
import com.virtualshape.gradule.domain.schedule.Timeslot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

/**
 * Маппинг `/schedule/group/{id}` в домен: одна запись = одна строка curricula (одна подгруппа),
 * ключ — (lesson.id, curriculum.id), `info` переносится как есть, мусор считается, а не роняет загрузку.
 */
class ScheduleMapperTest {
    private val url = ScheduleApi.scheduleForGroup(185)
    private val payload = ScheduleJson.decodeSchedule(FixtureHttpGet.fixture("schedule_group_185.json"), url)
    private val mapping = ScheduleMapper.map(payload)

    @Test
    fun `one entry per curriculum, keys unique`() {
        assertEquals(40, mapping.entries.size)
        assertEquals(0, mapping.skippedLessons)
        assertEquals(40, payload.curricula.size)
        assertEquals(
            40,
            mapping.entries
                .map(ScheduleEntry::key)
                .toSet()
                .size,
        )
        assertTrue(mapping.entries.all { it.source == Source.SERVER })
    }

    @Test
    fun `every lesson of fixture became entries with parsed timeslot`() {
        val byLesson = mapping.entries.groupBy { (it.key as EntryKey.Server).lessonId }

        assertEquals(payload.lessons.map { it.id }.toSet(), byLesson.keys)
        assertEquals(
            1,
            byLesson
                .getValue(3085)
                .map(ScheduleEntry::timeslot)
                .toSet()
                .size,
        )
        assertTrue(mapping.entries.all { it.timeslot.endMinute > it.timeslot.startMinute })
    }

    @Test
    fun `known slot maps to monday upper`() {
        val entry = mapping.entries.single { it.key == EntryKey.Server(3085, 5679) }

        assertEquals(Timeslot(DayOfWeek.MONDAY, 13 * 60 + 45, 15 * 60 + 20, Parity.UPPER), entry.timeslot)
        assertEquals("ИнЯз", entry.subgroups.single().subjectAbbr)
        assertEquals("208", entry.subgroups.single().roomName)
    }

    @Test
    fun `info carried as is`() {
        assertEquals("1 поток", mapping.entries.single { it.key == EntryKey.Server(3077, 5654) }.info)
        assertEquals(12, mapping.entries.count { it.info.isNotEmpty() })
    }

    @Test
    fun `subgroups of one lesson keep the same timeslot and stay separate`() {
        val subgroupEntries = mapping.entries.filter { (it.key as EntryKey.Server).lessonId == 3085L }

        assertEquals(5, subgroupEntries.size)
        assertEquals(1, subgroupEntries.map(ScheduleEntry::timeslot).toSet().size)
        assertEquals(listOf(5679L, 5680L, 5681L, 5682L, 5683L), subgroupEntries.map { (it.key as EntryKey.Server).curriculumId })
        assertEquals(listOf(1, 2, 3, 4, 5), subgroupEntries.map { it.subgroups.single().subnum }.sorted())
        assertEquals(5, subgroupEntries.map { it.subgroups.single().roomName }.toSet().size)
    }

    @Test
    fun `lesson without parsed timeslot or without curricula is counted as skipped`() {
        val broken =
            SchedulePayload(
                lessons =
                    listOf(
                        LessonDto(id = 1, timeslot = "(9,99:99:00,99:99:00,middle)"),
                        LessonDto(id = 2, timeslot = "(0,08:00:00,09:35:00,lower)"),
                        LessonDto(id = 3, timeslot = "(1,08:00:00,09:35:00,full)"),
                    ),
                curricula = listOf(CurriculumDto(id = 20, lessonid = 2, subnum = 1, subjectabbr = "МАТ")),
            )

        val mapped = ScheduleMapper.map(broken)

        assertEquals(2, mapped.skippedLessons)
        assertEquals(listOf(EntryKey.Server(2, 20)), mapped.entries.map(ScheduleEntry::key))
    }

    @Test
    fun `broken json gives understandable error`() {
        val broken = assertThrows(ScheduleDataException::class.java) { ScheduleJson.decodeSchedule("{не json", url) }

        assertEquals(url, broken.url)
        assertTrue(broken.cause != null)
    }

    @Test
    fun `empty response gives understandable error`() {
        val empty = assertThrows(ScheduleDataException::class.java) { ScheduleJson.decodeSchedule("", url) }

        assertEquals(url, empty.url)
    }
}
