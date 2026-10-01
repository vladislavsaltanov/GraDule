package com.virtualshape.gradule.data

import com.virtualshape.gradule.domain.schedule.EntryKey
import com.virtualshape.gradule.domain.schedule.ScheduleEntry
import com.virtualshape.gradule.domain.schedule.Source
import com.virtualshape.gradule.domain.schedule.Subgroup
import com.virtualshape.gradule.domain.schedule.TimeslotParser

/** Результат маппинга: записи + число пропущенных занятий (мусор считается, загрузку не роняет). */
data class ScheduleMapping(
    val entries: List<ScheduleEntry>,
    val skippedLessons: Int,
)

/**
 * Ответ расписания → домен. Одна запись = одна строка curricula (одна подгруппа): так подгруппы
 * не схлопываются в одну запись и не теряются, а ключ устойчив — (lesson.id, curriculum.id).
 * Занятие без распарсенного timeslot или без curricula пропускается и попадает в [ScheduleMapping.skippedLessons].
 */
object ScheduleMapper {
    fun map(payload: SchedulePayload): ScheduleMapping {
        val curriculaByLesson = payload.curricula.groupBy { it.lessonid }
        val entries = mutableListOf<ScheduleEntry>()
        var skipped = 0
        for (lesson in payload.lessons) {
            val timeslot = TimeslotParser.parse(lesson.timeslot)
            val curricula = curriculaByLesson[lesson.id].orEmpty()
            if (timeslot == null || curricula.isEmpty()) {
                skipped++
                continue
            }
            for (curriculum in curricula) {
                entries +=
                    ScheduleEntry(
                        key = EntryKey.Server(lesson.id, curriculum.id),
                        source = Source.SERVER,
                        timeslot = timeslot,
                        subgroups =
                            listOf(
                                Subgroup(
                                    id = curriculum.id,
                                    subnum = curriculum.subnum,
                                    subjectName = curriculum.subjectname,
                                    subjectAbbr = curriculum.subjectabbr,
                                    teacherName = curriculum.teachername,
                                    roomName = curriculum.roomname,
                                ),
                            ),
                        info = lesson.info,
                    )
            }
        }
        return ScheduleMapping(entries, skipped)
    }
}
