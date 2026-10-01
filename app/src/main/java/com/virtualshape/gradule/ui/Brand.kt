package com.virtualshape.gradule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.virtualshape.gradule.ui.theme.Card
import com.virtualshape.gradule.ui.theme.Paper

/**
 * Кромка листа берёт радиус и ширину из макета узла: у пары 24 px / 24 px,
 * у оценки 18 px / 18 px.
 * Рисуем двумя проходами — цветной скруглённый прямоугольник, поверх бумага
 * без правых [stripe] — иначе углы кромки скругляются сами по себе.
 */
fun Modifier.edgeStripe(
    color: Color,
    radius: Dp = 8.dp,
    stripe: Dp = 8.dp,
): Modifier =
    clip(RoundedCornerShape(radius)).drawBehind {
        val stripePx = stripe.toPx()
        val corner = radius.toPx()
        drawRoundRect(color = color, cornerRadius = CornerRadius(corner))
        drawRect(Paper, topLeft = Offset.Zero, size = Size(size.width - stripePx, size.height))
    }

/** Подложка карточки: `card` + тень, поля 3.33 dp по бокам (фрейм 1120 px → 1100 px). */
@Composable
fun CardSubstrate(
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 3.33.dp)
            .height(height)
            .shadow(4.dp, RoundedCornerShape(10.67.dp))
            .background(Card, RoundedCornerShape(10.67.dp)),
        content = content,
    )
}
