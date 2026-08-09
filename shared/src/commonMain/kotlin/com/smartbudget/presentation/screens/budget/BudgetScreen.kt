package com.smartbudget.presentation.screens.budget

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudget.core.storage.AppSettings
import com.smartbudget.core.util.formatMoney
import com.smartbudget.core.util.formatPercent
import com.smartbudget.core.util.greeting
import com.smartbudget.core.util.monthName
import com.smartbudget.domain.model.BudgetItem
import com.smartbudget.domain.model.BudgetOverview
import com.smartbudget.presentation.components.BudgetStatus
import com.smartbudget.presentation.components.MiniProgressBar
import com.smartbudget.presentation.components.SectionHeader
import com.smartbudget.presentation.components.StatusBadge
import com.smartbudget.presentation.components.budgetStatus
import com.smartbudget.presentation.theme.*
import org.koin.compose.koinInject

@Composable
fun BudgetScreen(
    key: Int = 0,
    onCategoryClick: (Long, String) -> Unit,
    onAddTransaction: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenBudgetEdit: () -> Unit,
    viewModel: BudgetViewModel = koinInject(),
    appSettings: AppSettings = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val syncResult by viewModel.syncResult.collectAsState()

    // перезагрузка при изменении key (возврат после AI-действия)
    LaunchedEffect(key) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                text = { Text("Трата") }
            )
        }
    ) { padding ->
        when (val s = state) {
            is BudgetState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is BudgetState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.load() }) { Text("Повторить") }
                }
            }
            is BudgetState.Success -> OverviewContent(
                overview = s.overview,
                forecast = forecast,
                username = appSettings.username,
                onCategoryClick = onCategoryClick,
                onSync = { viewModel.syncBank() },
                onOpenGoals = onOpenGoals,
                onOpenChat = onOpenChat,
                onOpenBudgetEdit = onOpenBudgetEdit,
                syncMessage = syncResult?.let { "Импортировано ${it.imported} операций" },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun OverviewContent(
    overview: BudgetOverview,
    forecast: com.smartbudget.domain.model.Forecast?,
    username: String,
    onCategoryClick: (Long, String) -> Unit,
    onSync: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenBudgetEdit: () -> Unit,
    syncMessage: String?,
    modifier: Modifier = Modifier
) {
    val name = if (username.isNotBlank()) ", $username" else ""
    val monthTitle = monthName(overview.periodMonth)
    val daysLeft = forecast?.daysLeft ?: 0L
    val canSpend = forecast?.let { (it.monthlyIncome - it.projectedSpent).coerceAtLeast(0.0) } ?: overview.monthlyIncome
    val daily = forecast?.dailyAvg ?: 0.0
    val hasRisk = forecast?.projectedOver?.let { it > 0.0 } ?: false

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // === Верх: приветствие + период + статус ===
        item {
            Column(Modifier.padding(16.dp, 12.dp)) {
                Text(
                    "${greeting()}$name",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$monthTitle · осталось $daysLeft дн.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                StatusChip(hasRisk = hasRisk)
            }
        }

        // === Главная карточка «Можно потратить» ===
        item {
            MainCard(
                canSpend = canSpend,
                daily = daily,
                overview = overview,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // === AI-инсайт (если есть риск) ===
        if (hasRisk) {
            item {
                Spacer(Modifier.height(12.dp))
                InsightCard(
                    forecast = forecast!!,
                    onClick = onOpenChat,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // === Быстрые действия ===
        item {
            Spacer(Modifier.height(16.dp))
            QuickActions(
                onBudgetEdit = onOpenBudgetEdit,
                onAddGoal = onOpenGoals,
                onAsk = onOpenChat
            )
        }

        // === Кнопка синхронизации банка ===
        item {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSync,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("↻")
                Spacer(Modifier.width(8.dp))
                Text("Синхронизировать банк")
            }
            syncMessage?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    "  $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // === Категории ===
        item {
            SectionHeader(
                title = "Категории",
                action = "Все",
                onAction = onOpenBudgetEdit
            )
        }
        // сортируем: сначала требующие внимания, потом по тратам
        val sorted = overview.allocations
            .filter { !it.isSavings }
            .sortedWith(compareByDescending<BudgetItem> { it.pct }.thenByDescending { it.spent })
        items(sorted.take(6)) { item -> CategoryRow(item, onClick = { onCategoryClick(item.categoryId, item.categoryName) }) }
    }
}

@Composable
private fun StatusChip(hasRisk: Boolean) {
    val (text, color) = if (hasRisk) "Нужно внимание" to WarnPeach else "Всё по плану" to SavingsGreen
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MainCard(canSpend: Double, daily: Double, overview: BudgetOverview, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Можно потратить до конца месяца", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(formatMoney(canSpend), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("${formatMoney(daily)} в день", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            // Полоса распределения: обязательные / повседневные / накопления / свободные
            val total = overview.monthlyIncome.coerceAtLeast(0.0001)
            val essential = overview.allocations.filter { !it.isSavings }.sumOf { it.limitAmount }
            val savings = overview.allocations.filter { it.isSavings }.sumOf { it.limitAmount }
            val free = (overview.monthlyIncome - essential - savings).coerceAtLeast(0.0)
            DistributionBar(
                segments = listOf(
                    "Обязательные" to (essential / total).toFloat(),
                    "Накопления" to (savings / total).toFloat(),
                    "Свободные" to (free / total).toFloat()
                )
            )
            Spacer(Modifier.height(8.dp))
            Row {
                LegendDot(SavingsGreen, "Обязательные")
                Spacer(Modifier.width(12.dp))
                LegendDot(CatSavings, "Накопления")
                Spacer(Modifier.width(12.dp))
                LegendDot(NeutralBlue, "Свободные")
            }
        }
    }
}

@Composable
private fun DistributionBar(segments: List<Pair<String, Float>>) {
    val colors = listOf(SavingsGreen, CatSavings, NeutralBlue)
    Surface(
        Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.fillMaxHeight()) {
            segments.forEachIndexed { i, (_, frac) ->
                if (frac > 0f) {
                    Box(
                        Modifier.fillMaxHeight().weight(frac).background(colors.getOrElse(i) { CatOther })
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InsightCard(forecast: com.smartbudget.domain.model.Forecast, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Похоже, расходы идут быстрее плана", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Прогноз превышения: ${formatMoney(forecast.projectedOver)}. Ассистент подскажет, что можно сделать.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text("Посмотреть варианты →", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun QuickActions(onBudgetEdit: () -> Unit, onAddGoal: () -> Unit, onAsk: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionTile("Изменить бюджет", "✎", Modifier.weight(1f), onBudgetEdit, accent = false)
        QuickActionTile("Добавить цель", "✚", Modifier.weight(1f), onAddGoal, accent = false)
    }
    Spacer(Modifier.height(8.dp))
    // Акцентная кнопка «Спросить ассистента»
    Surface(
        onClick = onAsk,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(28.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text("✦", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp) }
            Spacer(Modifier.width(10.dp))
            Text("Спросить ассистента", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun QuickActionTile(label: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit, accent: Boolean) {
    val bg = MaterialTheme.colorScheme.surface
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), color = bg, tonalElevation = 1.dp) {
        Column(
            Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun CategoryRow(item: BudgetItem, onClick: () -> Unit) {
    val status = budgetStatus(item.pct)
    val color = categoryColor(item.categoryName)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(item.icon, fontSize = 18.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.categoryName, fontWeight = FontWeight.Medium)
                    Text(
                        "${formatMoney(item.spent)} из ${formatMoney(item.limitAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status)
            }
            Spacer(Modifier.height(8.dp))
            MiniProgressBar(fraction = (item.pct / 100f).toFloat(), color = color)
            Spacer(Modifier.height(4.dp))
            Text(
                "Осталось ${formatMoney(item.remaining)} · ${formatPercent(item.pct)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
