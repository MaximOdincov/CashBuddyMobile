package com.smartbudget.presentation.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.data.repository.BudgetRepository
import com.smartbudget.data.repository.GoalsRepository
import com.smartbudget.data.repository.InsightsRepository
import com.smartbudget.data.repository.NotificationsRepository
import com.smartbudget.data.repository.TransactionRepository
import com.smartbudget.domain.model.BudgetOverview
import com.smartbudget.domain.model.Forecast
import com.smartbudget.domain.model.GoalView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val insightsRepository: InsightsRepository,
    private val goalsRepository: GoalsRepository,
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BudgetState>(BudgetState.Loading)
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    private val _forecast = MutableStateFlow<Forecast?>(null)
    val forecast: StateFlow<Forecast?> = _forecast.asStateFlow()

    private val _mainGoal = MutableStateFlow<GoalView?>(null)
    val mainGoal: StateFlow<GoalView?> = _mainGoal.asStateFlow()

    private val _currentMonth = MutableStateFlow(currentMonthStr())
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    init { load() }

    fun load(month: String? = null) {
        val target = month ?: _currentMonth.value
        _currentMonth.value = target
        viewModelScope.launch {
            // НЕ сбрасываем в Loading при повторной загрузке (fix мигания)
            try {
                val overview = budgetRepository.overview(target)
                _state.value = BudgetState.Success(overview)
            } catch (e: Exception) {
                _state.value = BudgetState.Error(e.message ?: "Не удалось загрузить бюджет")
            }
        }
        viewModelScope.launch {
            try { _forecast.value = insightsRepository.forecast() } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                _mainGoal.value = goalsRepository.list()
                    .maxByOrNull { it.progress }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try { _unreadCount.value = notificationsRepository.unreadCount()["count"] ?: 0L } catch (_: Exception) {}
        }
    }

    /** Перейти на предыдущий/следующий месяц. */
    fun shiftMonth(delta: Int) {
        _currentMonth.value = shiftMonthStr(_currentMonth.value, delta)
        load(_currentMonth.value)
    }

    private fun currentMonthStr(): String {
        // YYYY-MM текущий
        return try {
            val now = com.smartbudget.core.platform.currentHourOfDay()
            // используем системное время через NSDate/java.time на платформе — но для даты проще:
            shiftMonthStr(platformToday(), 0)
        } catch (_: Exception) {
            "2026-08"
        }
    }

    private fun shiftMonthStr(ym: String, delta: Int): String {
        val parts = ym.split("-")
        if (parts.size != 2) return ym
        var year = parts[0].toIntOrNull() ?: return ym
        var month = parts[1].toIntOrNull() ?: return ym
        month += delta
        while (month < 1) { month += 12; year -= 1 }
        while (month > 12) { month -= 12; year += 1 }
        val monthStr = if (month < 10) "0$month" else "$month"
        return "$year-$monthStr"
    }

    private fun platformToday(): String = com.smartbudget.core.platform.todayYearMonth()
}

sealed interface BudgetState {
    data object Loading : BudgetState
    data class Success(val overview: BudgetOverview) : BudgetState
    data class Error(val message: String) : BudgetState
}
