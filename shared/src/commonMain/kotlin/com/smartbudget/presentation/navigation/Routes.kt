package com.smartbudget.presentation.navigation

/** Все маршруты приложения. */
object Routes {
    // Главные (bottom nav)
    const val BUDGET = "budget"
    const val CHAT = "chat"
    const val MORE = "more"

    // Доп. экраны
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
    val icon: String // эмодзи-иконка (без зависимости от material-icons-extended)
)

val bottomTabs = listOf(
    BottomTab(Routes.BUDGET, "Бюджет", "📊"),
    BottomTab(Routes.CHAT, "Чат", "🤖"),
    BottomTab(Routes.MORE, "Ещё", "☰")
)
