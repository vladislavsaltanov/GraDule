package com.virtualshape.gradule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.virtualshape.gradule.domain.schedule.DayLabel
import com.virtualshape.gradule.ui.theme.Card
import com.virtualshape.gradule.ui.theme.Paper
import com.virtualshape.gradule.ui.theme.TopBar
import com.virtualshape.gradule.ui.theme.White
import java.time.LocalDate

/**
 * Каркас экрана по дизайну (docs/design/DESIGN.md, «Геометрия»):
 * чёрная полоса 36 dp, шапка 33 dp с аватарами и чипом даты, плавающая панель 271 dp.
 * Все числа — dp из прототипа (px / 3).
 */

/** Чёрная полоса-статусбар: 36 dp под системной строкой состояния. */
@Composable
fun TopBand(modifier: Modifier = Modifier) {
    val cutout = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier
            .fillMaxWidth()
            .background(TopBar)
            // 36 dp из дизайна, но на устройстве с вырезом полоса не должна быть ниже него
            .height(maxOf(36.dp, cutout)),
    )
}

/** Шапка: круг-меню, чип даты (по клику — сегодня), круг-аватар. */
@Composable
fun HeaderRow(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 9.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleSlot(onMenu)
        DateChip(
            date = date,
            onDateChange = onDateChange,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 11.dp),
        )
        CircleSlot(onClick = {})
    }
}

@Composable
private fun CircleSlot(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(33.dp)
            .background(Card, RoundedCornerShape(50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Person, null, tint = Paper, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun DateChip(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(33.dp)
            .background(Card, RoundedCornerShape(9.dp))
            .clickable { onDateChange(LocalDate.now()) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            DayLabel.date(date),
            style = MaterialTheme.typography.titleMedium,
            color = White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

/** Плавающая нижняя панель: подпись + светлый рельс + тёмный слот активной вкладки. */
@Composable
fun GraDuleNavBar(
    caption: String,
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(TopBar)
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            Modifier
                .width(271.dp)
                .shadow(4.dp, RoundedCornerShape(11.dp))
                .background(Card, RoundedCornerShape(11.dp)),
        ) {
            Text(
                caption,
                style = MaterialTheme.typography.labelSmall,
                color = White,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(43.dp)
                    .background(Paper, RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Tab.entries.forEach { entry ->
                        NavItem(
                            active = entry == selected,
                            onClick = { onSelect(entry) },
                            modifier = Modifier.weight(1f),
                            icon = { Icon(entry.icon, entry.label, modifier = Modifier.size(16.dp)) },
                        )
                    }
                }
            }
        }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun NavItem(
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(43.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (active) {
            Box(
                Modifier
                    .size(width = 67.dp, height = 39.dp)
                    .background(Card, RoundedCornerShape(9.dp)),
            )
        }
        Box(Modifier.padding(4.dp)) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides if (active) Paper else Card,
            ) { icon() }
        }
    }
}
