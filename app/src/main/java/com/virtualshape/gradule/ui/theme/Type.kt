package com.virtualshape.gradule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.virtualshape.gradule.R

/** Cascadia Mono (OFL, Microsoft) — заголовки и текст. */
val Mono =
    FontFamily(
        Font(R.font.cascadia_mono_regular, FontWeight.Normal),
        Font(R.font.cascadia_mono_semibold, FontWeight.SemiBold),
        Font(R.font.cascadia_mono_bold, FontWeight.Bold),
    )

/** Cascadia Code (OFL, Microsoft) — дата, оценка, подписи дока. */
val Code =
    FontFamily(
        Font(R.font.cascadia_code_regular, FontWeight.Normal),
        Font(R.font.cascadia_code_semibold, FontWeight.SemiBold),
    )

/** IBM Plex Mono (OFL, IBM) — время пары в новом стиле ячейки. */
val Plex =
    FontFamily(Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold))

/**
 * Роли из макета ячейки: размеры и боксы — px/3. Лишнего воздуха в line-height
 * нет: у мелких строк это бокс из макета (25/30/33 px), а не «шрифт + интерлиньяж».
 */
val GraDuleTypography =
    Typography(
        // Заголовок пары: 60 px / 700 Mono, 2 строки в 132 px (лист 215 px: 20 + 132 + 20 + 37)
        titleLarge =
            TextStyle(
                fontFamily = Mono,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
        // Дисциплина в оценке: 55 px / 600 Mono, lh 52
        titleMedium =
            TextStyle(
                fontFamily = Mono,
                fontSize = 18.33.sp,
                lineHeight = 17.33.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        // Время пары: IBM Plex Mono 30 px / 600, бокс 25 px
        bodyLarge =
            TextStyle(
                fontFamily = Plex,
                fontSize = 10.sp,
                lineHeight = 8.33.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        // «ауд. 120 • лекция»: 32 px / 600 Mono, бокс 33 px
        bodyMedium =
            TextStyle(
                fontFamily = Mono,
                fontSize = 10.67.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        // Преподаватель: 27 px / 600 Mono 65 %, бокс 30 px
        bodySmall =
            TextStyle(
                fontFamily = Mono,
                fontSize = 9.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink.copy(alpha = 0.65f),
            ),
        // Дата в чипе шапки: 48 px / 400 Code, lh 55.8
        labelLarge =
            TextStyle(
                fontFamily = Code,
                fontSize = 16.sp,
                lineHeight = 18.59.sp,
                fontWeight = FontWeight.Normal,
            ),
        // Подпись дока: 28 px / 400 Code, lh 32.5
        labelMedium =
            TextStyle(
                fontFamily = Code,
                fontSize = 9.33.sp,
                lineHeight = 10.85.sp,
                fontWeight = FontWeight.Normal,
            ),
    )
