package com.smartbudget.core.platform

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS: no-op. Навигация назад — через UI-стрелки.
}
