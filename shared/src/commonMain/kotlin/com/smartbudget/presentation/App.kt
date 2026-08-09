package com.smartbudget.presentation

import androidx.compose.foundation.layout.padding
import com.smartbudget.core.platform.AppBackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.smartbudget.core.storage.AppSettings
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
    data class CategoryDetail(val categoryId: Long, val categoryName: String = "") : Screen()
}

/** Корневые вкладки (для per-tab истории). */
private val Screen.tab: Screen get() = when (this) {
    is Screen.Budget, is Screen.Goals, is Screen.Notifications,
    is Screen.Transactions, is Screen.Settings, is Screen.AddTransaction,
    is Screen.CategoryDetail -> Screen.Budget
    is Screen.Chat -> Screen.Chat
    is Screen.More -> Screen.More
}

@Composable
fun App() {
    val appSettings: AppSettings = koinInject()
    var themeMode by remember { mutableStateOf(appSettings.themeMode) }
    var authed by remember { mutableStateOf(appSettings.isLoggedIn) }
    // refresh-триггер: сбрасывает ViewModels после AI-действий / навигации
    var refreshTrigger by remember { mutableStateOf(0) }

    // Per-tab backStack: каждая вкладка помнит свою историю.
    var tabStacks by remember { mutableStateOf(mapOf<Screen, List<Screen>>(
        Screen.Budget to listOf(Screen.Budget),
        Screen.Chat to listOf(Screen.Chat),
        Screen.More to listOf(Screen.More)
    )) }
    var currentTab by remember { mutableStateOf<Screen>(Screen.Budget) }
    val current: Screen = tabStacks.getValue(currentTab).last()

    fun navigate(screen: Screen) {
        val tab = screen.tab
        val stack = tabStacks[tab] ?: listOf(tab)
        tabStacks = tabStacks + (tab to (stack + screen))
        currentTab = tab
    }
    fun pop(): Boolean {
        val stack = tabStacks[currentTab] ?: return false
        if (stack.size <= 1) return false
        tabStacks = tabStacks + (currentTab to stack.dropLast(1))
        return true
    }
    fun switchTab(tab: Screen) { currentTab = tab }

    CashBuddyTheme(themeMode = themeMode) {
        val isMainScreen = current in listOf(Screen.Budget, Screen.Chat, Screen.More)

        // Системная кнопка «Назад»: на вторичных экранах → pop, на главных → выход
        AppBackHandler(enabled = !isMainScreen || tabStacks[currentTab]!!.size > 1) { pop() }

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
                            val selected = currentTab == tabScreen
                            NavigationBarItem(
                                selected = selected,
                                onClick = { switchTab(tabScreen) },
                                icon = {
                                    com.smartbudget.presentation.components.BottomTabIcon(
                                        route = tab.route, selected = selected
                                    )
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            if (!authed) {
                LoginScreen(onLoggedIn = {
                    authed = true
                    currentTab = Screen.Budget
                    tabStacks = mapOf(
                        Screen.Budget to listOf(Screen.Budget),
                        Screen.Chat to listOf(Screen.Chat),
                        Screen.More to listOf(Screen.More)
                    )
                })
            } else {
                androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
                    when (val s = current) {
                        Screen.Budget -> BudgetScreen(
                            key = refreshTrigger,
                            onCategoryClick = { id, name -> navigate(Screen.CategoryDetail(id, name)) },
                            onAddTransaction = { navigate(Screen.AddTransaction) },
                            onOpenGoals = { navigate(Screen.Goals) },
                            onOpenChat = { switchTab(Screen.Chat) },
                            onOpenBudgetEdit = { /* пока здесь же */ }
                        )
                        Screen.Chat -> ChatScreen(
                            onNavigateToBudget = {
                                refreshTrigger++ // обновить бюджет при возврате
                                switchTab(Screen.Budget)
                            },
                            onNavigateToGoals = {
                                refreshTrigger++
                                navigate(Screen.Goals)
                            }
                        )
                        Screen.More -> MoreScreen(
                            onGoals = { navigate(Screen.Goals) },
                            onNotifications = { navigate(Screen.Notifications) },
                            onTransactions = { navigate(Screen.Transactions) },
                            onSettings = { navigate(Screen.Settings) },
                            onLogout = { authed = false }
                        )
                        Screen.Goals -> GoalsScreen(onBack = { pop() })
                        Screen.Notifications -> NotificationsScreen(onBack = { pop() })
                        Screen.Transactions -> TransactionsScreen(onBack = { pop() })
                        Screen.Settings -> SettingsScreen(onBack = { pop() }, onThemeChanged = { themeMode = appSettings.themeMode })
                        Screen.AddTransaction -> AddTransactionScreen(onDone = { pop(); refreshTrigger++ })
                        is Screen.CategoryDetail -> CategoryDetailScreen(
                            categoryId = s.categoryId,
                            categoryName = s.categoryName,
                            onBack = { pop() }
                        )
                    }
                }
            }
        }
    }
}
