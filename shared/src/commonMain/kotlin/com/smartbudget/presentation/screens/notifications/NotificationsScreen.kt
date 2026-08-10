package com.smartbudget.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.domain.model.NotificationDto
import com.smartbudget.presentation.components.BackArrow
import com.smartbudget.presentation.theme.DangerRed
import com.smartbudget.presentation.theme.SavingsGreen
import com.smartbudget.presentation.theme.WarnPeach
import org.koin.compose.koinInject

@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    viewModel: NotificationsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Уведомления") }, navigationIcon = { BackArrow(onBack) }) }
    ) { padding ->
        when (val s = state) {
            is NotificationsState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is NotificationsState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(s.message, color = MaterialTheme.colorScheme.error) }
            is NotificationsState.Success -> {
                if (s.items.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("Нет уведомлений", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        Modifier.padding(padding).fillMaxSize(),
                        contentPadding = PaddingValues(12.dp, 8.dp, 12.dp, 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.items) { n -> NotificationCard(n) { viewModel.markRead(n.id) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(n: NotificationDto, onClick: () -> Unit) {
    val color = when (n.severity) {
        "CRITICAL" -> DangerRed
        "WARN" -> WarnPeach
        else -> SavingsGreen
    }
    // ВСЕ карточки белые/тёмные (surface), без тонировки
    Surface(
        onClick = onClick,
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            // маленькая цветная точка-индикатор вместо тонировки
            Box(Modifier.size(10.dp).padding(top = 6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(n.title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(n.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!n.isRead) {
                // маленькая точка "непрочитано" справа
                Box(Modifier.size(8.dp).padding(top = 4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            }
        }
    }
}
