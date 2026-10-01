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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.virtualshape.gradule.ui.theme.Bg
import com.virtualshape.gradule.ui.theme.Card
import com.virtualshape.gradule.ui.theme.GraDuleTheme
import com.virtualshape.gradule.ui.theme.Ink
import com.virtualshape.gradule.ui.theme.Paper
import com.virtualshape.gradule.ui.theme.White
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Три экрана внизу (story e01s01); остальное — drawer. */
private enum class Tab(
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
    val tab = Tab.valueOf(tabName)

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
                topBar = { TopBar(onMenu = { scope.launch { drawerState.open() } }) },
                bottomBar = {
                    NavigationBar(
                        containerColor = Card,
                        contentColor = White,
                    ) {
                        Tab.entries.forEach { entry ->
                            NavigationBarItem(
                                selected = entry == tab,
                                onClick = { tabName = entry.name },
                                icon = { Icon(entry.icon, entry.label) },
                                label = {
                                    Text(
                                        entry.label,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                                colors =
                                    androidx.compose.material3.NavigationBarItemDefaults.colors(
                                        selectedIconColor = Card,
                                        selectedTextColor = White,
                                        indicatorColor = Paper,
                                        unselectedIconColor = White,
                                        unselectedTextColor = White,
                                    ),
                            )
                        }
                    }
                },
            ) { pad -> Screen(tab, pad) }
        }
    }
}

@Composable
private fun TopBar(onMenu: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu) {
            Icon(Icons.Filled.Menu, "Меню", tint = Card)
        }
        DateChip(Modifier.weight(1f).padding(horizontal = 6.dp))
        Box(
            Modifier
                .size(33.dp)
                .background(Card, RoundedCornerShape(17.dp)),
        )
    }
}

@Composable
private fun DateChip(modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val text = "${today.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault()))} • ${today.year}"
    Box(
        modifier =
            modifier
                .background(Card, RoundedCornerShape(9.dp))
                .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = White,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            textAlign = TextAlign.Center,
        )
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

/** До e02/e03 тут пустые состояния — честные, не «тихие» (story e01s01). */
@Composable
private fun Screen(
    tab: Tab,
    pad: PaddingValues,
) {
    when (tab) {
        Tab.Schedule -> {
            // Выбор группы и загрузка — следующий срез; пока экран дня с честным пустым состоянием.
            var dateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
            var subnum by rememberSaveable { mutableStateOf<Int?>(null) }
            DayScreen(
                state = SyncUiState.Empty(EmptyReason.NoData),
                date = LocalDate.parse(dateText),
                onDateChange = { dateText = it.toString() },
                modifier = Modifier.padding(pad),
                subnumFilter = subnum,
                onSubnumChange = { subnum = it },
            )
        }

        else ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(pad)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                StatusCard(
                    title = tab.label,
                    state = SyncUiState.Empty(EmptyReason.NoData),
                )
            }
    }
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
                .padding(10.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Paper, RoundedCornerShape(6.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = if (state is SyncUiState.Content) Card else Ink,
            )
            Box(Modifier.weight(1f).width(8.dp))
            Text(
                state.statusText(),
                style = MaterialTheme.typography.bodySmall,
                color = Ink.copy(alpha = 0.65f),
            )
        }
    }
}
