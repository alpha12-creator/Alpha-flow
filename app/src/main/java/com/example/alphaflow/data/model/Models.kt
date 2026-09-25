package com.example.alphaflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val openingBalance: Double,
    val isSavings: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String, // "inc", "exp", "tr", "sv", "wd"
    val amount: Double,
    val accountId: String,
    val toAccountId: String? = null,
    val goalId: String? = null,
    val date: String, // "YYYY-MM-DD"
    val category: String? = null,
    val note: String? = null
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val seedAmount: Double,
    val targetAmount: Double,
    val targetDate: String? = null
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val monthlyLimit: Double
)

@Entity(tableName = "day_closures")
data class DayClosureEntity(
    @PrimaryKey val date: String
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val theme: String = "system", // "system", "light", "dark"
    val hideBalances: Boolean = false,
    val pin: String = "",
    val isDemo: Boolean = true
)

object Categories {
    val EXPENSE = listOf(
        "Food", "Transport", "Education", "Housing",
        "Airtime & Data", "Health", "Shopping", "Entertainment",
        "Bills", "Other"
    )

    val INCOME = listOf(
        "Salary", "Business", "Freelance", "Allowance", "Gift", "Other"
    )
}

data class MonthlyStat(
    val yearMonth: String, // "YYYY-MM"
    val displayMonth: String, // e.g. "Sep"
    val income: Double,
    val expense: Double,
    val net: Double,
    val savingsRate: Int? // percentage or null
)

data class AccountWithBalance(
    val account: AccountEntity,
    val currentBalance: Double
)

data class GoalWithProgress(
    val goal: GoalEntity,
    val currentAmount: Double,
    val percentage: Float
)

data class BudgetWithUsage(
    val budget: BudgetEntity,
    val currentSpent: Double,
    val usageRatio: Float
)
