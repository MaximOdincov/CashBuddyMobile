package com.smartbudget.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudget.presentation.theme.DangerRed
import com.smartbudget.presentation.theme.NeutralBlue
import com.smartbudget.presentation.theme.SavingsGreen
import com.smartbudget.presentation.theme.WarnPeach

// ============ DonutChart: распределение бюджета ============

data class DonutSegment(
    val label: String,
    val value: Double,
    val color: Color
)

/**
 * Кольцевая диаграмма распределения.
 * Сегменты рисуются по часовой стрелке от верха. Сумма значений = total.
 * По центру — [centerLabel] / [centerSub].
 */
@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    centerLabel: String = "",
    centerSub: String = "",
    strokeWidth: Float = 28f
) {
    val total = segments.sumOf { it.value }.coerceAtLeast(0.0001)
    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            // фон
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth)
            )
            var start = -90f
            for (seg in segments) {
                if (seg.value <= 0.0) continue
                val sweep = (seg.value / total * 360f).toFloat()
                drawArc(
                    color = seg.color,
                    startAngle = start, sweepAngle = sweep, useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (centerLabel.isNotBlank()) {
                Text(centerLabel, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            if (centerSub.isNotBlank()) {
                Text(centerSub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ============ ProgressRing: круговой прогресс цели ============

/**
 * Круговой прогресс с цифрой в центре. [progress] 0..1.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 160.dp,
    color: Color = SavingsGreen,
    centerText: String? = null
) {
    val animated by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(800))
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(10.dp)) {
            val stroke = 16f
            val diameter = this.size.minDimension - stroke
            val topLeft = Offset((this.size.width - diameter) / 2, (this.size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
            )
            drawArc(
                color = color,
                startAngle = -90f, sweepAngle = 360f * animated, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        if (centerText != null) {
            Text(centerText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// ============ MiniProgressBar: цветная полоска категории ============

@Composable
fun MiniProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 6.dp
) {
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(color.copy(alpha = 0.15f))
    ) {
        val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), animationSpec = tween(600))
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

// ============ StatusBadge: Норма/Внимание/Превышено ============

enum class BudgetStatus { OK, WARN, OVER }

@Composable
fun StatusBadge(status: BudgetStatus, modifier: Modifier = Modifier) {
    val (text, color) = when (status) {
        BudgetStatus.OK -> "Норма" to SavingsGreen
        BudgetStatus.WARN -> "Внимание" to WarnPeach
        BudgetStatus.OVER -> "Превышено" to DangerRed
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** Определяет статус по проценту потраченного. */
fun budgetStatus(pct: Double): BudgetStatus = when {
    pct >= 100.0 -> BudgetStatus.OVER
    pct >= 80.0 -> BudgetStatus.WARN
    else -> BudgetStatus.OK
}

// ============ SectionHeader ============

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, color = MaterialTheme.colorScheme.primary) }
        }
    }
}
