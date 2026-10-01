package com.virtualshape.gradule.domain.schedule

/** Тип занятия для кромки карточки (docs/design/DESIGN.md). */
enum class LessonType { PRACTICE, LECTURE }

/**
 * Тип занятия по названию и сокращению предмета.
 * ponytail: в API поля типа нет — эвристика по тексту; уточнить у БРС/владельца,
 * когда появится серверное поле (порог списка маркеров тогда удалить).
 *
 * В [subjectName] текст в скобках отбрасывается («Веб-программирование (практика)» —
 * уточнение названия), в [subjectAbbr] скобки значимы («Веб-прог (практика)»).
 * Регистр и «ё» не важны; всё неопознанное и пустое — [LessonType.LECTURE].
 */
object LessonTypeClassifier {
    private val practiceMarkers = listOf("практик", "лаборат", "лаб.", "семинар", "сем.", "проект")

    fun of(
        subjectName: String,
        subjectAbbr: String,
    ): LessonType {
        val source = fold(subjectName.substringBefore('(')) + " " + fold(subjectAbbr)
        return if (practiceMarkers.any { source.contains(it) }) LessonType.PRACTICE else LessonType.LECTURE
    }

    private fun fold(text: String): String = text.lowercase().replace('ё', 'е')
}
