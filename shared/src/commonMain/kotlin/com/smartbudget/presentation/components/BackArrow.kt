package com.smartbudget.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Кнопка «Назад» с Canvas-стрелкой (без material-icons-extended). */
@Composable
fun BackArrow(onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color? = null) {
    IconButton(onClick = onClick, modifier = modifier) {
        val color = tint ?: MaterialTheme.colorScheme.onSurface
        Canvas(Modifier.size(22.dp)) {
            val w = size.width
            val h = size.height
            val strokeW = 2.5f
            // левая V-образная часть стрелки
            drawLine(color, Offset(w * 0.35f, h * 0.2f), Offset(w * 0.15f, h * 0.5f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(color, Offset(w * 0.15f, h * 0.5f), Offset(w * 0.35f, h * 0.8f), strokeWidth = strokeW, cap = StrokeCap.Round)
            // горизонтальная линия
            drawLine(color, Offset(w * 0.15f, h * 0.5f), Offset(w * 0.85f, h * 0.5f), strokeWidth = strokeW, cap = StrokeCap.Round)
        }
    }
}
