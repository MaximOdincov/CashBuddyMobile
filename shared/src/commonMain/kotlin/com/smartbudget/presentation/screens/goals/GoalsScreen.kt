package com.smartbudget.presentation.screens.goals

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
import androidx.compose.ui.unit.sp
import com.smartbudget.core.util.formatMoney
import com.smartbudget.domain.model.GoalView
import com.smartbudget.presentation.components.BackArrow
import com.smartbudget.presentation.components.neutralTextFieldColors
import com.smartbudget.presentation.components.ProgressRing
import com.smartbudget.presentation.theme.SavingsGreen
import org.koin.compose.koinInject

@Composable
fun GoalsScreen(
    onBack: () -> Unit = {},
    viewModel: GoalsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var contributeGoal by remember { mutableStateOf<GoalView?>(null) }
    var amount by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Цели") },
                navigationIcon = { BackArrow(onBack) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                text = { Text("Новая цель") }
            )
        }
    ) { padding ->
        when (val s = state) {
            is GoalsState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is GoalsState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
            is GoalsState.Success -> {
                if (s.goals.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("Целей пока нет. Создайте первую!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        Modifier.padding(padding).fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Text("Активная цель", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                        item { ActiveGoalCard(s.goals.first(), onContribute = { contributeGoal = s.goals.first(); amount = "" }) }
                        if (s.goals.size > 1) {
                            item { Spacer(Modifier.height(8.dp)); Text("Другие цели", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                            items(s.goals.drop(1)) { g -> MiniGoalCard(g, onContribute = { contributeGoal = g; amount = "" }) }
                        }
                    }
                }
            }
        }
    }

    // Диалог пополнения
    contributeGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { contributeGoal = null },
            title = { Text("Пополнить «${goal.title}»") },
            text = {
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Сумма, ₽") }, singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.contribute(goal.id, amount.toDoubleOrNull() ?: 0.0); contributeGoal = null }) { Text("Пополнить") }
            },
            dismissButton = { TextButton(onClick = { contributeGoal = null }) { Text("Отмена") } }
        )
    }

    // Диалог создания
    if (showCreate) {
        CreateGoalDialog(onDismiss = { showCreate = false }, onCreate = { title, target ->
            viewModel.create(title, target); showCreate = false
        })
    }
}

@Composable
private fun ActiveGoalCard(goal: GoalView, onContribute: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ProgressRing(
                progress = (goal.progress / 100f).toFloat(),
                color = SavingsGreen,
                centerText = "${goal.progress.toInt()}%"
            )
            Spacer(Modifier.height(12.dp))
            Text(goal.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "${formatMoney(goal.currentAmount)} из ${formatMoney(goal.targetAmount)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val left = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
            Spacer(Modifier.height(4.dp))
            Text("Осталось: ${formatMoney(left)}", style = MaterialTheme.typography.labelLarge)
            goal.targetDate?.let {
                Text("Срок: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onContribute, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Пополнить")
            }
        }
    }
}

@Composable
private fun MiniGoalCard(goal: GoalView, onContribute: () -> Unit) {
    val frac = (goal.progress / 100f).toFloat()
    Surface(
        onClick = onContribute,
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(SavingsGreen.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Text("${goal.progress.toInt()}%", color = SavingsGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(goal.title, fontWeight = FontWeight.Medium)
                    Text("${formatMoney(goal.currentAmount)} из ${formatMoney(goal.targetAmount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            com.smartbudget.presentation.components.MiniProgressBar(fraction = frac, color = SavingsGreen)
        }
    }
}

@Composable
private fun CreateGoalDialog(onDismiss: () -> Unit, onCreate: (String, Double) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая цель") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = neutralTextFieldColors())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Сумма, ₽") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = neutralTextFieldColors())
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title.trim(), amount.toDoubleOrNull() ?: 0.0) },
                enabled = title.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } ?: false
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
