package com.virtualshape.gradule.core.util

import com.virtualshape.gradule.data.remote.ScheduleResponse
import com.virtualshape.gradule.schedule.domain.model.Lesson

data class ParsedTimeSlot(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val weekType: String
)

fun parseTimeslot(timeslot: String): ParsedTimeSlot {
    val clean = timeslot.removePrefix("(").removeSuffix(")")
    val parts = clean.split(",")
    return ParsedTimeSlot(
        dayOfWeek = parts[0].toInt(),
        startTime = parts[1].substring(0, 5),
        endTime = parts[2].substring(0, 5),
        weekType = parts[3]
    )
}

fun mapToLessons(response: ScheduleResponse): List<Lesson> {
    return response.curricula.mapNotNull { curriculum ->
        val lessonDto = response.lessons.find { it.id == curriculum.lessonid }
            ?: return@mapNotNull null
        val slot = parseTimeslot(lessonDto.timeslot)
        Lesson(
            id = lessonDto.id.toString(),
            subject = curriculum.subjectname,
            teacher = curriculum.teachername,
            room = curriculum.roomname,
            dayOfWeek = slot.dayOfWeek,
            startTime = slot.startTime,
            endTime = slot.endTime,
            weekType = slot.weekType
        )
    }.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))
}