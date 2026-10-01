package com.virtualshape.gradule.domain.schedule

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Тип занятия в дизайне («практика»/«лекция») в ответе БРС отсутствует —
 * выводим эвристикой из названия предмета.
 */
class LessonTypeClassifierTest {
    @Test
    fun `lecture marker in name stays lecture`() {
        assertEquals(LessonType.LECTURE, LessonTypeClassifier.of("Алгоритмы и структуры данных (лекции)", "АСД"))
    }

    @Test
    fun `practice word in brackets is not a lesson type`() {
        assertEquals(LessonType.LECTURE, LessonTypeClassifier.of("Веб-программирование (практика)", "Веб-прог"))
    }

    @Test
    fun `lab work is practice`() {
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Лабораторная работа по ИВТ", "ИВТ"))
    }

    @Test
    fun `project is practice`() {
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Проект", "Пр"))
    }

    @Test
    fun `empty name is lecture`() {
        assertEquals(LessonType.LECTURE, LessonTypeClassifier.of("", ""))
    }

    @Test
    fun `abbr is a source too`() {
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Веб-программирование", "Веб-прог (практика)"))
    }

    @Test
    fun `case and yo do not matter`() {
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("ПРАКТИКУМ", ""))
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Семинар", ""))
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Практика", ""))
    }

    @Test
    fun `unknown name is lecture`() {
        assertEquals(LessonType.LECTURE, LessonTypeClassifier.of("Иностранный язык", "ИнЯз"))
        assertEquals(LessonType.LECTURE, LessonTypeClassifier.of("CS251. Технологии баз данных", "CS251. Техн БД"))
    }
}
