package com.smartbudget.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.core.storage.AppSettings
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    onThemeChanged: () -> Unit,
    appSettings: AppSettings = koinInject()
) {
    var theme by remember { mutableStateOf(appSettings.themeMode) }
    var apiUrl by remember { mutableStateOf(appSettings.baseUrl) }
    var savedMsg by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Настройки") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            Text("Внешний вид", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Тема", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            listOf("system" to "Системная", "light" to "Светлая", "dark" to "Тёмная").forEach { (value, label) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = theme == value, onClick = {
                        theme = value
                        appSettings.themeMode = value
                        onThemeChanged()
                    })
                    Text(label)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Сервер", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = apiUrl,
                onValueChange = { apiUrl = it },
                label = { Text("URL API") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                appSettings.baseUrl = apiUrl.trim()
                savedMsg = true
            }) { Text("Сохранить") }
            if (savedMsg) {
                Spacer(Modifier.height(8.dp))
                Text("✅ Сохранено", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
