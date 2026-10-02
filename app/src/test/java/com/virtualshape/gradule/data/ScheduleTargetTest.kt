package com.virtualshape.gradule.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Заглушка выбора группы живёт в data-слое, а не в UI: когда появится выбор в настройках (e01s02),
 * подменяется только источник [ScheduleTarget], экран дня не трогаем.
 * Резолв по фикстуре — страховка от тихой подмены курса/номера/направления.
 */
class ScheduleTargetTest {
    @Test
    fun `default target resolves to a group id`() {
        val resolver =
            GroupResolver(
                FixtureHttpGet(
                    mapOf(
                        ScheduleApi.groupsForGrade(ScheduleTarget.DEFAULT.gradeId) to FixtureHttpGet.fixture("group_for_grade_3.json"),
                    ),
                ),
            )

        assertEquals(116L, resolver.resolve(ScheduleTarget.DEFAULT))
    }
}
