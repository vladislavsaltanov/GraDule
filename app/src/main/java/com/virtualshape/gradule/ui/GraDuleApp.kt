package com.virtualshape.gradule.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.virtualshape.gradule.data.GroupLoad
import com.virtualshape.gradule.data.OkHttpHttpGet
import com.virtualshape.gradule.data.ScheduleRepository
import com.virtualshape.gradule.data.ScheduleTarget
import com.virtualshape.gradule.data.SqliteScheduleStore
import com.virtualshape.gradule.domain.schedule.DayLabel
import com.virtualshape.gradule.domain.schedule.ScheduleEntry
import com.virtualshape.gradule.domain.schedule.WeekAnchor
import com.virtualshape.gradule.ui.theme.Bg
import com.virtualshape.gradule.ui.theme.Card
import com.virtualshape.gradule.ui.theme.GraDuleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Вкладки в порядке дока Figma: слот 1 активен на ScheduleScreenNew (76:187),
 * слот 2 со звездой — на GradesScreen (81:112).
 */
enum class Tab(
    val label: String,
    val icon: ImageVector,
    /** Иконка из Figma, если есть (0 — материал). */
    val iconRes: Int = 0,
) {
    Schedule("Расписание", Icons.Filled.DateRange, com.virtualshape.gradule.R.drawable.ic_dock_schedule),
    Grades("Оценки", Icons.Filled.Star),
    Home("Главная", Icons.Filled.Home),
}

@Composable
fun GraDuleApp() {
    var dark by rememberSaveable { mutableStateOf(false) }
    var scheduleRefresh by remember { mutableStateOf(0) }
    // Якорь недели с сервера; до ответа держим ISO-фолбэк, чтобы подпись дока и фильтр не расходились.
    var anchor by remember { mutableStateOf<WeekAnchor?>(null) }
    // Старт — экран дизайна с парами, а не заглушка.
    var tabName by rememberSaveable { mutableStateOf(Tab.Schedule.name) }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val tab = Tab.valueOf(tabName)
    val date = LocalDate.parse(dateText)
    val weekAnchor = anchor ?: WeekAnchor.guessed(LocalDate.now())

    GraDuleTheme(darkTheme = dark) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                GraDuleDrawer(
                    current = tab,
                    dark = dark,
                    onSelect = {
                        tabName = it.name
                        closeDrawer()
                    },
                    onToggleTheme = { dark = !dark },
                    onRefresh = {
                        scheduleRefresh++
                        closeDrawer()
                    },
                )
            },
        ) {
            Scaffold(
                containerColor = Bg,
                topBar = {
                    Column {
                        TopBand()
                        HeaderRow(
                            date = date,
                            onDateChange = { dateText = it.toString() },
                            onMenu = { scope.launch { drawerState.open() } },
                        )
                    }
                },
                bottomBar = {
                    GraDuleNavBar(
                        caption = DayLabel.of(date, weekAnchor),
                        tabs = Tab.entries,
                        selected = tab,
                        onSelect = { tabName = it.name },
                    )
                },
            ) { pad ->
                Box(modifier = Modifier.fillMaxSize().padding(pad)) {
                    when (tab) {
                        Tab.Schedule -> {
                            ScheduleTab(date, scheduleRefresh) { anchor = it }
                        }

                        Tab.Grades -> {
                            // ponytail: источника оценок ещё нет (story e03s01) —
                            // экран собран по Figma, отдаёт честное пустое состояние.
                            GradesScreen(SyncUiState.Empty(EmptyReason.NoData))
                        }

                        Tab.Home -> {
                            StatusCard("Главная", SyncUiState.Empty(EmptyReason.NoData).statusText())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleTab(
    date: LocalDate,
    refreshKey: Int,
    onAnchor: (WeekAnchor) -> Unit,
) {
    val context = LocalContext.current
    val store = remember { SqliteScheduleStore(context.applicationContext) }
    val repository = remember { ScheduleRepository(OkHttpHttpGet(), store) }
    var state by remember { mutableStateOf<SyncUiState<List<ScheduleEntry>>>(SyncUiState.Loading) }
    var anchor by remember { mutableStateOf(WeekAnchor.guessed(LocalDate.now())) }
    LaunchedEffect(refreshKey) {
        state =
            withContext(Dispatchers.IO) {
                val cached = store.entries()
                // Якорь недели спросим с сервера даже если расписание не загрузилось: подпись дня без него врёт.
                anchor =
                    try {
                        repository.weekAnchor(LocalDate.now())
                    } catch (e: Exception) {
                        WeekAnchor.guessed(LocalDate.now())
                    }
                onAnchor(anchor)
                try {
                    when (repository.load(ScheduleTarget.DEFAULT)) {
                        is GroupLoad.Loaded -> {
                            SyncUiState.Content(store.entries(), System.currentTimeMillis())
                        }

                        is GroupLoad.Unresolved -> {
                            if (cached.isEmpty()) {
                                SyncUiState.FailedNoCache("группа исчезла из справочника")
                            } else {
                                SyncUiState.StaleWithError(
                                    cached,
                                    System.currentTimeMillis(),
                                    "группа исчезла из справочника",
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (cached.isEmpty()) {
                        SyncUiState.FailedNoCache(e.message ?: "сеть недоступна")
                    } else {
                        SyncUiState.StaleWithError(cached, System.currentTimeMillis(), e.message ?: "сеть недоступна")
                    }
                }
            }
    }
    DayScreen(state = state, date = date, anchor = anchor)
}

@Composable
private fun GraDuleDrawer(
    current: Tab,
    dark: Boolean,
    onSelect: (Tab) -> Unit,
    onToggleTheme: () -> Unit,
    onRefresh: () -> Unit,
) {
    ModalDrawerSheet(drawerContainerColor = Bg) {
        Column(Modifier.padding(horizontal = 12.dp)) {
            Text(
                "GraDule",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = Card,
                modifier = Modifier.padding(16.dp),
            )
            HorizontalDivider(color = Card)
            Tab.entries.forEach { entry ->
                NavigationDrawerItem(
                    label = { Text(entry.label) },
                    selected = entry == current,
                    onClick = { onSelect(entry) },
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            HorizontalDivider(color = Card, modifier = Modifier.padding(vertical = 8.dp))
            Action("Обновить расписание", onRefresh)
            Action(if (dark) "Тема: тёмная" else "Тема: светлая", onToggleTheme)
        }
    }
}

@Composable
private fun Action(
    label: String,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}
