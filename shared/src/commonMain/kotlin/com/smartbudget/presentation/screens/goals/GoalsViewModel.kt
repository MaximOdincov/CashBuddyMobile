package com.smartbudget.presentation.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.data.repository.GoalsRepository
import com.smartbudget.domain.model.GoalView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(
    private val goalsRepository: GoalsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<GoalsState>(GoalsState.Loading)
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            // НЕ сбрасываем в Loading (fix мигания)
            try {
                _state.value = GoalsState.Success(goalsRepository.list())
            } catch (e: Exception) {
                _state.value = GoalsState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun contribute(goalId: Long, amount: Double) {
        viewModelScope.launch {
            try {
                goalsRepository.contribute(goalId, amount)
                load()
            } catch (_: Exception) { }
        }
    }

    fun create(title: String, targetAmount: Double) {
        viewModelScope.launch {
            try {
                goalsRepository.create(title, targetAmount)
                load()
            } catch (_: Exception) { }
        }
    }
}

sealed interface GoalsState {
    data object Loading : GoalsState
    data class Success(val goals: List<GoalView>) : GoalsState
    data class Error(val message: String) : GoalsState
}
