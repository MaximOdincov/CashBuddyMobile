package com.smartbudget.presentation.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.domain.model.BudgetItem
import com.smartbudget.domain.model.BudgetOverview
import org.koin.compose.koinInject

@Composable
fun BudgetScreen(
    onCategoryClick: (Long) -> Unit,
    onAddTransaction: () -> Unit,
    viewModel: BudgetViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val syncResult by viewModel.syncResult.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Text("➕") },
                text = { Text("Добавить") }
            )
        }
    ) { padding ->
        when (val s = state) {
            is BudgetState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is BudgetState.Error -> Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadBudget() }) { Text("Повторить") }
                }
            }
            is BudgetState.Success -> BudgetContent(
                overview = s.overview,
                onCategoryClick = onCategoryClick,
                onSync = { viewModel.syncBank() },
                syncMessage = syncResult?.let { "Импортировано ${it.imported} операций" },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun BudgetContent(
    overview: BudgetOverview,
    onCategoryClick: (Long) -> Unit,
    onSync: () -> Unit,
    syncMessage: String?,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Spacer(Modifier.height(16.dp))
            // Карточка дохода
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Доход в месяц", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${overview.monthlyIncome} ₽", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Период: ${overview.periodMonth}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            // Кнопка синхронизации банка
            OutlinedButton(onClick = onSync, modifier = Modifier.fillMaxWidth()) {
                Text("🔄")
                Spacer(Modifier.width(8.dp))
                Text("Синхронизировать банк")
            }
            syncMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(20.dp))
            Text("Категории", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
        }
        items(overview.allocations) { item ->
            BudgetItemRow(item, onClick = { onCategoryClick(item.categoryId) })
        }
        item { Spacer(Modifier.height(80.dp)) } // место под FAB
    }
}

@Composable
private fun BudgetItemRow(item: BudgetItem, onClick: () -> Unit) {
    val pct = item.pct.toDoubleOrNull() ?: 0.0
    val isOver = pct >= 100.0

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.icon, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.categoryName, fontWeight = FontWeight.Medium)
                    Text("${item.spent} / ${item.limitAmount} ₽", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${item.pct}%",
                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (pct / 100f).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            if (isOver) {
                Spacer(Modifier.height(4.dp))
                Text("Лимит превышен", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            } else {
                Spacer(Modifier.height(4.dp))
                Text("Осталось ${item.remaining} ₽", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
