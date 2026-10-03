package com.virtualshape.gradule.domain.schedule

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Откуда запись: сервер расписания или пользовательский слой. */
enum class Source { SERVER, LOCAL }

/** Подгруппа/аудитория внутри записи; несколько подгрупп = одна запись ленты. */
data class Subgroup(
    val id: Long,
    val subnum: Int,
    val subjectName: String,
    val subjectAbbr: String,
    val teacherName: String,
    val roomName: String,
)

/**
 * Устойчивый ключ записи: стабильный серверный id (lesson + curriculum),
 * пользовательский uuid или производный human-ключ (день, начало, чётность, предмет, subnum).
 */
sealed interface EntryKey {
    data class Server(
        val lessonId: Long,
        val curriculumId: Long,
    ) : EntryKey

    data class Local(
        val uuid: String,
    ) : EntryKey

    data class Derived(
        val day: DayOfWeek,
        val startMinute: Int,
        val parity: Parity,
        val subjectAbbr: String,
        val subnum: Int,
    ) : EntryKey
}

/**
 * Запись расписания. `onceDate == null` — WEEKLY (день + чётность в [timeslot]),
 * иначе ONE_OFF (только `onceDate`, время в [timeslot] игнорируется).
 */
data class ScheduleEntry(
    val key: EntryKey,
    val source: Source,
    val timeslot: Timeslot,
    val subgroups: List<Subgroup>,
    val info: String = "",
    val onceDate: LocalDate? = null,
) {
    /** Запасной human-ключ по первой подгруппе — для переноса заметок при смене lessonId. */
    val derivedKey: EntryKey.Derived?
        get() =
            subgroups.firstOrNull()?.let {
                EntryKey.Derived(timeslot.day, timeslot.startMinute, timeslot.parity, it.subjectAbbr, it.subnum)
            }
}

/** Выборка записей на дату. */
object ScheduleQuery {
    /**
     * Записи на [date]: ONE_OFF — только в свою дату, WEEKLY — по дню недели и чётности недели,
     * которую задаёт [anchor] (серверный /APIv1/week). Порядок: начало занятия, затем сокращение
     * предмета первой подгруппы.
     */
    fun entriesOn(
        date: LocalDate,
        entries: List<ScheduleEntry>,
        anchor: WeekAnchor,
    ): List<ScheduleEntry> =
        entries
            .filter { matches(it, date, anchor) }
            .sortedWith(compareBy({ it.timeslot.startMinute }, { it.subgroups.firstOrNull()?.subjectAbbr ?: "" }))

    private fun matches(
        entry: ScheduleEntry,
        date: LocalDate,
        anchor: WeekAnchor,
    ): Boolean {
        val once = entry.onceDate
        if (once != null) return once == date
        if (entry.timeslot.day != date.dayOfWeek) return false
        return when (entry.timeslot.parity) {
            Parity.FULL -> true
            else -> entry.timeslot.parity == weekParity(date, anchor)
        }
    }

    /**
     * Чётность недели [date] относительно якоря: через нечётное число недель чётность переворачивается.
     * Считаем по понедельникам, поэтому переход через новый год счёт не ломает.
     */
    fun weekParity(
        date: LocalDate,
        anchor: WeekAnchor,
    ): Parity {
        val weeks = ChronoUnit.WEEKS.between(anchor.date.with(DayOfWeek.MONDAY), date.with(DayOfWeek.MONDAY))
        val parity = anchor.parity
        return if (weeks % 2 == 0L) parity else if (parity == Parity.UPPER) Parity.LOWER else Parity.UPPER
    }
}
