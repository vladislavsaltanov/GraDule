package com.virtualshape.gradule.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Токены из docs/design/DESIGN.md (прототипы claude.ai design). Не плодить копии:
// новые цвета — только сюда.
val Card = Color(0xFF3A3937)
val Paper = Color(0xFFC0C0C0)
val Bg = Color(0xFFE8E8E8)
val Ink = Color(0xFF1A1A1A)
val Practice = Color(0xFFEE7747)
val Lecture = Color(0xFF559A8A)
val White = Color(0xFFFFFFFF)

/** Чёрная полоса-статусбар из дизайна (36 dp). */
val TopBar = Color(0xFF000000)

// Тёмной темы в прототипах нет — выведена инверсией (см. DESIGN.md, «Отклонения»).
private val CardNight = Color(0xFF2A2927)
private val BgNight = Color(0xFF1A1A1A)

val LightColors =
    lightColorScheme(
        primary = Card,
        onPrimary = White,
        background = Bg,
        onBackground = Ink,
        surface = Paper,
        onSurface = Ink,
        surfaceVariant = Bg,
        onSurfaceVariant = Ink,
        secondary = Practice,
        tertiary = Lecture,
    )

val DarkColors =
    darkColorScheme(
        primary = Paper,
        onPrimary = Card,
        background = BgNight,
        onBackground = Bg,
        surface = CardNight,
        onSurface = Bg,
        surfaceVariant = CardNight,
        onSurfaceVariant = Bg,
        secondary = Practice,
        tertiary = Lecture,
    )
