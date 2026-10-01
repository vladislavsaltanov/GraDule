package com.virtualshape.gradule.domain.schedule

/**
 * Визуальный слот дня: записи одного занятия (подгруппы), у которых совпали
 * день, начало, конец и чётность недели. Внутри слота [entries] отсортированы по subnum.
 */
data class LessonSlot(
    val timeslot: Timeslot,
    val entries: List<ScheduleEntry>,
)

object ScheduleSlots {
    /**
     * Записи → слоты дня. Сначала фильтр по подгруппе (subnum первой подгруппы записи),
     * потом группировка по ключу слота. Слоты отсортированы по времени начала.
     * [subnum] = null — все подгруппы.
     */
    fun group(
        entries: List<ScheduleEntry>,
        subnum: Int? = null,
    ): List<LessonSlot> =
        entries
            .filter { subnum == null || it.subgroups.firstOrNull()?.subnum == subnum }
            .groupBy { it.timeslot }
            .map { (timeslot, group) ->
                LessonSlot(timeslot, group.sortedBy { it.subgroups.firstOrNull()?.subnum ?: Int.MAX_VALUE })
            }.sortedBy { it.timeslot.startMinute }
}
