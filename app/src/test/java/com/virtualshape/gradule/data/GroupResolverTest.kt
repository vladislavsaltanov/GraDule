package com.virtualshape.gradule.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * B5: числовой id группы — кэш, а не идентичность. Резолвим его заново по human-ключу
 * (курс + номер + направление) при каждой загрузке; не нашлось — null, приложение просит выбрать заново.
 */
class GroupResolverTest {
    private val resolver =
        GroupResolver(
            FixtureHttpGet(
                mapOf(
                    ScheduleApi.groupsForGrade(1) to FixtureHttpGet.fixture("group_for_grade_1.json"),
                ),
            ),
        )

    @Test
    fun `resolves group id by num and name`() {
        assertEquals(185L, resolver.resolve(GroupKey(gradeId = 1, num = 7, name = "ММ и ИИ")))
        assertEquals(167L, resolver.resolve(GroupKey(gradeId = 1, num = 1, name = "ММ и искусственный интеллект")))
    }

    @Test
    fun `other num gives no id`() {
        assertNull(resolver.resolve(GroupKey(gradeId = 1, num = 9, name = "ММ и ИИ")))
    }

    @Test
    fun `other name gives no id`() {
        assertNull(resolver.resolve(GroupKey(gradeId = 1, num = 7, name = "ММ и искусственный интеллект")))
    }
}
