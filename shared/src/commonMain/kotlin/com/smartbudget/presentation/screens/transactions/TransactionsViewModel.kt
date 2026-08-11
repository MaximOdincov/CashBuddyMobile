package com.smartbudget.presentation.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.data.repository.TransactionRepository
import com.smartbudget.domain.model.TransactionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<TransactionsState>(TransactionsState.Loading)
    val state: StateFlow<TransactionsState> = _state.asStateFlow()

    // НЕ вызываем load() в init — categoryId задаётся вызывающим экраном.
    // init { load() } грузил бы все транзакции (categoryId=null) и конкурировал с LaunchedEffect.

    fun load(categoryId: Long? = null) {
        viewModelScope.launch {
            // НЕ сбрасываем в Loading при повторной загрузке — данные обновятся без мигания
            try {
                _state.value = TransactionsState.Success(transactionRepository.list(categoryId = categoryId))
            } catch (e: Exception) {
                _state.value = TransactionsState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun add(amount: Double, merchant: String, mcc: String?, description: String?) {
        viewModelScope.launch {
            try {
                transactionRepository.add(amount, merchant, mcc, description)
                load()
            } catch (_: Exception) { }
        }
    }
}

sealed interface TransactionsState {
    data object Loading : TransactionsState
    data class Success(val items: List<TransactionDto>) : TransactionsState
    data class Error(val message: String) : TransactionsState
}
