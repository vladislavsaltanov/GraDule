package com.virtualshape.gradule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.virtualshape.gradule.data.GroupKey
import com.virtualshape.gradule.data.GroupLoad
import com.virtualshape.gradule.data.OkHttpHttpGet
import com.virtualshape.gradule.data.ScheduleRepository
import com.virtualshape.gradule.data.SqliteScheduleStore
import com.virtualshape.gradule.domain.schedule.DayLabel
import com.virtualshape.gradule.domain.schedule.ScheduleEntry
import com.virtualshape.gradule.ui.theme.Bg
import com.virtualshape.gradule.ui.theme.Card
import com.virtualshape.gradule.ui.theme.GraDuleTheme
import com.virtualshape.gradule.ui.theme.Ink
import com.virtualshape.gradule.ui.theme.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Дефолтная группа до экрана выбора: ММ и ИИ, 1 курс, группа 7 (id 185). */
private val DEFAULT_GROUP = GroupKey(gradeId = 1, num = 7, name = "ММ и ИИ")

/** Три экрана внизу (story e01s01); остальное — drawer. */
enum class Tab(
    val label: String,
    val icon: ImageVector,
) {
    Home("Главная", Icons.Filled.Home),
    Schedule("Расписание", Icons.Filled.DateRange),
    Grades("Оценки", Icons.Filled.Star),
}

@Composable
fun GraDuleApp() {
    var dark by rememberSaveable { mutableStateOf(false) }
    var tabName by rememberSaveable { mutableStateOf(Tab.Home.name) }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val tab = Tab.valueOf(tabName)
    val date = LocalDate.parse(dateText)

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
                    onDismiss = closeDrawer,
                )
            },
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    Column {
                        TopBand()
                        HeaderRow(
                            date = date,
                            onDateChange = { dateText = it.toString() },
                            onMenu = { scope.launch { drawerState.open() } },
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                },
                bottomBar = {
                    GraDuleNavBar(
                        caption = tab.caption(date),
                        selected = tab,
                        onSelect = { tabName = it.name },
                    )
                },
            ) { pad -> Screen(tab, date, pad) }
        }
    }
}

/** Подпись панели — день и чётность недели, как в прототипе. */
private fun Tab.caption(date: LocalDate): String = DayLabel.of(date)

/** До e02/e03 тут пустые состояния — честные, не «тихие» (story e01s01). */
@Composable
private fun Screen(
    tab: Tab,
    date: LocalDate,
    pad: PaddingValues,
) {
    when (tab) {
        Tab.Schedule -> {
            // ponytail: дефолтная группа до экрана выбора (остаток e02s02).
            val context = LocalContext.current
            val store = remember { SqliteScheduleStore(context.applicationContext) }
            val repository = remember { ScheduleRepository(OkHttpHttpGet(), store) }
            var schedule by remember { mutableStateOf<SyncUiState<List<ScheduleEntry>>>(SyncUiState.Loading) }
            LaunchedEffect(Unit) {
                schedule =
                    withContext(Dispatchers.IO) {
                        val cached = store.entries()
                        try {
                            when (repository.load(DEFAULT_GROUP)) {
                                is GroupLoad.Loaded -> {
                                    SyncUiState.Content(store.entries(), System.currentTimeMillis())
                                }

                                is GroupLoad.Unresolved -> {
                                    if (cached.isEmpty()) {
                                        SyncUiState.FailedNoCache("группа исчезла из справочника")
                                    } else {
                                        SyncUiState.StaleWithError(cached, System.currentTimeMillis(), "группа исчезла из справочника")
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
            DayScreen(
                state = schedule,
                date = date,
                modifier = Modifier.padding(pad),
            )
        }

        else -> {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(horizontal = 5.dp),
            ) {
                StatusCard(
                    title = tab.label,
                    state = SyncUiState.Empty(EmptyReason.NoData),
                )
            }
        }
    }
}

@Composable
private fun GraDuleDrawer(
    current: Tab,
    dark: Boolean,
    onSelect: (Tab) -> Unit,
    onToggleTheme: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalDrawerSheet(drawerContainerColor = Bg) {
        Column(Modifier.padding(horizontal = 12.dp)) {
            Text(
                "GraDule",
                style = MaterialTheme.typography.titleLarge,
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
            Action("Напоминания", Icons.Filled.Star, onDismiss)
            Action("Обновить", Icons.Filled.Refresh, onDismiss)
            Action("Импорт / экспорт", Icons.Filled.Settings, onDismiss)
            Action(if (dark) "Тема: тёмная" else "Тема: светлая", Icons.Filled.Settings, onToggleTheme)
        }
    }
}

@Composable
private fun Action(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, null) },
        modifier = Modifier.padding(vertical = 2.dp),
    )
}

/** Карточка-папка из дизайна: тёмная подложка + светлый лист (docs/design/DESIGN.md). */
@Composable
fun StatusCard(
    title: String,
    state: SyncUiState<*>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Card, RoundedCornerShape(9.dp))
                .padding(top = 10.dp, bottom = 10.dp, start = 3.dp, end = 3.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Paper, RoundedCornerShape(6.dp))
                .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                state.statusText(),
                style = MaterialTheme.typography.bodySmall,
                color = Ink.copy(alpha = 0.65f),
            )
        }
    }
}
