package com.virtualshape.gradule.domain.schedule

/**
 * Тип занятия для кромки карточки (docs/design/DESIGN.md).
 * [UNKNOWN] — маркера нет: в ответе schedule.sfedu.ru типа занятия нет вовсе
 * (на живой фикстуре ни в названии, ни в сокращении предмета маркеров не встречается).
 */
enum class LessonType { PRACTICE, LECTURE, UNKNOWN }

/**
 * Тип занятия по названию и сокращению предмета.
 * ponytail: серверного поля типа нет, это эвристика по тексту. Когда появится
 * тип из БРС (`grade.sfedu.ru`, дисциплина по карточке) — он станет источником,
 * а список маркеров можно удалить.
 *
 * Ищем в обоих полях целиком, вместе со скобками: «Веб-программирование (практика)»
 * — это практика. Регистр и «ё» не важны; без маркера — [LessonType.UNKNOWN],
 * красить карточку цветом «лекция» наугад нельзя.
 */
object LessonTypeClassifier {
    private val practiceMarkers = listOf("практик", "лаборат", "лаб.", "семинар", "сем.", "проект")
    private val lectureMarkers = listOf("лекци", "лектор")

    fun of(
        subjectName: String,
        subjectAbbr: String,
    ): LessonType {
        val source = fold(subjectName) + " " + fold(subjectAbbr)
        return when {
            practiceMarkers.any { source.contains(it) } -> LessonType.PRACTICE
            lectureMarkers.any { source.contains(it) } -> LessonType.LECTURE
            else -> LessonType.UNKNOWN
        }
    }

    private fun fold(text: String): String = text.lowercase().replace('ё', 'е')
}
