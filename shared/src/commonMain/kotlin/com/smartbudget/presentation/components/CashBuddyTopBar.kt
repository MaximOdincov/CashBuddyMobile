package com.smartbudget.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Единый тулбар главных экранов с минималистичными Canvas-иконками.
 */
@Composable
fun CashBuddyTopBar(
    title: String,
    onNotifications: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    unreadCount: Long = 0
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Колокольчик (уведомления)
            Box {
                CircleIconButton(onClick = onNotifications) {
                    BellIcon(color = MaterialTheme.colorScheme.onSurface)
                }
                if (unreadCount > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) { Text(if (unreadCount > 9) "9+" else unreadCount.toString(), fontSize = 9.sp) }
                }
            }
            Spacer(Modifier.width(8.dp))
            // Шестерёнка (настройки)
            CircleIconButton(onClick = onSettings) {
                GearIcon(color = MaterialTheme.colorScheme.onSurface)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
fun CircleIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(42.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}

// ===== Минималистичные Canvas-иконки =====

@Composable
private fun BellIcon(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            // Стандартный Material "notifications" контур, отмасштабированный в 24x24
            moveTo(12f, 22f); cubicTo(13.1f, 22f, 14f, 21.1f, 14f, 20f); lineTo(10f, 20f)
            cubicTo(10f, 21.1f, 10.9f, 22f, 12f, 22f); close()
            moveTo(18f, 16f); lineTo(18f, 11f)
            cubicTo(18f, 7.93f, 16.37f, 5.36f, 13.5f, 4.68f); lineTo(13.5f, 4f)
            cubicTo(13.5f, 3.17f, 12.83f, 2.5f, 12f, 2.5f)
            cubicTo(11.17f, 2.5f, 10.5f, 3.17f, 10.5f, 4f); lineTo(10.5f, 4.68f)
            cubicTo(7.64f, 5.36f, 6f, 7.92f, 6f, 11f); lineTo(6f, 16f); lineTo(4f, 18f)
            lineTo(4f, 19f); lineTo(20f, 19f); lineTo(20f, 18f); close()
        }
        val scale = size.width / 24f
        translate(left = 0f, top = 0f) {
            scale(scale, scale, pivot = Offset.Zero) {
                drawPath(path, color = color)
            }
        }
    }
}

@Composable
private fun GearIcon(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
        drawGear(color)
    }
}

private fun DrawScope.drawGear(color: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val stroke = 2f
    // внешнее кольцо
    drawCircle(color = color, radius = w * 0.32f, center = Offset(cx, cy), style = Stroke(width = stroke))
    // внутренний круг
    drawCircle(color = color, radius = w * 0.12f, center = Offset(cx, cy), style = Stroke(width = stroke))
    // 6 зубцов (короткие линии наружу)
    val toothLen = w * 0.12f
    for (i in 0..5) {
        val angle = (i * 60).toDouble() * PI / 180.0
        val innerR = w * 0.34f
        val outerR = innerR + toothLen
        val x1 = cx + (innerR * cos(angle)).toFloat()
        val y1 = cy + (innerR * sin(angle)).toFloat()
        val x2 = cx + (outerR * cos(angle)).toFloat()
        val y2 = cy + (outerR * sin(angle)).toFloat()
        drawLine(color, Offset(x1, y1), Offset(x2, y2), stroke, cap = StrokeCap.Round)
    }
}
