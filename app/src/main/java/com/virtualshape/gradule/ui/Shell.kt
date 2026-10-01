package com.virtualshape.gradule.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.virtualshape.gradule.domain.schedule.DayLabel
import com.virtualshape.gradule.ui.theme.Bg
import com.virtualshape.gradule.ui.theme.Card
import com.virtualshape.gradule.ui.theme.Ink
import com.virtualshape.gradule.ui.theme.Paper
import com.virtualshape.gradule.ui.theme.White
import java.time.LocalDate

/**
 * Каркас приложения по Figma (ScheduleScreenNew 76:187 / GradesScreen 81:112).
 * На Android фон `bg` проходит под прозрачным status bar; геометрия шапки и дока
 * сохраняет Figma-координаты при учёте системных inset.
 */

/** Продолжает фон экрана под прозрачным системным статус-баром и резервирует его inset. */
@Composable
fun TopBand(modifier: Modifier = Modifier) {
    val cutout = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier
            .fillMaxWidth()
            .background(Bg)
            .height(cutout.coerceAtLeast(24.dp)),
    )
}

/**
 * Шапка: круг-меню 100 px (33.33 dp), чип даты 836×100 px (278.67×33.33 dp, r 9.33),
 * круг-аватар. Поля по краям 26 px (8.67 dp), промежутки 32 px (10.67 dp).
 * Сверху фиксированные 12 dp от системного статус-бара: на устройствах с высоким
 * inset шапка не прижималась к часам.
 */
@Composable
fun HeaderRow(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.67.dp)
            .padding(top = 12.dp, bottom = 16.67.dp),
        horizontalArrangement = Arrangement.spacedBy(10.67.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleSlot(
            icon = Icons.Filled.Menu,
            contentDescription = "Открыть меню",
            onClick = onMenu,
        )
        DateChip(
            date = date,
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day -> onDateChange(LocalDate.of(year, month + 1, day)) },
                    date.year,
                    date.monthValue - 1,
                    date.dayOfMonth,
                ).show()
            },
            modifier = Modifier.weight(1f),
        )
        CircleSlot(icon = Icons.Filled.Person, contentDescription = "Профиль")
    }
}

/** Чип даты: текст 16 sp Code по центру, иконка календаря 20 dp у правого края (60 px). */
@Composable
private fun DateChip(
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(33.33.dp)
            .clip(RoundedCornerShape(9.33.dp))
            .background(Card)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            DayLabel.date(date),
            style = MaterialTheme.typography.labelLarge,
            color = White,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
        ) {
            Icon(Icons.Filled.DateRange, null, tint = White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CircleSlot(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier =
        onClick?.let { Modifier.clickable(onClick = it, role = Role.Button) } ?: Modifier
    Box(
        modifier
            .size(33.33.dp)
            .clip(CircleShape)
            .background(Card)
            .then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = White, modifier = Modifier.size(15.dp))
    }
}

/**
 * Плавающий док: 814×188 px (271.33×62.67 dp), r 32 px (10.67 dp). Подпись 28 px
 * Code (отступ 10 px сверху), рельс `paper` 814×130 px (43.33 dp) с тенью
 * 0 4/4/1, под активной вкладкой тёмный слот 200×116 px (66.67×38.67 dp, r 9.33),
 * иконки 53 px (17.67 dp) с шагом 200 px (66.67 dp). Активная иконка белая,
 * неактивные — чёрные.
 */
@Composable
fun GraDuleNavBar(
    caption: String,
    tabs: List<Tab>,
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.67.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .width(271.33.dp)
                .shadow(4.dp, RoundedCornerShape(10.67.dp))
                .clip(RoundedCornerShape(10.67.dp))
                .background(Card),
        ) {
            Text(
                caption,
                style = MaterialTheme.typography.labelMedium,
                color = White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp, top = 3.33.dp, bottom = 5.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(43.33.dp)
                    .background(Paper, RoundedCornerShape(bottomStart = 10.67.dp, bottomEnd = 10.67.dp)),
                contentAlignment = Alignment.Center,
            ) {
                DockIcons(tabs, selected, onSelect)
            }
        }
    }
}

/** Figma dock uses four tracks; reserve unknown routes until their screens and icons exist. */
private const val DOCK_SLOT_COUNT = 4

@Composable
private fun DockIcons(
    tabs: List<Tab>,
    selected: Tab,
    onSelect: (Tab) -> Unit,
) {
    val slot = 66.67.dp
    val group = slot * DOCK_SLOT_COUNT.toFloat()
    Box(Modifier.width(group), contentAlignment = Alignment.Center) {
        Row(Modifier.width(group)) {
            tabs.take(DOCK_SLOT_COUNT).forEach { entry ->
                DockItem(
                    active = entry == selected,
                    onClick = { onSelect(entry) },
                    entry = entry,
                    modifier = Modifier.width(slot),
                )
            }
            repeat((DOCK_SLOT_COUNT - tabs.size).coerceAtLeast(0)) {
                Spacer(Modifier.width(slot).height(38.67.dp))
            }
        }
    }
}

@Composable
private fun DockItem(
    active: Boolean,
    onClick: () -> Unit,
    entry: Tab,
    modifier: Modifier = Modifier,
) {
    val tint = if (active) White else Color.Black
    Box(
        modifier
            .fillMaxWidth()
            .height(38.67.dp)
            .clip(RoundedCornerShape(9.33.dp))
            .selectable(selected = active, onClick = onClick, role = Role.Tab),
        contentAlignment = Alignment.Center,
    ) {
        if (active) {
            Box(
                Modifier
                    .size(width = 66.67.dp, height = 38.67.dp)
                    .background(Card, RoundedCornerShape(9.33.dp)),
            )
        }
        if (entry.iconRes != 0) {
            Icon(
                painterResource(entry.iconRes),
                entry.label,
                tint = tint,
                modifier = Modifier.size(17.67.dp),
            )
        } else {
            Icon(entry.icon, entry.label, tint = tint, modifier = Modifier.size(17.67.dp))
        }
    }
}

/** Карточка-папка для пустых состояний и ошибок: та же подложка и лист. */
@Composable
fun StatusCard(
    title: String,
    status: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().padding(horizontal = 5.33.dp)) {
        CardSubstrate(height = 63.33.dp) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 3.33.dp, top = 4.dp)
                    .fillMaxWidth()
                    .height(55.33.dp)
                    .background(Paper, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, maxLines = 2)
                    Text(
                        status,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
