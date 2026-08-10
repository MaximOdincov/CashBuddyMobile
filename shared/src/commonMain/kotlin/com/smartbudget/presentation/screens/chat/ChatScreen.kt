package com.smartbudget.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudget.presentation.components.ActionCard
import com.smartbudget.presentation.components.CashBuddyTopBar
import com.smartbudget.presentation.components.MarkdownText
import com.smartbudget.presentation.components.neutralTextFieldColors
import org.koin.compose.koinInject

@Composable
fun ChatScreen(
    onNavigateToBudget: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onSettings: () -> Unit = {},
    viewModel: ChatViewModel = koinInject()
) {
    val messages by viewModel.messages.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    var input by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = { CashBuddyTopBar("Ассистент", onNotifications, onSettings) },
        // П.5: клик вне клавиатуры скрывает её
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize()
                // П.4: поле ввода поднимается с клавиатурой, с отступом
                .imePadding()
        ) {
            val listState = rememberLazyListState()
            // Авто-скролл к последнему сообщению
            LaunchedEffect(messages.size, isSending) {
                val last = messages.lastIndex + if (isSending) 1 else 0
                if (last >= 0) listState.animateScrollToItem(last)
            }

            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                state = listState,
                // П.3: контент прижат вниз, как в мессенджерах (пустое место сверху)
                verticalArrangement = if (messages.isEmpty()) Arrangement.Top
                                      else Arrangement.spacedBy(10.dp, Alignment.Bottom),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(64.dp).clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))),
                                contentAlignment = Alignment.Center
                            ) { Text("✦", color = MaterialTheme.colorScheme.onPrimary, fontSize = 28.sp) }
                            Spacer(Modifier.height(12.dp))
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
            // Поле ввода — с padding от клавиатуры (imePadding уже на Column)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Спросите ассистента...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    colors = neutralTextFieldColors()
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
            // П.7: Markdown-рендеринг вместо обычного Text
            MarkdownText(
                text = msg.text,
                color = fg,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
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
