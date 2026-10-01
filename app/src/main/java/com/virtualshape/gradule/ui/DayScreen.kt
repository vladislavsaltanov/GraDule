package com.virtualshape.gradule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import com.virtualshape.gradule.ui.theme.Ink
import com.virtualshape.gradule.ui.theme.Lecture
import com.virtualshape.gradule.ui.theme.Paper
import com.virtualshape.gradule.ui.theme.Practice
import java.time.LocalDate

/**
 * Экран «Расписание» — Figma ScheduleScreenNew 76:187 (LessonCard 76:208).
 * dp = px/3. Список: поля 16 px (5.33 dp) по бокам, фрейм карточки 1120×320 px
 * (подложка 100 dp + 20 px низа), шаг карточек 335 px (зазор 15 px = 5 dp).
 * Тексты стоят абсолютными смещениями от подложки — высота карточки не зависит
 * от длины заголовка, а текст обрезается по дизайн-боксу.
 * Подгрупп в UI нет: подгруппа выбирается в профиле, здесь всё расписание дня.
 */
@Composable
fun DayScreen(
    state: SyncUiState<List<ScheduleEntry>>,
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    val entries = (state as? SyncUiState.Content)?.data ?: (state as? SyncUiState.StaleWithError)?.data
    val slots = entries?.let { ScheduleSlots.group(ScheduleQuery.entriesOn(date, it), subnum = null) }.orEmpty()

    Box(modifier.fillMaxSize()) {
        when (state) {
            SyncUiState.Loading -> {
                Centered { CircularProgressIndicator() }
            }

            is SyncUiState.Empty -> {
                StatusCard("Расписание", state.statusText())
            }

            is SyncUiState.FailedNoCache -> {
                StatusCard("Расписание", state.statusText())
            }

            is SyncUiState.StaleWithError -> {
                SlotList(slots)
                Text(
                    state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
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
    contentPadding: PaddingValues = PaddingValues(start = 5.33.dp, end = 5.33.dp, bottom = 5.dp),
) {
    if (slots.isEmpty()) {
        StatusCard("Расписание", SyncUiState.Empty(EmptyReason.NoData).statusText())
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        contentPadding = contentPadding,
    ) {
        // ponytail: моушн из awwwards-рефа (NexDash) — только штатный animateItem,
        // статика бренда и геометрия Figma не трогаются.
        items(slots, key = { slotKey(it) }) { slot ->
            LessonCard(slot, modifier = Modifier.animateItem())
        }
    }
}

/** Ключ списка: Lazy хранит ключи в Bundle — только примитивы и строки. */
private fun slotKey(slot: LessonSlot): String {
    val t = slot.timeslot
    return "${t.day}-${t.startMinute}-${t.endMinute}-${t.parity}"
}

@Composable
internal fun Centered(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

/**
 * LessonCard 76:208 (новый стиль). Фрейм 1120×320 px, r 20 px, обрезка по фрейму.
 * Подложка `card` 1100×300 px (100 dp, r 32 px), тень 0 4/10/3 25 %.
 * Лист `paper` 1070×215 px (356.67×71.67 dp, r 24 px) на 70 px (23.33 dp) сверху,
 * поля 15 px (5 dp) по бокам, кромка-бордер 24 px (8 dp) справа цветом типа.
 * Время — IBM Plex Mono 30 px, по центру ячейки; чернила в боксе 25 px на 23 px
 * от подложки (сдвиг 5.17 dp — Compose кладёт базовую линию ниже бокса).
 * Заголовок — Cascadia Mono 52 px, бокс 1003 px, отступ 20 px от верха листа.
 * Строка «ауд. • тип» прижата к низу листа (отступ 20 px), чернила 65 %.
 */
@Composable
private fun LessonCard(
    slot: LessonSlot,
    modifier: Modifier = Modifier,
) {
    val entry = slot.entries.first()
    val subgroup = entry.subgroups.firstOrNull()
    val type = LessonTypeClassifier.of(subgroup?.subjectName.orEmpty(), subgroup?.subjectAbbr.orEmpty())
    val edge = if (type == LessonType.PRACTICE) Practice else Lecture

// Фрейм карточки — колонка: подложка 100 dp + 20 px (6.67 dp) низа, иначе
    // Spacer лёг бы поверх Box и шаг стал бы 315 px вместо 335 px из Figma.
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(6.67.dp))) {
        CardSubstrate(height = 100.dp) {
            Text(
                DayLabel.timeRange(slot.timeslot.startMinute, slot.timeslot.endMinute),
                style = MaterialTheme.typography.bodyLarge,
                color = Bg,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset(y = 5.17.dp),
            )
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 23.33.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
                    .height(71.67.dp)
                    .background(Paper, RoundedCornerShape(8.dp))
                    .edgeStripe(edge),
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(start = 11.67.dp, top = 6.67.dp, bottom = 6.67.dp),
                ) {
                    Text(
                        subgroup?.subjectName.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(1003f / 1070f),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        placeAndType(subgroup?.roomName.orEmpty(), entry.info, type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(1030f / 1070f),
                    )
                }
            }
        }
        // 20 px (6.67 dp) низа фрейма + зазор 15 px (5 dp) между карточками.
        Spacer(Modifier.height(6.67.dp))
    }
}

/**
 * «ауд. 317 • практика»; заметка сервера («с 17-00 до 18-30») дописывается в конец:
 * в дизайне отдельной строки под неё нет, а терять данные расписания нельзя.
 * Без известного типа — просто «ауд. 317».
 */
internal fun placeAndType(
    room: String,
    info: String,
    type: LessonType,
): AnnotatedString {
    val typeLabel =
        when (type) {
            LessonType.PRACTICE -> "практика"
            LessonType.LECTURE -> "лекция"
            LessonType.UNKNOWN -> null
        }
    // Префикс «ауд.» — только для номера аудитории: в данных встречаются и
    // удалённые («Онлайн»), для них «ауд. Онлайн» — чушь.
    val roomLabel = room.ifBlank { null }?.let { if (it.any(Char::isDigit)) "ауд. $it" else it }
    return buildAnnotatedString {
        listOfNotNull(roomLabel, typeLabel, info.ifBlank { null }).forEachIndexed { index, label ->
            if (index > 0) {
                withStyle(SpanStyle(fontWeight = FontWeight.Normal)) { append(" • ") }
            }
            append(label)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE8E8E8)
@Composable
private fun DayScreenPreview() {
    val today = LocalDate.of(2026, 6, 15)
    // Три длины названия: однострочное, двухстрочное из макета и заведомо длинное —
    // проверить перенос на вторую строку и многоточие.
    val subjects =
        listOf(
            "Математический анализ",
            "CS242. Алгоритмы и структуры данных",
            "Основы компьютерных наук и искусственного интеллекта",
        )
    DayScreen(
        state =
            SyncUiState.Content(
                subjects.mapIndexed { index, subject ->
                    ScheduleEntry(
                        key = EntryKey.Server(3001L + index, 5001L + index),
                        source = Source.SERVER,
                        timeslot =
                            Timeslot(
                                today.dayOfWeek,
                                9 * 60 + index * 240,
                                10 * 60 + index * 240,
                                Parity.FULL,
                            ),
                        subgroups =
                            listOf(
                                Subgroup(5001L + index, 1, subject, "Пр", "Иванова Ольга Александровна", "302"),
                            ),
                        info = "",
                    )
                },
                updatedAt = 0L,
            ),
        date = today,
    )
}
