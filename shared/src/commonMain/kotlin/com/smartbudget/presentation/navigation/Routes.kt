package com.smartbudget.presentation.navigation

/** Все маршруты приложения. */
object Routes {
    const val BUDGET = "budget"
    const val CHAT = "chat"
    const val TRANSACTIONS_TAB = "transactions_tab"   // третья вкладка
    const val LOGIN = "login"
    const val CATEGORY_DETAIL = "category/{categoryId}"
    const val TRANSACTIONS = "transactions"
    const val ADD_TX = "add_transaction"
    const val GOALS = "goals"
    const val GOAL_DETAIL = "goal/{goalId}"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
    const val BUDGET_EDIT = "budget_edit"

    fun categoryDetail(categoryId: Long) = "category/$categoryId"
    fun goalDetail(goalId: Long) = "goal/$goalId"
}

/** Описание вкладки нижней навигации. */
data class BottomTab(
    val route: String,
    val label: String,
    val icon: String
)

val bottomTabs = listOf(
    BottomTab(Routes.BUDGET, "Бюджет", "budget"),
    BottomTab(Routes.CHAT, "Чат", "chat"),
    BottomTab(Routes.TRANSACTIONS_TAB, "Траты", "transactions_tab")
)
