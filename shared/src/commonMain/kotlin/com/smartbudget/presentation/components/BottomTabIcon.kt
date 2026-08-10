package com.smartbudget.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Монохромная векторная иконка вкладки (рисуется в Canvas, без эмодзи и material-icons).
 * Цвет: primary если выбрано, иначе onSurfaceVariant.
 */
@Composable
fun BottomTabIcon(route: String, selected: Boolean, modifier: Modifier = Modifier) {
    val color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier.size(24.dp)) {
        when (route) {
            "budget" -> drawDonutIcon(color)
            "chat" -> drawOrbIcon(color)
            else -> drawListIcon(color)
        }
    }
}

/** Кольцо с сегментом — символ бюджета/распределения. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDonutIcon(color: Color) {
    val stroke = 2.5f
    val arcSize = Size(size.minDimension - stroke, size.minDimension - stroke)
    val topLeft = Offset((size.width - arcSize.width) / 2, (size.height - arcSize.height) / 2)
    // фоновое кольцо
    drawArc(
        color = color.copy(alpha = 0.25f),
        startAngle = 0f, sweepAngle = 360f, useCenter = false,
        topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
    )
    // активный сегмент (~70%)
    drawArc(
        color = color,
        startAngle = -90f, sweepAngle = 252f, useCenter = false,
        topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
    )
}

/** Орб/круг — символ AI-ассистента. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrbIcon(color: Color) {
    val r = size.minDimension / 2f
    // внешнее мягкое кольцо
    drawCircle(
        color = color.copy(alpha = 0.2f),
        radius = r * 0.95f,
        center = Offset(size.width / 2, size.height / 2)
    )
    // основное ядро
    drawCircle(
        color = color,
        radius = r * 0.6f,
        center = Offset(size.width / 2, size.height / 2)
    )
}

/** Список (3 строки с точками-маркерами) — символ «Траты». */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawListIcon(color: Color) {
    val w = size.width
    val h = size.height
    val rows = 3
    val rowH = h / rows
    val dotR = rowH * 0.18f
    val lineH = rowH * 0.14f
    for (i in 0 until rows) {
        val y = rowH * (i + 0.5f)
        // точка-маркер слева
        drawCircle(color = color, radius = dotR, center = Offset(dotR + 1f, y))
        // линия справа от точки
        drawRect(
            color = color,
            topLeft = Offset(dotR * 2 + 5f, y - lineH / 2),
            size = Size(w - dotR * 2 - 6f, lineH)
        )
    }
}
