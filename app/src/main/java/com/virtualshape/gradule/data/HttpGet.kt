package com.virtualshape.gradule.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/** Сеть за интерфейсом: реализация подменяется фейком в юнит-тестах, сеть в тестах не используется. */
interface HttpGet {
    /** Тело GET-ответа; сетевые сбои — [ScheduleDataException], а не голое [IOException]. */
    fun get(url: String): String
}

/** Публичный REST расписания ЮФУ, без авторизации. Пути — по specs/fixtures/schedule/grammar-note.md. */
object ScheduleApi {
    const val BASE_URL = "https://schedule.sfedu.ru/APIv1"

    fun groupsForGrade(gradeId: Long): String = "$BASE_URL/group/forGrade/$gradeId"

    fun scheduleForGroup(groupId: Long): String = "$BASE_URL/schedule/group/$groupId"

    /** Текущая неделя семестра: чётное значение — верхняя, нечётное — нижняя. */
    fun week(): String = "$BASE_URL/week"
}

/** Реализация [HttpGet] на OkHttp. */
class OkHttpHttpGet(
    private val client: OkHttpClient = OkHttpClient(),
) : HttpGet {
    override fun get(url: String): String =
        try {
            client
                .newCall(
                    Request
                        .Builder()
                        .url(url)
                        .get()
                        .build(),
                ).execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        throw ScheduleDataException("HTTP ${response.code} $url", url)
                    }
                    response.body.string()
                }
        } catch (e: IOException) {
            throw ScheduleDataException("сеть недоступна: ${e.message}", url, e)
        }
}

/** Ошибка загрузки/разбора данных расписания: всегда с url и причиной, голых исключений наверх не пропускаем. */
class ScheduleDataException(
    message: String,
    val url: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
