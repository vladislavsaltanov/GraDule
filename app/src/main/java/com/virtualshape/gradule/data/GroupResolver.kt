package com.virtualshape.gradule.data

import kotlinx.serialization.Serializable

/**
 * Человеческий ключ группы (B5): id — кэш, ключ переживает смену id.
 * [gradeId] = выбранный курс (из `/grade/list`), [num] = номер группы, [name] = направление.
 */
data class GroupKey(
    val gradeId: Long,
    val num: Int,
    val name: String,
)

/** Резолв числового id группы по [GroupKey] — при каждой загрузке заново. */
class GroupResolver(
    private val http: HttpGet,
) {
    /** id группы или `null` — направление/номер больше не существует,group надо перевыбрать. */
    fun resolve(key: GroupKey): Long? {
        val url = ScheduleApi.groupsForGrade(key.gradeId)
        val groups = ScheduleJson.decodeGroups(http.get(url), url)
        return groups.singleOrNull { it.gradeid == key.gradeId && it.num == key.num && it.name == key.name }?.id
    }
}

/** Список групп курса, как отдаёт `/group/forGrade/{gradeId}`. */
@Serializable
data class GroupDto(
    val id: Long,
    val num: Int,
    val name: String,
    val gradeid: Long,
)
