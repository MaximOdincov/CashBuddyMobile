package com.smartbudget.di

import com.russhwolf.settings.Settings
import com.smartbudget.core.network.createHttpClient
import com.smartbudget.core.storage.AppSettings
import com.smartbudget.data.remote.ApiClient
import com.smartbudget.data.repository.AiRepository
import com.smartbudget.data.repository.AuthRepository
import com.smartbudget.data.repository.BudgetRepository
import com.smartbudget.data.repository.GoalsRepository
import com.smartbudget.data.repository.InsightsRepository
import com.smartbudget.data.repository.NotificationsRepository
import com.smartbudget.data.repository.TransactionRepository
import com.smartbudget.presentation.screens.auth.LoginViewModel
import com.smartbudget.presentation.screens.budget.BudgetEditViewModel
import com.smartbudget.presentation.screens.budget.BudgetViewModel
import com.smartbudget.presentation.screens.chat.ChatViewModel
import com.smartbudget.presentation.screens.goals.GoalsViewModel
import com.smartbudget.presentation.screens.notifications.NotificationsViewModel
import com.smartbudget.presentation.screens.transactions.TransactionsViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Инициализация Koin. Вызывается один раз при старте приложения (Android: MainActivity, iOS: MainViewController).
 */
fun initKoin() {
    startKoin {
        modules(appModule())
    }
}

fun appModule(): Module = module {
    // Storage
    single { Settings() }
    single { AppSettings(get()) }

    // Network
    single { createHttpClient(get()) }
    single { ApiClient(get(), get()) }

    // Repositories
    single { AuthRepository(get()) }
    single { BudgetRepository(get()) }
    single { TransactionRepository(get()) }
    single { GoalsRepository(get()) }
    single { AiRepository(get()) }
    single { NotificationsRepository(get()) }
    single { InsightsRepository(get()) }

    // ViewModels
    factory { LoginViewModel(get(), get()) }
    factory { BudgetViewModel(get(), get(), get(), get(), get()) }
    factory { BudgetEditViewModel(get()) }
    factory { ChatViewModel(get()) }
    factory { GoalsViewModel(get()) }
    factory { NotificationsViewModel(get()) }
    factory { TransactionsViewModel(get()) }
}
