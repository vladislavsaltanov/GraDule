package com.virtualshape.gradule.data

import com.virtualshape.gradule.domain.schedule.ScheduleReconcile

/** Итог загрузки: записи записаны, либо группу надо перевыбрать (id исчез/сменился). */
sealed interface GroupLoad {
    data class Loaded(
        val groupId: Long,
        val entries: Int,
        val skippedLessons: Int,
        val deleted: Int,
    ) : GroupLoad

    data class Unresolved(
        val key: GroupKey,
    ) : GroupLoad
}

/**
 * Сценарий загрузки целиком: пере-резолв id группы по human-ключу (B5) → загрузка шаблона →
 * план замены → точечное применение. LOCAL-записи не участвуют и не трогаются.
 */
class ScheduleRepository(
    private val http: HttpGet,
    private val store: ScheduleStore,
) {
    private val groups = GroupResolver(http)

    /** id группы по human-ключу; `null` — направления/номера больше нет, нужен выбор заново. */
    fun resolveGroup(key: GroupKey): Long? = groups.resolve(key)

    /** Загрузка и замена SERVER-набора. Ошибка сети/разбора — [ScheduleDataException], хранилище не трогаем. */
    fun load(key: GroupKey): GroupLoad {
        val groupId = groups.resolve(key) ?: return GroupLoad.Unresolved(key)
        val url = ScheduleApi.scheduleForGroup(groupId)
        val mapping = ScheduleMapper.map(ScheduleJson.decodeSchedule(http.get(url), url))
        val plan = ScheduleReconcile.plan(store.entries(), mapping.entries)

        store.applyNoteRemap(plan.noteRemap)
        store.delete(plan.deletions)
        store.upsert(plan.upserts)
        return GroupLoad.Loaded(
            groupId = groupId,
            entries = plan.upserts.size,
            skippedLessons = mapping.skippedLessons,
            deleted = plan.deletions.size,
        )
    }
}
