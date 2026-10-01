package com.virtualshape.gradule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.virtualshape.gradule.domain.schedule.DayLabel
import com.virtualshape.gradule.domain.schedule.EntryKey
import com.virtualshape.gradule.domain.schedule.LessonSlot
import com.virtualshape.gradule.domain.schedule.LessonType
import com.virtualshape.gradule.domain.schedule.LessonTypeClassifier
import com.virtualshape.gradule.domain.schedule.Parity
import com.virtualshape.gradule.domain.schedule.ScheduleEntry
import com.virtualshape.gradule.domain.schedule.ScheduleQuery
import com.virtualshape.gradule.domain.schedule.ScheduleSlots
import com.virtualshape.gradule.domain.schedule.Source
import com.virtualshape.gradule.domain.schedule.Subgroup
import com.virtualshape.gradule.domain.schedule.Timeslot
import com.virtualshape.gradule.ui.theme.Bg
import com.virtualshape.gradule.ui.theme.Card
import com.virtualshape.gradule.ui.theme.Ink
import com.virtualshape.gradule.ui.theme.Lecture
import com.virtualshape.gradule.ui.theme.Paper
import com.virtualshape.gradule.ui.theme.Practice
import java.time.LocalDate

/**
 * Экран дня: список слотов по дизайну (docs/design/DESIGN.md, «Компоненты → Пара»).
 * Дата живёт в каркасе (Shell.HeaderRow), экран только фильтрует по ней.
 * Данные приходят параметром — экран ничего не знает про сеть и хранилище.
 */
@Composable
fun DayScreen(
    state: SyncUiState<List<ScheduleEntry>>,
    date: LocalDate,
    modifier: Modifier = Modifier,
    subnumFilter: Int? = null,
    onSubnumChange: (Int?) -> Unit = {},
) {
    val entries = (state as? SyncUiState.Content)?.data ?: (state as? SyncUiState.StaleWithError)?.data
    val slots = entries?.let { ScheduleSlots.group(ScheduleQuery.entriesOn(date, it), subnumFilter) }.orEmpty()
    val subnums =
        entries
            .orEmpty()
            .mapNotNull { it.subgroups.firstOrNull()?.subnum }
            .distinct()
            .sorted()

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        if (subnums.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = subnumFilter == null,
                    onClick = { onSubnumChange(null) },
                    label = { Text("все") },
                )
                subnums.forEach { subnum ->
                    FilterChip(
                        selected = subnumFilter == subnum,
                        onClick = { onSubnumChange(if (subnumFilter == subnum) null else subnum) },
                        label = { Text("подгруппа $subnum") },
                    )
                }
            }
        }
        when (state) {
            SyncUiState.Loading -> {
                Centered { CircularProgressIndicator() }
            }

            is SyncUiState.Empty -> {
                Centered { StatusCard("Расписание", state) }
            }

            is SyncUiState.FailedNoCache -> {
                Centered { StatusCard("Расписание", state) }
            }

            is SyncUiState.StaleWithError -> {
                Text(
                    state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                SlotList(slots)
            }

            is SyncUiState.Content -> {
                SlotList(slots)
            }
        }
    }
}

@Composable
private fun SlotList(
    slots: List<LessonSlot>,
    contentPadding: PaddingValues = PaddingValues(bottom = 15.dp),
) {
    if (slots.isEmpty()) {
        Centered { StatusCard("Расписание", SyncUiState.Empty(EmptyReason.NoData)) }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = contentPadding,
    ) {
        items(slots) { slot -> LessonSlotCard(slot) }
    }
}

@Composable
internal fun Centered(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.TopCenter,
    ) { content() }
}

/**
 * Карточка-папка: тёмная подложка, время на ней, лист бумаги с текстом
 * и цветной кромкой справа (тип занятия).
 */
@Composable
private fun LessonSlotCard(
    slot: LessonSlot,
    modifier: Modifier = Modifier,
) {
    val entry = slot.entries.first()
    val subgroup = entry.subgroups.firstOrNull()
    val type = LessonTypeClassifier.of(subgroup?.subjectName.orEmpty(), subgroup?.subjectAbbr.orEmpty())
    Column(
        modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(9.dp))
            .background(Card, RoundedCornerShape(9.dp))
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 10.dp),
    ) {
        Text(
            DayLabel.timeRange(slot.timeslot.startMinute, slot.timeslot.endMinute),
            style = MaterialTheme.typography.bodyMedium,
            color = Bg,
        )
        Row(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .weight(1f)
                    .background(Paper, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
            ) {
                Text(
                    subgroup?.subjectName.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                if (slot.entries.size > 1) {
                    Text(
                        "подгрупп: ${slot.entries.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.65f),
                    )
                }
                if (!subgroup?.teacherName.isNullOrBlank()) {
                    Text(
                        subgroup.teacherName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.65f),
                    )
                }
                if (entry.info.isNotBlank()) {
                    Text(
                        entry.info,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink.copy(alpha = 0.65f),
                    )
                }
                Text(
                    placeAndType(subgroup?.roomName.orEmpty(), type),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            }
            Box(
                Modifier
                    .width(6.dp)
                    .background(typeAccent(type), RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)),
            )
        }
    }
}

/** «ауд. 317 • практика»; без известного типа — просто «ауд. 317». */
private fun placeAndType(
    room: String,
    type: LessonType,
): String {
    val typeLabel =
        when (type) {
            LessonType.PRACTICE -> "практика"
            LessonType.LECTURE -> "лекция"
            LessonType.UNKNOWN -> null
        }
    return listOfNotNull(room.ifBlank { null }, typeLabel).joinToString(" • ")
}

/** Типа в API нет: UNKNOWN рисуем нейтральной кромкой, а не выдуманной лекцией. */
private fun typeAccent(type: LessonType): Color =
    when (type) {
        LessonType.PRACTICE -> Practice
        LessonType.LECTURE -> Lecture
        LessonType.UNKNOWN -> Paper
    }

@Preview(showBackground = true, backgroundColor = 0xFFE8E8E8)
@Composable
private fun DayScreenPreview() {
    val today = LocalDate.of(2026, 6, 15)
    val timeslot = Timeslot(today.dayOfWeek, 11 * 60 + 55, 13 * 60 + 30, Parity.FULL)
    val entries =
        (1..3).map { subnum ->
            ScheduleEntry(
                key = EntryKey.Server(3000L + subnum, 5000L + subnum),
                source = Source.SERVER,
                timeslot = timeslot,
                subgroups =
                    listOf(
                        Subgroup(
                            5000L + subnum,
                            subnum,
                            "Веб-программирование (практика)",
                            "Веб-прог",
                            "Горшков Сергей Андреевич",
                            "ЛОС 4",
                        ),
                    ),
                info = "",
            )
        }
    DayScreen(
        state = SyncUiState.Content(entries, updatedAt = 0L),
        date = today,
    )
}
