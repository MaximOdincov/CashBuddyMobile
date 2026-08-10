package com.smartbudget.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.smartbudget.core.platform.AppBackHandler
import com.smartbudget.core.storage.AppSettings
import com.smartbudget.presentation.navigation.bottomTabs
import com.smartbudget.presentation.components.BottomTabIcon
import com.smartbudget.presentation.screens.auth.LoginScreen
import com.smartbudget.presentation.screens.budget.BudgetEditScreen
import com.smartbudget.presentation.screens.budget.BudgetScreen
import com.smartbudget.presentation.screens.category.CategoryDetailScreen
import com.smartbudget.presentation.screens.chat.ChatScreen
import com.smartbudget.presentation.screens.goals.GoalsScreen
import com.smartbudget.presentation.screens.notifications.NotificationsScreen
import com.smartbudget.presentation.screens.settings.SettingsScreen
import com.smartbudget.presentation.screens.transactions.AddTransactionScreen
import com.smartbudget.presentation.screens.transactions.TransactionsScreen
import com.smartbudget.presentation.theme.CashBuddyTheme
import org.koin.compose.koinInject

sealed class Screen {
    data object Budget : Screen()
    data object Chat : Screen()
    data object TransactionsTab : Screen()
    data object Goals : Screen()
    data object Notifications : Screen()
    data object Settings : Screen()
    data object AddTransaction : Screen()
    data object BudgetEdit : Screen()
    data class CategoryDetail(val categoryId: Long, val categoryName: String = "") : Screen()
}

/** Корневые вкладки (для per-tab истории). */
private val Screen.tab: Screen get() = when (this) {
    is Screen.Budget, is Screen.Goals, is Screen.Notifications,
    is Screen.Settings, is Screen.AddTransaction,
    is Screen.CategoryDetail, is Screen.BudgetEdit -> Screen.Budget
    is Screen.Chat -> Screen.Chat
    is Screen.TransactionsTab -> Screen.TransactionsTab
}

@Composable
fun App() {
    val appSettings: AppSettings = koinInject()
    var themeMode by remember { mutableStateOf(appSettings.themeMode) }
    var authed by remember { mutableStateOf(appSettings.isLoggedIn) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Per-tab backStack
    var tabStacks by remember { mutableStateOf(
        mapOf<Screen, List<Screen>>(
            Screen.Budget to listOf(Screen.Budget),
            Screen.Chat to listOf(Screen.Chat),
            Screen.TransactionsTab to listOf(Screen.TransactionsTab)
        )
    )}
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
    fun backOrExit() {
        // на вторичных → pop; на главных вкладках (кроме Бюджет) → переход в Бюджет; на Бюджет → выход
        if (tabStacks[currentTab]!!.size > 1) pop()
        else if (currentTab != Screen.Budget) switchTab(Screen.Budget)
    }

    CashBuddyTheme(themeMode = themeMode) {
        val isMainScreen = current in listOf(Screen.Budget, Screen.Chat, Screen.TransactionsTab)
        AppBackHandler(enabled = true) { backOrExit() }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (isMainScreen && authed) {
                    NavigationBar {
                        bottomTabs.forEach { tab ->
                            val tabScreen = when (tab.route) {
                                "budget" -> Screen.Budget
                                "chat" -> Screen.Chat
                                else -> Screen.TransactionsTab
                            }
                            val selected = currentTab == tabScreen
                            NavigationBarItem(
                                selected = selected,
                                onClick = { switchTab(tabScreen) },
                                icon = { BottomTabIcon(route = tab.route, selected = selected) },
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
                        Screen.TransactionsTab to listOf(Screen.TransactionsTab)
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
                            onOpenBudgetEdit = { navigate(Screen.BudgetEdit) },
                            onNotifications = { navigate(Screen.Notifications) },
                            onSettings = { navigate(Screen.Settings) }
                        )
                        Screen.Chat -> ChatScreen(
                            onNavigateToBudget = { refreshTrigger++; switchTab(Screen.Budget) },
                            onNavigateToGoals = { refreshTrigger++; navigate(Screen.Goals) },
                            onNotifications = { navigate(Screen.Notifications) },
                            onSettings = { navigate(Screen.Settings) }
                        )
                        Screen.TransactionsTab -> TransactionsScreen(
                            asTab = true,
                            key = refreshTrigger,
                            onAddTransaction = { navigate(Screen.AddTransaction) },
                            onNotifications = { navigate(Screen.Notifications) },
                            onSettings = { navigate(Screen.Settings) }
                        )
                        Screen.Goals -> GoalsScreen(onBack = { pop(); refreshTrigger++ })
                        Screen.Notifications -> NotificationsScreen(onBack = { pop() })
                        Screen.Settings -> SettingsScreen(onBack = { pop() }, onThemeChanged = { themeMode = appSettings.themeMode })
                        Screen.AddTransaction -> AddTransactionScreen(onDone = { pop(); refreshTrigger++ })
                        Screen.BudgetEdit -> BudgetEditScreen(onBack = { pop(); refreshTrigger++ })
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
