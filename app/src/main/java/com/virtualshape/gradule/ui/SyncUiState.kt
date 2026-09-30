package com.virtualshape.gradule.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Состояние любого экрана с внешними данными (БРС, расписание).
 * Пустота легитимна, но не тихая: [Empty] различает «нет данных» и
 * «факультет не поддержан» (story e01s01).
 */
sealed interface SyncUiState<out T> {
    data object Loading : SyncUiState<Nothing>

    data class Empty(
        val reason: EmptyReason,
    ) : SyncUiState<Nothing>

    data class Content<out T>(
        val data: T,
        val updatedAt: Long,
    ) : SyncUiState<T>

    data class StaleWithError<out T>(
        val data: T,
        val updatedAt: Long,
        val error: String,
    ) : SyncUiState<T>

    data class FailedNoCache(
        val error: String,
    ) : SyncUiState<Nothing>
}

enum class EmptyReason { NoData, FacultyNotSupported }

fun SyncUiState<*>.statusText(): String =
    when (this) {
        SyncUiState.Loading -> {
            "Загружаю…"
        }

        is SyncUiState.Empty -> {
            when (reason) {
                EmptyReason.NoData -> "Пока нет данных"
                EmptyReason.FacultyNotSupported -> "Этот факультет не поддерживается"
            }
        }

        is SyncUiState.Content -> {
            "обновлено ${stamp(updatedAt)}"
        }

        is SyncUiState.StaleWithError -> {
            "не удалось обновить: $error"
        }

        is SyncUiState.FailedNoCache -> {
            "нет кэша: $error"
        }
    }

private fun stamp(
    millis: Long,
    now: Long = System.currentTimeMillis(),
): String {
    val zone = ZoneId.systemDefault()
    val then = Instant.ofEpochMilli(millis).atZone(zone)
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val time = then.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
    return if (then.toLocalDate() == today) {
        "сегодня в $time"
    } else {
        then.format(DateTimeFormatter.ofPattern("d MMMM в HH:mm", Locale.getDefault()))
    }
}
