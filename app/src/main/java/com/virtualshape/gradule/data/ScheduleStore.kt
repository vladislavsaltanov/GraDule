package com.virtualshape.gradule.data

import com.virtualshape.gradule.domain.schedule.EntryKey
import com.virtualshape.gradule.domain.schedule.ScheduleEntry

/**
 * Хранилище расписания. Замена SERVER-набора — только точечными вызовами [upsert]/[delete] по ключу:
 * REPLACE/CASCADE запрещены, иначе вместе с записями снесутся заметки (e04).
 */
interface ScheduleStore {
    /** Все записи (SERVER + LOCAL) в произвольном, но стабильном порядке. */
    fun entries(): List<ScheduleEntry>

    /** Точечная вставка/обновление по ключу: повторный upsert того же ключа не плодит дубликаты. */
    fun upsert(entries: List<ScheduleEntry>)

    /** Удаление ровно по ключу. */
    fun delete(keys: List<EntryKey>)

    /**
     * Перенос заметок со старых ключей на новые (смена lessonId при том же human-ключе).
     * Хранилище без заметок — no-op; реализация с таблицей заметок переопределяет.
     */
    fun applyNoteRemap(remap: Map<EntryKey, EntryKey>) {}
}

/** In-memory [ScheduleStore] для юнит-тестов и для срезов без БД. */
class InMemoryScheduleStore : ScheduleStore {
    private val rows = LinkedHashMap<EntryKey, ScheduleEntry>()

    /** Применённые переносы заметок — по одному на загрузку, для проверки. */
    val appliedNoteRemaps = mutableListOf<Map<EntryKey, EntryKey>>()

    override fun entries(): List<ScheduleEntry> = rows.values.toList()

    override fun upsert(entries: List<ScheduleEntry>) {
        entries.forEach { rows[it.key] = it }
    }

    override fun delete(keys: List<EntryKey>) {
        keys.forEach { rows.remove(it) }
    }

    override fun applyNoteRemap(remap: Map<EntryKey, EntryKey>) {
        if (remap.isNotEmpty()) appliedNoteRemaps += remap
    }
}
