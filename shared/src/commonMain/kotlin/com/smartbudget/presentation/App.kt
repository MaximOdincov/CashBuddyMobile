package com.smartbudget.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartbudget.core.storage.AppSettings
import com.smartbudget.presentation.navigation.Routes
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

@Composable
fun App() {
    val appSettings: AppSettings = koinInject()
    var themeMode by remember { mutableStateOf(appSettings.themeMode) }
    var authed by remember { mutableStateOf(appSettings.isLoggedIn) }

    CashBuddyTheme(themeMode = themeMode) {
        val navController = rememberNavController()

        // отслеживаем текущий маршрут для показа BottomBar
        val navBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStack?.destination?.route
        val showBottomBar = currentRoute in bottomTabs.map { it.route }

        Scaffold(
            bottomBar = {
                if (showBottomBar && authed) {
                    NavigationBar {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Text(tab.icon, style = MaterialTheme.typography.titleMedium) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            if (!authed) {
                LoginScreen(onLoggedIn = { authed = true })
            } else {
                NavHost(
                    navController = navController,
                    startDestination = Routes.BUDGET,
                    modifier = androidx.compose.ui.Modifier.padding(padding)
                ) {
                    // --- главные вкладки ---
                    composable(Routes.BUDGET) {
                        BudgetScreen(
                            onCategoryClick = { id -> navController.navigate(Routes.categoryDetail(id)) },
                            onAddTransaction = { navController.navigate(Routes.ADD_TX) }
                        )
                    }
                    composable(Routes.CHAT) { ChatScreen() }
                    composable(Routes.MORE) {
                        MoreScreen(
                            onGoals = { navController.navigate(Routes.GOALS) },
                            onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onTransactions = { navController.navigate(Routes.TRANSACTIONS) },
                            onSettings = { navController.navigate(Routes.SETTINGS) },
                            onLogout = {
                                authed = false
                                navController.navigate(Routes.LOGIN) { popUpTo(0) }
                            }
                        )
                    }

                    // --- доп. экраны ---
                    composable(
                        route = Routes.CATEGORY_DETAIL,
                        arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
                    ) { entry ->
                        // Читаем Long-аргумент через NavType (KMP-совместимо: SavedState API)
                        val categoryId = entry.arguments?.let { args ->
                            NavType.LongType.get(args, "categoryId")
                        } ?: 0L
                        CategoryDetailScreen(categoryId = categoryId)
                    }
                    composable(Routes.TRANSACTIONS) { TransactionsScreen() }
                    composable(Routes.ADD_TX) {
                        AddTransactionScreen(onDone = { navController.popBackStack() })
                    }
                    composable(Routes.GOALS) { GoalsScreen() }
                    composable(Routes.NOTIFICATIONS) { NotificationsScreen() }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(onThemeChanged = { themeMode = appSettings.themeMode })
                    }
                }
            }
        }
    }
}
