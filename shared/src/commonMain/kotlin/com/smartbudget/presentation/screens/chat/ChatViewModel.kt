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
                        // Сервер хранит ChatRole.USER (@Enumerated STRING) → Jackson отдаёт "USER"
                        isUser = it.role.equals("user", ignoreCase = true),
                        actions = emptyList()
                    )
                }
            } catch (_: Exception) { /* игнорируем — начнём с пустого чата */ }
        }
    }

    fun send(text: String) {
        if (text.isBlank() || _isSending.value) return

        _messages.value = _messages.value + UiMessage(text = text, isUser = true, actions = emptyList())
        _isSending.value = true

        viewModelScope.launch {
            var lastError: Exception? = null
            // retry до 3 попыток (AI может быть медленным)
            repeat(3) { attempt ->
                try {
                    val reply = aiRepository.chat(text)
                    // успех — заменяем последнее сообщение об ошибке (если было) на ответ
                    val msgs = _messages.value.toMutableList()
                    // убираем предыдущее сообщение об ошибке AI (если оно последнее)
                    if (msgs.isNotEmpty() && !msgs.last().isUser && msgs.last().text.startsWith("⚠️")) {
                        msgs.removeAt(msgs.lastIndex)
                    }
                    msgs.add(UiMessage(text = reply.reply, isUser = false, actions = reply.actions))
                    _messages.value = msgs
                    lastError = null
                    return@repeat
                } catch (e: Exception) {
                    lastError = e
                    if (attempt < 2) kotlinx.coroutines.delay(2000L)
                }
            }
            if (lastError != null) {
                _messages.value = _messages.value + UiMessage(
                    text = "⚠️ Не удалось получить ответ. Попробуйте ещё раз.",
                    isUser = false, actions = emptyList()
                )
            }
            _isSending.value = false
        }
    }
}

data class UiMessage(
    val text: String,
    val isUser: Boolean,
    val actions: List<PerformedAction>
)
