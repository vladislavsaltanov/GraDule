package com.virtualshape.gradule.core.util

import com.virtualshape.gradule.data.remote.ScheduleResponse
import com.virtualshape.gradule.schedule.domain.model.Lesson
import com.virtualshape.gradule.schedule.domain.model.Subgroup

data class ParsedTimeSlot(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val weekType: String
)

fun parseTimeslot(timeslot: String): ParsedTimeSlot {
    val clean = timeslot.removePrefix("(").removeSuffix(")")
    val parts = clean.split(",")
    val rawWeekType = parts[3]
    val weekType = when (rawWeekType) {
        "upper" -> "0"
        "lower" -> "1"
        else -> rawWeekType
    }
    return ParsedTimeSlot(
        dayOfWeek = parts[0].toInt(),
        startTime = parts[1].substring(0, 5),
        endTime = parts[2].substring(0, 5),
        weekType = weekType
    )
}

fun mapToLessons(response: ScheduleResponse): List<Lesson> {
    // 1. Group curricula by lessonid to form initial Lesson objects
    val initialLessons = response.curricula.groupBy { it.lessonid }.mapNotNull { (lessonId, curricula) ->
        val lessonDto = response.lessons.find { it.id == lessonId }
            ?: return@mapNotNull null
        
        val slot = parseTimeslot(lessonDto.timeslot)
        val firstCurriculum = curricula.first()
        
        Lesson(
            id = lessonId.toString(),
            subject = firstCurriculum.subjectname,
            dayOfWeek = slot.dayOfWeek,
            startTime = slot.startTime,
            endTime = slot.endTime,
            weekType = slot.weekType,
            subcount = firstCurriculum.subcount,
            subgroups = curricula.map {
                Subgroup(
                    teacher = it.teachername,
                    room = it.roomname,
                    subnum = it.subnum
                )
            }
        )
    }

    // 2. Secondary grouping by timeslot and subject to merge subgroups with different lessonids
    return initialLessons.groupBy { 
        "${it.dayOfWeek}_${it.startTime}_${it.endTime}_${it.weekType}_${it.subject}"
    }.map { (_, group) ->
        val first = group.first()
        Lesson(
            id = group.joinToString(",") { it.id },
            subject = first.subject,
            dayOfWeek = first.dayOfWeek,
            startTime = first.startTime,
            endTime = first.endTime,
            weekType = first.weekType,
            subcount = group.maxOf { it.subcount },
            subgroups = group.flatMap { it.subgroups }.sortedBy { it.subnum }
        )
    }.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))
}
