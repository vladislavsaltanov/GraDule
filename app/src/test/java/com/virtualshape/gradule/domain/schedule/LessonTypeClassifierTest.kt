package com.virtualshape.gradule.domain.schedule

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Тип занятия в дизайне («практика»/«лекция») в ответе schedule.sfedu.ru отсутствует:
 * на живой фикстуре ни в названии, ни в сокращении маркеров нет. Поэтому выводим эвристикой
 * и, если маркера нет, честно отдаём [LessonType.UNKNOWN], а не рисуем «лекцию».
 */
class LessonTypeClassifierTest {
    @Test
    fun `lecture marker in name stays lecture`() {
        assertEquals(LessonType.LECTURE, LessonTypeClassifier.of("Алгоритмы и структуры данных (лекции)", "АСД"))
    }

    @Test
    fun `practice word in brackets counts`() {
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Веб-программирование (практика)", "Веб-прог"))
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
    fun `seminar and practicum are practice`() {
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Семинар", ""))
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("ПРАКТИКУМ", ""))
    }

    @Test
    fun `case and yo do not matter`() {
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Практика", ""))
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Лабораторные работы", ""))
    }

    @Test
    fun `abbr is a source too`() {
        assertEquals(LessonType.PRACTICE, LessonTypeClassifier.of("Веб-программирование", "Веб-прог (практика)"))
    }

    @Test
    fun `empty name is unknown`() {
        assertEquals(LessonType.UNKNOWN, LessonTypeClassifier.of("", ""))
    }

    @Test
    fun `real fixture names have no marker and stay unknown`() {
        // ровно эти значения встречаются в schedule_group_185.json
        assertEquals(LessonType.UNKNOWN, LessonTypeClassifier.of("Иностранный язык", "ИнЯз"))
        assertEquals(LessonType.UNKNOWN, LessonTypeClassifier.of("Программирование", "Прогр-е"))
        assertEquals(LessonType.UNKNOWN, LessonTypeClassifier.of("Дискретная математика", "Дискр Матем"))
    }
}
