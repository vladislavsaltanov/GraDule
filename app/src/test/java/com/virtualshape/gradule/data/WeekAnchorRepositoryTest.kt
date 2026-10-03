package com.virtualshape.gradule.data

import com.virtualshape.gradule.domain.schedule.Parity
import com.virtualshape.gradule.domain.schedule.WeekAnchor
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Якорь чётности недели приходит с сервера (`/APIv1/week`, 0 — верхняя), а не вычисляется по ISO.
 * Проверено живьём: 2026-10-02 сервер отдал `{"week":0}` (верхняя), а ISO-неделя 40 чётная — гипотеза
 * «нечётная = верхняя» дала бы нижнюю и молча переврала бы пятничные пары.
 */
class WeekAnchorRepositoryTest {
    private val today = LocalDate.of(2026, 10, 2)

    private fun repositoryReturning(body: String) =
        ScheduleRepository(
            FixtureHttpGet(mapOf(ScheduleApi.week() to body)),
            InMemoryScheduleStore(),
        )

    @Test
    fun `week zero is the upper week`() {
        val anchor = repositoryReturning("""{"week":0}""").weekAnchor(today)

        assertEquals(WeekAnchor(today, Parity.UPPER), anchor)
    }

    @Test
    fun `odd week is the lower one`() {
        assertEquals(Parity.LOWER, repositoryReturning("""{"week":1}""").weekAnchor(today).parity)
    }
}