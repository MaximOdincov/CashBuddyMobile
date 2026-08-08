package com.smartbudget.presentation.screens.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.domain.model.GoalView
import org.koin.compose.koinInject

@Composable
fun GoalsScreen(viewModel: GoalsViewModel = koinInject()) {
    val state by viewModel.state.collectAsState()
    var contributeGoal by remember { mutableStateOf<GoalView?>(null) }
    var amount by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Цели") }) }) { padding ->
        when (val s = state) {
            is GoalsState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is GoalsState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
            is GoalsState.Success -> LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(s.goals) { goal ->
                    GoalCard(goal, onContribute = { contributeGoal = goal; amount = "" })
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
                Button(onClick = {
                    viewModel.contribute(goal.id, amount)
                    contributeGoal = null
                }) { Text("Пополнить") }
            },
            dismissButton = { TextButton(onClick = { contributeGoal = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun GoalCard(goal: GoalView, onContribute: () -> Unit) {
    val progress = (goal.progress.toDoubleOrNull() ?: 0.0) / 100f
    Card(Modifier.fillMaxWidth().padding(16.dp, 4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(goal.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("${goal.currentAmount} / ${goal.targetAmount} ₽", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Text("${goal.progress}% от цели", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            goal.targetDate?.let {
                Spacer(Modifier.height(4.dp))
                Text("Цель до $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onContribute) { Text("Пополнить") }
        }
    }
}
