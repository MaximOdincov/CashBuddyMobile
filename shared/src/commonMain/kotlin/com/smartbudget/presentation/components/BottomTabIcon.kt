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
            else -> drawGridIcon(color)
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

/** Сетка из 4 квадратов — символ «Ещё». */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridIcon(color: Color) {
    val gap = 3f
    val cell = (size.minDimension - gap) / 2f
    val positions = listOf(
        Offset(0f, 0f),
        Offset(cell + gap, 0f),
        Offset(0f, cell + gap),
        Offset(cell + gap, cell + gap)
    )
    for (p in positions) {
        drawRect(color = color, topLeft = p, size = Size(cell, cell))
    }
}
