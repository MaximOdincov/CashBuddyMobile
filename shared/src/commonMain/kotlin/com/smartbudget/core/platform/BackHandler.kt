package com.smartbudget.core.platform

import androidx.compose.runtime.Composable

/**
 * Обработка системной кнопки «Назад».
 * Android — перехват через OnBackPressedCallback.
 * iOS — no-op (физической кнопки нет, навигация через UI-стрелки).
 */
@Composable
expect fun AppBackHandler(enabled: Boolean, onBack: () -> Unit)
