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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudget.core.util.formatMoney
import com.smartbudget.core.util.formatPercent
import com.smartbudget.core.util.monthName
import com.smartbudget.domain.model.BudgetItem
import com.smartbudget.domain.model.BudgetOverview
import com.smartbudget.domain.model.GoalView
import com.smartbudget.presentation.components.*
import com.smartbudget.presentation.theme.*
import org.koin.compose.koinInject

@Composable
fun BudgetScreen(
    key: Int = 0,
    onCategoryClick: (Long, String) -> Unit,
    onAddTransaction: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenBudgetEdit: () -> Unit,
    onNotifications: () -> Unit = {},
    onSettings: () -> Unit = {},
    viewModel: BudgetViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val mainGoal by viewModel.mainGoal.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    LaunchedEffect(key) { viewModel.load() }

    Scaffold(
        topBar = { CashBuddyTopBar("Бюджет", onNotifications, onSettings, unreadCount = unreadCount) }
    ) { padding ->
        when (val s = state) {
            is BudgetState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
                mainGoal = mainGoal,
                onCategoryClick = onCategoryClick,
                onOpenGoals = onOpenGoals,
                onOpenBudgetEdit = onOpenBudgetEdit,
                currentMonth = currentMonth,
                onPrevMonth = { viewModel.shiftMonth(-1) },
                onNextMonth = { viewModel.shiftMonth(+1) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun OverviewContent(
    overview: BudgetOverview,
    forecast: com.smartbudget.domain.model.Forecast?,
    mainGoal: GoalView?,
    onCategoryClick: (Long, String) -> Unit,
    onOpenGoals: () -> Unit,
    onOpenBudgetEdit: () -> Unit,
    currentMonth: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = monthName(currentMonth)
    val daysLeft = forecast?.daysLeft ?: 0L
    val canSpend = forecast?.let { (it.monthlyIncome - it.projectedSpent).coerceAtLeast(0.0) } ?: overview.monthlyIncome
    val daily = forecast?.dailyAvg ?: 0.0
    val hasRisk = forecast?.projectedOver?.let { it > 0.0 } ?: false

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
        // Переключатель месяца ‹ Месяц ›
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPrevMonth) { Text("‹", style = MaterialTheme.typography.titleLarge) }
                Text(
                    monthTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                TextButton(onClick = onNextMonth) { Text("›", style = MaterialTheme.typography.titleLarge) }
            }
        }

        // Главная карточка
        item { MainCard(canSpend, daily, overview) }

        // AI-инсайт при риске
        if (hasRisk) {
            item {
                Spacer(Modifier.height(12.dp))
                InsightCard(forecast!!, Modifier.padding(horizontal = 16.dp))
            }
        }

        // Виджет главной цели
        if (mainGoal != null) {
            item {
                Spacer(Modifier.height(16.dp))
                MainGoalWidget(mainGoal!!, onOpen = onOpenGoals, Modifier.padding(horizontal = 16.dp))
            }
        }

        // Быстрые действия
        item {
            Spacer(Modifier.height(16.dp))
            QuickActions(onBudgetEdit = onOpenBudgetEdit, onGoal = onOpenGoals)
        }

        // Категории: расходы
        val expenses = overview.allocations.filter { !it.isSavings }
            .sortedWith(compareByDescending<BudgetItem> { budgetStatusRank(it.pct) }.thenByDescending { it.spent })
        item { SectionHeader("Расходы") }
        // Легенда цветов категорий
        item { CategoryLegend(expenses) }
        items(expenses) { item -> CategoryRow(item, onClick = { onCategoryClick(item.categoryId, item.categoryName) }) }

        // Категории: накопления
        val savings = overview.allocations.filter { it.isSavings }
        if (savings.isNotEmpty()) {
            item { SectionHeader("Накопления") }
            items(savings) { item -> CategoryRow(item, onClick = { onCategoryClick(item.categoryId, item.categoryName) }) }
        }
    }
}

private fun budgetStatusRank(pct: Double): Int = when {
    pct >= 100.0 -> 3
    pct >= 80.0 -> 2
    else -> 1
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun MainCard(canSpend: Double, daily: Double, overview: BudgetOverview) {
    Surface(
        Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Можно потратить до конца месяца", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(formatMoney(canSpend), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("${formatMoney(daily)} в день", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            // Полоса распределения
            val total = overview.monthlyIncome.coerceAtLeast(0.0001)
            val essential = overview.allocations.filter { !it.isSavings }.sumOf { it.limitAmount }
            val savingsSum = overview.allocations.filter { it.isSavings }.sumOf { it.limitAmount }
            val free = (overview.monthlyIncome - essential - savingsSum).coerceAtLeast(0.0)
            DistributionBar(listOf(
                "Обязательные" to (essential / total).toFloat(),
                "Накопления" to (savingsSum / total).toFloat(),
                "Свободные" to (free / total).toFloat()
            ))
            Spacer(Modifier.height(8.dp))
            Row {
                LegendDot(CatBills, "Обязательные")
                Spacer(Modifier.width(12.dp))
                LegendDot(CatSavings, "Накопления")
                Spacer(Modifier.width(12.dp))
                LegendDot(NeutralBlue, "Свободные")
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Это ваш доход, распределённый по категориям. «Свободные» — то, что можно потратить без риска для бюджета.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DistributionBar(segments: List<Pair<String, Float>>) {
    val colors = listOf(CatBills, CatSavings, NeutralBlue)
    Surface(
        Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.fillMaxHeight()) {
            segments.forEachIndexed { i, (_, frac) ->
                if (frac > 0f) {
                    Box(Modifier.fillMaxHeight().weight(frac).background(colors.getOrElse(i) { CatOther }))
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
private fun InsightCard(forecast: com.smartbudget.domain.model.Forecast, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Расходы идут быстрее плана", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Прогноз превышения: ${formatMoney(forecast.projectedOver)}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MainGoalWidget(goal: GoalView, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(CatSavings.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text("${goal.progress.toInt()}%", color = CatSavings, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Цель", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(goal.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text("${formatMoney(goal.currentAmount)} из ${formatMoney(goal.targetAmount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QuickActions(onBudgetEdit: () -> Unit, onGoal: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionTile("Изменить бюджет", "✎", Modifier.weight(1f), onBudgetEdit)
        QuickActionTile("Цель", "◎", Modifier.weight(1f), onGoal)
    }
}

@Composable
private fun QuickActionTile(label: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
        shadowElevation = 1.dp
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

@Composable
private fun CategoryLegend(items: List<BudgetItem>) {
    Surface(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        // Сетка 2 колонки: цветная точка + имя категории
        Column(Modifier.padding(12.dp)) {
            items.chunked(2).forEach { row ->
                Row(Modifier.padding(vertical = 3.dp)) {
                    row.forEach { item ->
                        val color = categoryColor(item.categoryName)
                        Row(
                            Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(10.dp).clip(CircleShape).background(color)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                item.categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // заполнитель, если в ряду 1 элемент
                    if (row.size == 1) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
