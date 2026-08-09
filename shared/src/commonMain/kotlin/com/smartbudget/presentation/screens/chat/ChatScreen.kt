package com.smartbudget.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudget.domain.model.PerformedAction
import com.smartbudget.presentation.components.ActionCard
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ChatScreen(
    onNavigateToBudget: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    viewModel: ChatViewModel = koinInject()
) {
    val messages by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    var input by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        // Заголовок с AI-орбом
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .background(
                        Brush_radialGradient(MaterialTheme.colorScheme.primary)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Финансовый ассистент", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Анализирует бюджет и цели", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider()

        // Лента
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Чем помочь?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                    }
                }
                items(listOf(
                    "Сколько я могу потратить сегодня?",
                    "Куда ушли деньги в этом месяце?",
                    "Помоги распределить доход",
                    "Как быстрее накопить?",
                    "Переведи 5% из развлечений в продукты"
                )) { hint ->
                    AssistChip(
                        onClick = { viewModel.send(hint) },
                        label = { Text(hint) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            items(messages) { msg ->
                MessageItem(
                    msg = msg,
                    onActionClick = { type ->
                        if (type.contains("budget", ignoreCase = true)) onNavigateToBudget()
                        else if (type.contains("goal", ignoreCase = true) || type.contains("saving", ignoreCase = true)) onNavigateToGoals()
                    }
                )
            }
            if (isSending) {
                item {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Анализирую...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        HorizontalDivider()
        // Поле ввода
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Спросите ассистента...") },
                maxLines = 3,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (input.isNotBlank()) { viewModel.send(input.trim()); input = "" }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Text("➤", fontSize = 18.sp) }
        }
    }
}

@Composable
private fun MessageItem(msg: UiMessage, onActionClick: (String) -> Unit) {
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val bg = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = bg,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                msg.text,
                color = fg,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        // Карточки действий под сообщением AI
        if (msg.actions.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                msg.actions.forEach { action ->
                    ActionCard(action = action, onClick = { onActionClick(action.type) })
                }
            }
        }
    }
}

// мини-хелпер радиального градиента для орба
private fun Brush_radialGradient(color: Color): androidx.compose.ui.graphics.Brush =
    androidx.compose.ui.graphics.Brush.radialGradient(
        colors = listOf(color, color.copy(alpha = 0.7f))
    )
