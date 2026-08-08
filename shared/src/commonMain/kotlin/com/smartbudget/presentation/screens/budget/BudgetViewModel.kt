package com.smartbudget.presentation.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.core.network.ApiException
import com.smartbudget.data.repository.BudgetRepository
import com.smartbudget.data.repository.TransactionRepository
import com.smartbudget.domain.model.BudgetOverview
import com.smartbudget.domain.model.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BudgetState>(BudgetState.Loading)
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    private val _syncResult = MutableStateFlow<SyncResult?>(null)
    val syncResult: StateFlow<SyncResult?> = _syncResult.asStateFlow()

    init { loadBudget() }

    fun loadBudget() {
        viewModelScope.launch {
            _state.value = BudgetState.Loading
            try {
                val overview = budgetRepository.overview()
                _state.value = BudgetState.Success(overview)
            } catch (e: Exception) {
                _state.value = BudgetState.Error(e.message ?: "Не удалось загрузить бюджет")
            }
        }
    }

    /** Импорт транзакций из банка. */
    fun syncBank(count: Int = 5) {
        viewModelScope.launch {
            try {
                val result = transactionRepository.generateBank(count)
                _syncResult.value = result
                loadBudget() // обновляем экран после синхронизации
            } catch (e: Exception) {
                _state.value = BudgetState.Error(e.message ?: "Ошибка синхронизации")
            }
        }
    }
}

sealed interface BudgetState {
    data object Loading : BudgetState
    data class Success(val overview: BudgetOverview) : BudgetState
    data class Error(val message: String) : BudgetState
}
