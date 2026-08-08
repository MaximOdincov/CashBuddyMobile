package com.smartbudget.presentation.screens.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import com.smartbudget.core.storage.AppSettings

@Composable
fun MoreScreen(
    onGoals: () -> Unit,
    onNotifications: () -> Unit,
    onTransactions: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    appSettings: AppSettings = koinInject()
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Ещё", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
        }
        item { MenuCard("🐷 Цели и накопления", "Управляйте финансовыми целями", onGoals) }
        item { MenuCard("🔔 Уведомления", "Пороги, советы и события", onNotifications) }
        item { MenuCard("📋 Транзакции", "История всех операций", onTransactions) }
        item { MenuCard("⚙️ Настройки", "Сервер, тема, выход", onSettings) }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            OutlinedButton(
                onClick = { appSettings.logout(); onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("🚪 Выйти из аккаунта")
            }
        }
    }
}

@Composable
private fun MenuCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
