package com.smartbudget.data.repository

import com.smartbudget.data.remote.ApiClient
import com.smartbudget.domain.model.*

class AuthRepository(api: ApiClient) : ApiClient(api.httpClient, api.appSettings) {
    suspend fun login(username: String, password: String): AuthResponse =
        post("/api/auth/login", LoginRequest(username, password))

    suspend fun register(username: String, password: String): AuthResponse =
        post("/api/auth/register", RegisterRequest(username, password))

    suspend fun loginByCode(shareCode: String): AuthResponse =
        post("/api/auth/login-by-code", ShareCodeRequest(shareCode))

    suspend fun me(): MeResponse = get("/api/auth/me")
}

class BudgetRepository(api: ApiClient) : ApiClient(api.httpClient, api.appSettings) {
    suspend fun overview(month: String? = null): BudgetOverview =
        get("/api/budget", mapOf("month" to month))

    suspend fun categories(): List<CategoryView> = get("/api/budget/categories")

    suspend fun setIncome(income: Double) {
        putUnit("/api/budget/income", IncomeRequest(income))
    }

    suspend fun updateAllocations(items: List<AllocationItem>) {
        putUnit("/api/budget/allocations", UpdateAllocationsRequest(items))
    }
}

class TransactionRepository(api: ApiClient) : ApiClient(api.httpClient, api.appSettings) {
    suspend fun list(
        from: String? = null,
        to: String? = null,
        categoryId: Long? = null,
        limit: Int = 100
    ): List<TransactionDto> = get(
        "/api/transactions",
        mapOf("from" to from, "to" to to, "categoryId" to categoryId?.toString(), "limit" to limit.toString())
    )

    suspend fun summary(month: String? = null): List<CategorySpendRow> =
        get("/api/transactions/summary", mapOf("month" to month))

    suspend fun add(amount: Double, merchant: String, mcc: String? = null, description: String? = null): TransactionDto =
        post("/api/transactions", AddTransactionRequest(amount, merchant, mcc, description))

    suspend fun generateBank(count: Int = 5, hoursBack: Long = 24): SyncResult =
        postWithParams("/api/bank/generate", mapOf("count" to count.toString(), "hoursBack" to hoursBack.toString()))
}

class GoalsRepository(api: ApiClient) : ApiClient(api.httpClient, api.appSettings) {
    suspend fun list(): List<GoalView> = get("/api/goals")

    suspend fun create(
        title: String,
        targetAmount: Double,
        targetDate: String? = null,
        linkedCategoryId: Long? = null
    ): GoalView = post("/api/goals", CreateGoalRequest(title, targetAmount, targetDate, linkedCategoryId))

    suspend fun contribute(goalId: Long, amount: Double): GoalView =
        post("/api/goals/$goalId/contribute", ContributeRequest(amount))
}

class AiRepository(api: ApiClient) : ApiClient(api.httpClient, api.appSettings) {
    suspend fun chat(message: String): ChatReply =
        post("/api/ai/chat", ChatRequest(message))

    suspend fun history(): List<ChatMessageDto> = get("/api/ai/chat/history")

    suspend fun generateAdvice() {
        postEmpty<Unit>("/api/ai/advice/generate")
    }

    suspend fun advice(unreadOnly: Boolean = false): List<ChatMessageDto> =
        get("/api/ai/advice", mapOf("unreadOnly" to unreadOnly.toString()))
}

class NotificationsRepository(api: ApiClient) : ApiClient(api.httpClient, api.appSettings) {
    suspend fun list(unreadOnly: Boolean = false): List<NotificationDto> =
        get("/api/notifications", mapOf("unreadOnly" to unreadOnly.toString()))

    suspend fun unreadCount(): Map<String, Long> = get("/api/notifications/unread-count")

    suspend fun markRead(id: Long) {
        postEmpty<Unit>("/api/notifications/$id/read")
    }
}

class InsightsRepository(api: ApiClient) : ApiClient(api.httpClient, api.appSettings) {
    suspend fun forecast(): Forecast = get("/api/insights/forecast")

    suspend fun breakdown(month: String? = null): List<BreakdownSlice> =
        get("/api/insights/breakdown", mapOf("month" to month))

    suspend fun trend(days: Int = 30): List<TrendPoint> =
        get("/api/insights/trend", mapOf("days" to days.toString()))
}
