package com.smartbudget.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.smartbudget.core.storage.AppSettings
import com.smartbudget.presentation.navigation.BottomTab
import com.smartbudget.presentation.navigation.bottomTabs
import com.smartbudget.presentation.screens.auth.LoginScreen
import com.smartbudget.presentation.screens.budget.BudgetScreen
import com.smartbudget.presentation.screens.category.CategoryDetailScreen
import com.smartbudget.presentation.screens.chat.ChatScreen
import com.smartbudget.presentation.screens.goals.GoalsScreen
import com.smartbudget.presentation.screens.more.MoreScreen
import com.smartbudget.presentation.screens.notifications.NotificationsScreen
import com.smartbudget.presentation.screens.settings.SettingsScreen
import com.smartbudget.presentation.screens.transactions.AddTransactionScreen
import com.smartbudget.presentation.screens.transactions.TransactionsScreen
import com.smartbudget.presentation.theme.CashBuddyTheme
import org.koin.compose.koinInject

/**
 * Простые экраны приложения (state-based навигация).
 * Заменяет navigation-compose, которая вызывает klib-ошибки на Kotlin 2.4.10.
 */
sealed class Screen {
    data object Budget : Screen()
    data object Chat : Screen()
    data object More : Screen()
    data object Goals : Screen()
    data object Notifications : Screen()
    data object Transactions : Screen()
    data object Settings : Screen()
    data object AddTransaction : Screen()
    data class CategoryDetail(val categoryId: Long) : Screen()
}

@Composable
fun App() {
    val appSettings: AppSettings = koinInject()
    var themeMode by remember { mutableStateOf(appSettings.themeMode) }
    var authed by remember { mutableStateOf(appSettings.isLoggedIn) }

    // Стек навигации: последний элемент = текущий экран
    var backStack by remember { mutableStateOf<List<Screen>>(listOf(Screen.Budget)) }
    val current: Screen = backStack.last()
    fun navigate(screen: Screen) { backStack = backStack + screen }
    fun pop() { if (backStack.size > 1) backStack = backStack.dropLast(1) }
    fun switchTab(tab: Screen) { backStack = listOf(tab) }

    CashBuddyTheme(themeMode = themeMode) {
        val isMainScreen = current in listOf(Screen.Budget, Screen.Chat, Screen.More)

        Scaffold(
            bottomBar = {
                if (isMainScreen && authed) {
                    NavigationBar {
                        bottomTabs.forEach { tab ->
                            val tabScreen = when (tab.route) {
                                "budget" -> Screen.Budget
                                "chat" -> Screen.Chat
                                else -> Screen.More
                            }
                            val selected = current == tabScreen
                            NavigationBarItem(
                                selected = selected,
                                onClick = { switchTab(tabScreen) },
                                icon = { Text(tab.icon, style = MaterialTheme.typography.titleMedium) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            if (!authed) {
                LoginScreen(onLoggedIn = { authed = true; backStack = listOf(Screen.Budget) })
            } else {
                androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
                    when (val s = current) {
                        Screen.Budget -> BudgetScreen(
                            onCategoryClick = { id -> navigate(Screen.CategoryDetail(id)) },
                            onAddTransaction = { navigate(Screen.AddTransaction) }
                        )
                        Screen.Chat -> ChatScreen()
                        Screen.More -> MoreScreen(
                            onGoals = { navigate(Screen.Goals) },
                            onNotifications = { navigate(Screen.Notifications) },
                            onTransactions = { navigate(Screen.Transactions) },
                            onSettings = { navigate(Screen.Settings) },
                            onLogout = { authed = false }
                        )
                        Screen.Goals -> GoalsScreen()
                        Screen.Notifications -> NotificationsScreen()
                        Screen.Transactions -> TransactionsScreen()
                        Screen.Settings -> SettingsScreen(onThemeChanged = { themeMode = appSettings.themeMode })
                        Screen.AddTransaction -> AddTransactionScreen(onDone = { pop() })
                        is Screen.CategoryDetail -> CategoryDetailScreen(categoryId = s.categoryId)
                    }
                }
            }
        }
    }
}
