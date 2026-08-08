package com.smartbudget.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.data.repository.NotificationsRepository
import com.smartbudget.domain.model.NotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = NotificationsState.Loading
            try {
                _state.value = NotificationsState.Success(notificationsRepository.list())
                _unreadCount.value = notificationsRepository.unreadCount()["count"] ?: 0L
            } catch (e: Exception) {
                _state.value = NotificationsState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun markRead(id: Long) {
        viewModelScope.launch {
            try { notificationsRepository.markRead(id); load() } catch (_: Exception) { }
        }
    }
}

sealed interface NotificationsState {
    data object Loading : NotificationsState
    data class Success(val items: List<NotificationDto>) : NotificationsState
    data class Error(val message: String) : NotificationsState
}
