package com.virtualshape.gradule.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun GraDuleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Дизайн брендовый, поэтому динамические цвета Android 12+ выключены по умолчанию;
    // флаг оставлен — включать в настройках, когда решим.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
                dynamicDarkColorScheme(context)
            }

            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColors
            }

            else -> {
                LightColors
            }
        }
    MaterialTheme(colorScheme = colors, typography = GraDuleTypography, content = content)
}
