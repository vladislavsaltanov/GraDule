package com.virtualshape.gradule.data

/**
 * Заглушка выбора группы: ФИИТ, 4 курс, группа 4 (id 116).
 * Живёт в data-слое, чтобы экран дня не знал, откуда взялась цель: когда выбор уедет
 * в настройки (e01s02), меняется только источник [DEFAULT], не UI.
 */
object ScheduleTarget {
    val DEFAULT = GroupKey(gradeId = 3, num = 4, name = "ФИИТ")
}
