package com.smartbudget.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartbudget.data.repository.AiRepository
import com.smartbudget.domain.model.PerformedAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    init {
        // грузим историю с сервера
        viewModelScope.launch {
            try {
                val history = aiRepository.history()
                _messages.value = history.map {
                    UiMessage(
                        text = it.content,
                        isUser = it.role == "user",
                        actions = emptyList()
                    )
                }
            } catch (_: Exception) { /* игнорируем — начнём с пустого чата */ }
        }
    }

    fun send(text: String) {
        if (text.isBlank() || _isSending.value) return

        // мгновенно показываем сообщение пользователя
        _messages.value = _messages.value + UiMessage(text = text, isUser = true, actions = emptyList())
        _isSending.value = true

        viewModelScope.launch {
            try {
                val reply = aiRepository.chat(text)
                _messages.value = _messages.value + UiMessage(
                    text = reply.reply,
                    isUser = false,
                    actions = reply.actions
                )
            } catch (e: Exception) {
                _messages.value = _messages.value + UiMessage(
                    text = "⚠️ Не удалось получить ответ. Проверьте подключение к серверу.",
                    isUser = false,
                    actions = emptyList()
                )
            } finally {
                _isSending.value = false
            }
        }
    }
}

data class UiMessage(
    val text: String,
    val isUser: Boolean,
    val actions: List<PerformedAction>
)
