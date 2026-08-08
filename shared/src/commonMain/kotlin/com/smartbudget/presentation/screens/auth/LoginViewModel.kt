package com.smartbudget.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.core.storage.AppSettings
import com.smartbudget.data.repository.AuthRepository
import com.smartbudget.domain.model.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val appSettings: AppSettings
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(username: String, password: String) = execute { authRepository.login(username, password) }
    fun register(username: String, password: String) = execute { authRepository.register(username, password) }
    fun loginByCode(shareCode: String) = execute { authRepository.loginByCode(shareCode) }

    private fun execute(block: suspend () -> AuthResponse) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val result = block()
                appSettings.accessToken = result.accessToken
                appSettings.lastShareCode = result.shareCode
                _state.value = LoginState.Success(result)
            } catch (e: Exception) {
                _state.value = LoginState.Error(humanizeError(e))
            }
        }
    }

    private fun humanizeError(e: Throwable): String = when (e) {
        is com.smartbudget.core.network.ApiException -> when (e.statusCode) {
            401 -> "Неверный логин или пароль"
            409 -> "Пользователь с таким именем уже существует"
            else -> "Ошибка сервера (${e.statusCode})"
        }
        is com.smartbudget.core.network.NetworkException -> "Нет соединения с сервером"
        else -> e.message ?: "Неизвестная ошибка"
    }
}

sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data class Success(val data: AuthResponse) : LoginState
    data class Error(val message: String) : LoginState
}
