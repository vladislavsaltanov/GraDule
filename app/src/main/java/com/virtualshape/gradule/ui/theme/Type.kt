package com.virtualshape.gradule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Cascadia Code/Mono из дизайна не бандлим: +1.5 МБ APK. Моноширинный системный —
// ближайший бесплатный аналог (см. DESIGN.md «Шрифты»).
private val Mono = FontFamily.Monospace

val GraDuleTypography =
    Typography(
        titleLarge = TextStyle(fontFamily = Mono, fontSize = 18.sp, fontWeight = FontWeight.Bold),
        titleMedium = TextStyle(fontFamily = Mono, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
        bodyMedium = TextStyle(fontFamily = Mono, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
        bodySmall =
            TextStyle(
                fontFamily = Mono,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink.copy(alpha = 0.65f),
            ),
        labelSmall = TextStyle(fontFamily = Mono, fontSize = 9.sp),
    )
