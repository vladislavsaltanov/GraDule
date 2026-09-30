package com.virtualshape.gradule.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Разбор ответов расписания: мусор/пустой ответ — [ScheduleDataException] с url, не голое исключение. */
object ScheduleJson {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = false
        }

    fun decodeGroups(
        body: String,
        url: String,
    ): List<GroupDto> = decode(body, url)

    fun decodeSchedule(
        body: String,
        url: String,
    ): SchedulePayload = decode(body, url)

    private inline fun <reified T> decode(
        body: String,
        url: String,
    ): T =
        try {
            json.decodeFromString<T>(body)
        } catch (e: SerializationException) {
            throw ScheduleDataException("не разобрался ответ $url: ${e.message}", url, e)
        } catch (e: IllegalArgumentException) {
            throw ScheduleDataException("пустой или битый ответ $url: ${e.message}", url, e)
        }
}

/** Ответ `/schedule/group/{groupId}`: занятия + curricula (подгруппы), связь `curricula.lessonid → lessons.id`. */
@Serializable
data class SchedulePayload(
    val lessons: List<LessonDto> = emptyList(),
    val curricula: List<CurriculumDto> = emptyList(),
)

/** Одно вхождение в сетке (день+время+чётность); `subcount` — число подгрупп. */
@Serializable
data class LessonDto(
    val id: Long,
    val subcount: Int = 1,
    val timeslot: String,
    val info: String = "",
)

/** Подгруппа/аудитория внутри занятия. Пустые строки отдаёт сервер — оставляем как есть, отображать как есть. */
@Serializable
data class CurriculumDto(
    val id: Long,
    val lessonid: Long,
    val subnum: Int,
    val subjectname: String = "",
    val subjectabbr: String = "",
    val teachername: String = "",
    val roomname: String = "",
)
