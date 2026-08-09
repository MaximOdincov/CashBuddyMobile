package com.smartbudget.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.domain.model.PerformedAction

/**
 * Карточка действия, выполненного AI.
 * Кликабельная — навигация к релевантному экрану через [onClick].
 */
@Composable
fun ActionCard(
    action: PerformedAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (action.applied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val bg = if (action.applied) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
             else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    val icon = actionIcon(action.type)
    val label = actionLabel(action.type)

    Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    action.summary,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("→", color = accent, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun actionIcon(type: String): String = when {
    type.contains("budget", ignoreCase = true) -> "📊"
    type.contains("goal", ignoreCase = true) || type.contains("saving", ignoreCase = true) -> "🐷"
    else -> "✅"
}

private fun actionLabel(type: String): String = when {
    type.contains("budget", ignoreCase = true) -> "Изменения в бюджете"
    type.contains("goal", ignoreCase = true) || type.contains("saving", ignoreCase = true) -> "Изменения в целях"
    else -> "Действие выполнено"
}
