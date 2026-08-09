package com.smartbudget.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.core.storage.AppSettings
import com.smartbudget.presentation.components.BackArrow
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onThemeChanged: () -> Unit = {},
    appSettings: AppSettings = koinInject()
) {
    var theme by remember { mutableStateOf(appSettings.themeMode) }
    var apiUrl by remember { mutableStateOf(appSettings.baseUrl) }
    var savedMsg by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }, navigationIcon = { BackArrow(onBack) }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            SectionTitle("Внешний вид")
            Spacer(Modifier.height(8.dp))
            Text("Тема", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            listOf("system" to "Системная", "light" to "Светлая", "dark" to "Тёмная").forEach { (value, label) ->
                Surface(
                    onClick = {
                        theme = value
                        appSettings.themeMode = value
                        onThemeChanged()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                        RadioButton(selected = theme == value, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Сервер")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = apiUrl, onValueChange = { apiUrl = it },
                label = { Text("URL API") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { appSettings.baseUrl = apiUrl.trim(); savedMsg = true }) { Text("Сохранить") }
            if (savedMsg) {
                Spacer(Modifier.height(6.dp))
                Text("✓ Сохранено", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}
