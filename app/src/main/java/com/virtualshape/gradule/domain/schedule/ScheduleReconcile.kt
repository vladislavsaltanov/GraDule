package com.virtualshape.gradule.domain.schedule

/**
 * Явный план замены серверного набора: [upserts] — только SERVER-записи [incoming],
 * [deletions] — SERVER-ключи, которых больше нет, [untouchedLocal] — LOCAL-слои пользователя.
 * REPLACE/CASCADE не используются: применение плана — точечный upsert по ключу.
 */
data class ReconcilePlan(
    val upserts: List<ScheduleEntry>,
    val deletions: List<EntryKey>,
    val noteRemap: Map<EntryKey, EntryKey>,
    val untouchedLocal: List<EntryKey>,
)

object ScheduleReconcile {
    /**
     * [incoming] — полный новый набор SERVER. LOCAL-записи из [existing] не участвуют
     * ни в upsert, ни в удалении, ни в переносе заметок.
     */
    fun plan(
        existing: List<ScheduleEntry>,
        incoming: List<ScheduleEntry>,
    ): ReconcilePlan {
        val incomingServer = incoming.filter { it.source == Source.SERVER }
        val incomingKeys = incomingServer.associateBy { it.key }
        val existingServer = existing.filter { it.source == Source.SERVER }
        val vanished = existingServer.filter { it.key !in incomingKeys }

        // Заметки переносим только если human-ключ однозначен: ровно одна существующая запись с тем же ключом.
        // Запись с тем же ключом И тем же human-ключом не кандидат — её заметки остаются на месте.
        val noteRemap =
            incomingServer
                .filter { it.key !in existingServer.map(ScheduleEntry::key).toSet() }
                .mapNotNull { entry ->
                    val derived = entry.derivedKey ?: return@mapNotNull null
                    val candidates =
                        existingServer.filter { it.derivedKey == derived && incomingKeys[it.key]?.derivedKey != derived }
                    candidates.singleOrNull()?.let { it.key to entry.key }
                }.toMap()

        return ReconcilePlan(
            upserts = incomingServer,
            deletions = vanished.map { it.key },
            noteRemap = noteRemap,
            untouchedLocal = existing.filter { it.source == Source.LOCAL }.map { it.key },
        )
    }
}
