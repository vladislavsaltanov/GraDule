package com.virtualshape.gradule.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Пусто не должно быть тихим»: у каждого состояния есть свой текст (story e01s01). */
class SyncUiStateTest {
    @Test
    fun `loading has its own text`() {
        assertEquals("Загружаю…", SyncUiState.Loading.statusText())
    }

    @Test
    fun `empty no data differs from unsupported faculty`() {
        val noData = SyncUiState.Empty(EmptyReason.NoData).statusText()
        val unsupported = SyncUiState.Empty(EmptyReason.FacultyNotSupported).statusText()
        assertEquals("Пока нет данных", noData)
        assertEquals("Этот факультет не поддерживается", unsupported)
    }

    @Test
    fun `stale keeps data and names the error`() {
        val state = SyncUiState.StaleWithError(listOf(1, 2), 0L, "таймаут")
        assertEquals("не удалось обновить: таймаут", state.statusText())
    }

    @Test
    fun `failed without cache names the error`() {
        assertEquals("нет кэша: 401", SyncUiState.FailedNoCache("401").statusText())
    }

    @Test
    fun `content reports last update`() {
        assertTrue(SyncUiState.Content(listOf(1), 0L).statusText().startsWith("обновлено "))
    }
}
