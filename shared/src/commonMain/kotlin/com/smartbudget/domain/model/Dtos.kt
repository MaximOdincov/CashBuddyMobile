package com.smartbudget.domain.model

import kotlinx.serialization.Serializable

// ============ Auth ============

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class ShareCodeRequest(val shareCode: String)

@Serializable
data class AuthResponse(
    val userId: Long,
    val accessToken: String,
    val shareCode: String
)

@Serializable
data class MeResponse(
    val userId: Long,
    val username: String,
    val monthlyIncome: Double,
    val currency: String,
    val shareCode: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String
)

// ============ Budget ============

@Serializable
data class BudgetOverview(
    val monthlyIncome: Double,
    val currency: String,
    val periodMonth: String,
    val totalPercent: Double,
    val allocations: List<BudgetItem>
)

@Serializable
data class BudgetItem(
    val categoryId: Long,
    val categoryName: String,
    val icon: String,
    val color: String,
    val isSavings: Boolean,
    val percent: Double,
    val limitAmount: Double,
    val spent: Double,
    val remaining: Double,
    val pct: Double
)

@Serializable
data class CategoryView(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String,
    val isSavings: Boolean
)

@Serializable
data class IncomeRequest(val monthlyIncome: Double)

@Serializable
data class UpdateAllocationsRequest(val allocations: List<AllocationItem>)

@Serializable
data class AllocationItem(
    val categoryId: Long,
    val percent: Double
)

// ============ Transactions ============

@Serializable
data class TransactionDto(
    val id: Long? = null,
    val amount: Double = 0.0,
    val merchant: String = "",
    val mcc: String? = null,
    val categoryName: String? = null,
    val description: String? = null,
    val source: String? = null,
    val timestamp: String? = null
)

@Serializable
data class AddTransactionRequest(
    val amount: Double,
    val merchant: String,
    val mcc: String? = null,
    val description: String? = null
)

@Serializable
data class CategorySpendRow(
    val categoryId: Long? = null,
    val categoryName: String,
    val icon: String,
    val color: String,
    val spent: Double,
    val count: Long
)

// ============ Bank ============

@Serializable
data class SyncResult(
    val generated: Int,
    val imported: Int
)

// ============ Goals ============

@Serializable
data class GoalView(
    val id: Long,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val progress: Double,
    val targetDate: String? = null,
    val linkedCategoryId: Long? = null
)

@Serializable
data class CreateGoalRequest(
    val title: String,
    val targetAmount: Double,
    val targetDate: String? = null,
    val linkedCategoryId: Long? = null
)

@Serializable
data class ContributeRequest(val amount: Double)

// ============ Insights ============

@Serializable
data class Forecast(
    val spentSoFar: Double,
    val dailyAvg: Double,
    val projectedSpent: Double,
    val monthlyIncome: Double,
    val projectedOver: Double,
    val daysLeft: Long
)

@Serializable
data class BreakdownSlice(
    val categoryId: Long? = null,
    val label: String,
    val amount: Double
)

@Serializable
data class TrendPoint(
    val date: String,
    val amount: Double
)

// ============ Notifications ============

@Serializable
data class NotificationDto(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val severity: String,
    val isRead: Boolean,
    val createdAt: String
)

// ============ AI ============

@Serializable
data class ChatRequest(val message: String)

@Serializable
data class ChatReply(
    val reply: String,
    val actions: List<PerformedAction> = emptyList()
)

@Serializable
data class PerformedAction(
    val type: String,
    val summary: String,
    val applied: Boolean
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String,
    val createdAt: String? = null
)

@Serializable
data class ApiError(
    val error: String? = null,
    val message: String? = null,
    val status: Int? = null
)
