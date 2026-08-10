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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.core.util.formatMoney
import com.smartbudget.data.repository.BudgetRepository
import com.smartbudget.domain.model.AllocationItem
import com.smartbudget.presentation.components.BackArrow
import com.smartbudget.presentation.components.DonutChart
import com.smartbudget.presentation.components.DonutSegment
import com.smartbudget.presentation.theme.categoryColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun BudgetEditScreen(
    onBack: () -> Unit,
    viewModel: BudgetEditViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val editing by viewModel.editing.collectAsState()
    val total = editing.values.sum().toInt()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Распределение бюджета") },
                navigationIcon = { BackArrow(onBack) }
            )
        }
    ) { padding ->
        when (val s = state) {
            is BudgetEditState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is BudgetEditState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
            is BudgetEditState.Success -> {
                val income = s.overview.monthlyIncome
                LazyColumn(
                    Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        val segments = editing.map { (catId, pct) ->
                            val name = s.overview.allocations.firstOrNull { it.categoryId == catId }?.categoryName ?: ""
                            DonutSegment(name, pct.toDouble(), categoryColor(name))
                        }
                        DonutChart(
                            segments = segments,
                            centerLabel = "$total%",
                            centerSub = "из 100%"
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Все 100% дохода (${formatMoney(income)}) распределяются по категориям. " +
                                "Меняйте проценты слайдерами. Накопления — отдельная категория для целей.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = if (total == 100) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Распределено $total% из 100%",
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium,
                                color = if (total == 100) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    item { Spacer(Modifier.height(12.dp)) }
                    items(s.overview.allocations) { item ->
                        val pct = editing[item.categoryId] ?: 0.0f
                        AllocationSliderRow(
                            name = item.categoryName,
                            icon = item.icon,
                            percent = pct,
                            amount = income * pct / 100.0,
                            onValueChange = { viewModel.set(item.categoryId, it) }
                        )
                    }

                    item {
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.save { onBack() } },
                            enabled = total == 100,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Text("Сохранить", fontWeight = FontWeight.Medium) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllocationSliderRow(
    name: String,
    icon: String,
    percent: Float,
    amount: Double,
    onValueChange: (Float) -> Unit
) {
    val color = categoryColor(name)
    Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(name, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                Text(
                    "${percent.toInt()}% · ${formatMoney(amount)}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(4.dp))
            Slider(
                value = percent,
                onValueChange = onValueChange,
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.2f)
                )
            )
        }
    }
}

class BudgetEditViewModel(
    private val budgetRepository: BudgetRepository
) : ViewModel() {
    private val _state = MutableStateFlow<BudgetEditState>(BudgetEditState.Loading)
    val state: StateFlow<BudgetEditState> = _state.asStateFlow()

    private val _editing = MutableStateFlow<Map<Long, Float>>(emptyMap())
    val editing: StateFlow<Map<Long, Float>> = _editing.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = BudgetEditState.Loading
            try {
                val overview = budgetRepository.overview()
                _editing.value = overview.allocations.associate { it.categoryId to it.percent.toFloat() }
                _state.value = BudgetEditState.Success(overview)
            } catch (e: Exception) {
                _state.value = BudgetEditState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun set(categoryId: Long, value: Float) {
        _editing.value = _editing.value + (categoryId to value.coerceIn(0f, 100f))
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                val items = _editing.value.map { (catId, pct) -> AllocationItem(catId, pct.toDouble()) }
                budgetRepository.updateAllocations(items)
                onDone()
            } catch (_: Exception) {}
        }
    }
}

sealed interface BudgetEditState {
    data object Loading : BudgetEditState
    data class Success(val overview: com.smartbudget.domain.model.BudgetOverview) : BudgetEditState
    data class Error(val message: String) : BudgetEditState
}
