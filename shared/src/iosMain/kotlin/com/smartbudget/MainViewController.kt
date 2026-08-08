package com.smartbudget

import androidx.compose.ui.window.ComposeUIViewController
import com.smartbudget.di.initKoin
import com.smartbudget.presentation.App

fun MainViewController() = ComposeUIViewController {
    // Koin инициализируется один раз при старте приложения
    initKoin()
    App()
}
