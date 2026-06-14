package com.virtualshape.gradule.schedule.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.virtualshape.gradule.schedule.domain.model.Lesson
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel()) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val groupedLessons = lessons.groupBy { it.dayOfWeek }
    val dayNames = mapOf(1 to "Понедельник", 2 to "Вторник", 3 to "Среда", 4 to "Четверг", 5 to "Пятница", 6 to "Суббота")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расписание") }
            )
        }
    ) { paddingValues ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        groupedLessons.forEach { (day, dayLessons) ->
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = dayNames[day] ?: "День $day",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            items(dayLessons, key = { it.id }) { lesson ->
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
        Column(modifier = Modifier.padding(12.dp)) {
            Text(lesson.subject, style = MaterialTheme.typography.titleSmall)
            Text(
                "${lesson.startTime} - ${lesson.endTime}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(lesson.teacher, style = MaterialTheme.typography.bodySmall)

            if (lesson.room != "?")
                Text("Ауд. ${lesson.room}", style = MaterialTheme.typography.bodySmall)
        }
    }
}