package com.virtualshape.gradule.schedule.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virtualshape.gradule.schedule.domain.model.Lesson
import java.util.Calendar


val CascadiaFont = FontFamily(
    androidx.compose.ui.text.font.Font(com.virtualshape.gradule.R.font.cascadia_code, FontWeight.Normal)
)

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel()) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val currentWeekType by viewModel.currentWeekType.collectAsStateWithLifecycle()
    val weekLabel = if (currentWeekType == "0") "Верхняя неделя" else "Нижняя неделя"

    val processedLessons = remember(lessons, currentWeekType) {
        lessons
            .filter { it.weekType == "full" || it.weekType == currentWeekType }
            .groupBy { it.dayOfWeek }
    }

    val dayNames = remember {
        mapOf(
            0 to "Понедельник", 1 to "Вторник", 2 to "Среда",
            3 to "Четверг", 4 to "Пятница", 5 to "Суббота"
        )
    }

    Scaffold(
        topBar = { TopAppBar(
            title = {
                Column {
                    Text("Расписание", style = MaterialTheme.typography.titleLarge,
                        fontFamily = CascadiaFont)
                    Text(weekLabel, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = CascadiaFont)
                    Text(viewModel.groupName, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = CascadiaFont)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            processedLessons.forEach { (day, dayLessons) ->
                stickyHeader(key = "header_$day") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = dayNames[day] ?: "День $day",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = CascadiaFont
                        )
                    }
                }

                items(
                    items = dayLessons,
                    key = { lesson -> lesson.id },
                    contentType = { "lesson" }
                ) { lesson ->
                    LessonCard(lesson)
                }
            }
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column {
            Text(
                text = lesson.subject,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${lesson.startTime} - ${lesson.endTime}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                lesson.subgroups.forEachIndexed { index, subgroup ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        horizontalAlignment = if (lesson.subcount > 1) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        if (lesson.subcount > 1) {
                            Text(
                                text = "Подгруппа ${subgroup.subnum}:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = if (lesson.subcount > 1) TextAlign.Center else TextAlign.Start
                            )
                        }

                        if (subgroup.teacher.isNotBlank()) {
                            Text(
                                text = subgroup.teacher,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = if (lesson.subcount > 1) TextAlign.Center else TextAlign.Start
                            )
                        }

                        if (subgroup.room.isNotBlank() && !subgroup.room.contains("?")) {
                            Text(
                                text = "ауд. ${subgroup.room}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Рисуем разделитель между преподавателями, если их больше одного
                    if (index < lesson.subgroups.size - 1) {
                        VerticalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}
