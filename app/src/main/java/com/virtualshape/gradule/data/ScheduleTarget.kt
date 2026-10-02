package com.virtualshape.gradule.data

/**
 * Заглушка выбора группы: ММ и ИИ, 1 курс, группа 7 (id 185 на момент старта).
 * Живёт в data-слое, чтобы экран дня не знал, откуда взялась цель: когда выбор уедет
 * в настройки (e01s02), меняется только источник [DEFAULT], не UI.
 */
object ScheduleTarget {
    val DEFAULT = GroupKey(gradeId = 1, num = 7, name = "ММ и ИИ")
}
