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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.virtualshape.gradule.domain.schedule.LessonType
import com.virtualshape.gradule.ui.theme.Bg
import com.virtualshape.gradule.ui.theme.Code
import com.virtualshape.gradule.ui.theme.Ink
import com.virtualshape.gradule.ui.theme.Lecture
import com.virtualshape.gradule.ui.theme.Paper
import com.virtualshape.gradule.ui.theme.Practice

/** Дисциплина + оценка для карточки экрана. Источник данных — БРС (story e03s01). */
data class GradeItem(
    val subject: String,
    val value: String,
    val type: LessonType = LessonType.LECTURE,
)

/**
 * Экран «Оценки» — Figma GradesScreen 81:112 (LessonCard 81:133). dp = px/3.
 * Фрейм карточки 1120×220 px: подложка 1100×190 px (63.33 dp) + 30 px (10 dp)
 * низа, шаг карточек ровно 220 px — зазора между ними в дизайне нет.
 * Лист 880×166 px (293.33×55.33 dp, r 18 px) на 12 px (4 dp) сверху и 10 px
 * (3.33 dp) слева; крупная оценка 55 px / 700 Code #E8E8E8 на тёмном справа.
 */
@Composable
fun GradesScreen(
    state: SyncUiState<List<GradeItem>>,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        when (state) {
            SyncUiState.Loading -> {
                Centered { CircularProgressIndicator() }
            }

            is SyncUiState.Empty -> {
                StatusCard("Оценки", state.statusText())
            }

            is SyncUiState.FailedNoCache -> {
                StatusCard("Оценки", state.statusText())
            }

            is SyncUiState.StaleWithError -> {
                GradeList(state.data)
                Text(
                    state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            is SyncUiState.Content -> {
                GradeList(state.data)
            }
        }
    }
}

@Composable
private fun GradeList(
    items: List<GradeItem>,
    contentPadding: PaddingValues = PaddingValues(start = 5.33.dp, end = 5.33.dp, bottom = 5.dp),
) {
    if (items.isEmpty()) {
        StatusCard("Оценки", SyncUiState.Empty(EmptyReason.NoData).statusText())
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Зазора между карточками в Figma нет: шаг 220 px = высота фрейма.
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = contentPadding,
    ) {
        items(items, key = { it.subject }) { item ->
            GradeCard(item, modifier = Modifier.animateItem())
        }
    }
}

/**
 * Карточка оценки: подложка 63.33 dp, лист 293.33×55.33 dp на 4 dp сверху.
 * Дисциплина 55 px / 600 Mono (смещение 30, 24 px от листа), оценка — правый край
 * листа минус 30 px (10 dp), верх 23 px (7.67 dp) от подложки.
 */
@Composable
private fun GradeCard(
    item: GradeItem,
    modifier: Modifier = Modifier,
) {
    val edge = if (item.type == LessonType.PRACTICE) Practice else Lecture
    // Фрейм карточки — колонка: подложка 63.33 dp + 30 px (10 dp) низа. В Box
    // Spacer лёг бы поверх и шаг стал бы 190 px вместо 220 px из Figma.
    Column(modifier.fillMaxWidth()) {
        CardSubstrate(height = 63.33.dp) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 3.33.dp, top = 4.dp)
                    .width(293.33.dp)
                    .height(55.33.dp)
                    .background(Paper, RoundedCornerShape(6.dp))
                    .edgeStripe(edge, radius = 6.dp, stripe = 6.dp),
            ) {
                Text(
                    item.subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset(x = 10.dp, y = 8.dp),
                )
            }
            Text(
                item.value,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = Code),
                color = Bg,
                maxLines = 1,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 7.67.dp),
            )
        }
        // 30 px (10 dp) низа фрейма — в Figma между карточками зазора нет.
        Spacer(Modifier.height(10.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE8E8E8)
@Composable
private fun GradesScreenPreview() {
    GradesScreen(
        state =
            SyncUiState.Content(
                listOf(
                    GradeItem("CS242. Алгоритмы и структуры данных", "57", LessonType.LECTURE),
                    GradeItem("Веб-программирование", "64", LessonType.PRACTICE),
                ),
                updatedAt = 0L,
            ),
    )
}
