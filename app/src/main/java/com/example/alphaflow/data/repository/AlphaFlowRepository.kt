package com.example.alphaflow.data.repository

import com.example.alphaflow.data.local.AlphaFlowDao
import com.example.alphaflow.data.model.AccountEntity
import com.example.alphaflow.data.model.BudgetEntity
import com.example.alphaflow.data.model.DayClosureEntity
import com.example.alphaflow.data.model.GoalEntity
import com.example.alphaflow.data.model.TransactionEntity
import com.example.alphaflow.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class AlphaFlowRepository(private val dao: AlphaFlowDao) {

    val accounts: Flow<List<AccountEntity>> = dao.getAllAccounts()
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val goals: Flow<List<GoalEntity>> = dao.getAllGoals()
    val budgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
    val closures: Flow<List<DayClosureEntity>> = dao.getAllClosures()
    val settings: Flow<UserSettingsEntity?> = dao.getSettings()

    suspend fun saveAccount(account: AccountEntity) {
        dao.insertAccount(account)
    }

    suspend fun deleteAccount(id: String) {
        dao.deleteAccountById(id)
    }

    suspend fun saveTransaction(tx: TransactionEntity) {
        dao.insertTransaction(tx)
    }

    suspend fun deleteTransaction(id: String) {
        dao.deleteTransactionById(id)
    }

    suspend fun saveGoal(goal: GoalEntity) {
        dao.insertGoal(goal)
    }

    suspend fun deleteGoal(id: String) {
        dao.deleteTransactionsByGoalId(id)
        dao.deleteGoalById(id)
    }

    suspend fun saveBudget(budget: BudgetEntity) {
        dao.insertBudget(budget)
    }

    suspend fun deleteBudget(category: String) {
        dao.deleteBudgetByCategory(category)
    }

    suspend fun closeDay(date: String) {
        dao.insertClosure(DayClosureEntity(date))
    }

    suspend fun updateSettings(settings: UserSettingsEntity) {
        dao.insertSettings(settings)
    }

    suspend fun seedDemoData() {
        val now = LocalDate.now()
        val currentYearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val todayStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE)

        fun ym(offsetMonths: Long): String {
            val d = now.minusMonths(offsetMonths)
            return d.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        }

        fun dd(day: Int): String {
            val d = minOf(day, now.dayOfMonth)
            return String.format("%s-%02d", currentYearMonth, d)
        }

        val initialTargets = listOf(
            Triple("a1", "Cash", 80000.0),
            Triple("a2", "Bank", 250000.0),
            Triple("a3", "Mobile Money", 55000.0),
            Triple("a4", "Savings", 300000.0)
        )

        val txList = mutableListOf<TransactionEntity>()

        fun addTx(type: String, amt: Double, acc: String, date: String, cat: String? = null, note: String? = null, to: String? = null, goal: String? = null) {
            txList.add(
                TransactionEntity(
                    id = UUID.randomUUID().toString().substring(0, 8),
                    type = type,
                    amount = amt,
                    accountId = acc,
                    toAccountId = to,
                    goalId = goal,
                    date = date,
                    category = cat,
                    note = note
                )
            )
        }

        addTx("inc", 250000.0, "a2", dd(1), "Allowance", "Monthly allowance")
        addTx("inc", 200000.0, "a3", dd(10), "Business", "CV design clients")
        addTx("tr", 100000.0, "a2", dd(2), null, "Save first", to = "a4")

        val expenses = listOf(
            Triple(30000.0, "a1", Triple(3, "Food", "Groceries")),
            Triple(25000.0, "a3", Triple(9, "Food", "Lunch & dinner")),
            Triple(30000.0, "a1", Triple(17, "Food", "Market")),
            Triple(15000.0, "a3", Triple(4, "Transport", "Bus fare")),
            Triple(20000.0, "a3", Triple(12, "Transport", "Bajaj rides")),
            Triple(10000.0, "a1", Triple(20, "Transport", "Daladala")),
            Triple(20000.0, "a3", Triple(14, "Entertainment", "Cinema")),
            Triple(60000.0, "a2", Triple(5, "Education", "Course materials")),
            Triple(15000.0, "a3", Triple(6, "Airtime & Data", "Bundle")),
            Triple(25000.0, "a2", Triple(8, "Bills", "Electricity")),
            Triple(15000.0, "a1", Triple(15, "Shopping", "Clothes"))
        )

        for ((amt, acc, details) in expenses) {
            val (day, cat, note) = details
            addTx("exp", amt, acc, dd(day), cat, note)
        }

        // Previous 4 months data
        val factors = listOf(1.0, 0.9, 1.1, 0.95)
        for (i in 1..4) {
            val m = ym(i.toLong())
            val k = factors[i - 1]
            addTx("inc", 250000.0, "a2", "$m-01", "Allowance", "Monthly allowance")
            val bizAmt = Math.round(150000.0 * k / 1000.0) * 1000.0
            addTx("inc", bizAmt, "a3", "$m-12", "Business", "CV design clients")

            val pastExp = listOf(
                Pair("Food", 90000.0 * k),
                Pair("Transport", 40000.0 * k),
                Pair("Education", 70000.0),
                Pair("Bills", 30000.0)
            )
            for (p in pastExp) {
                val pastAmt = Math.round(p.second / 1000.0) * 1000.0
                val day = 5 + (i * 3) % 15
                addTx("exp", pastAmt, "a2", String.format("%s-%02d", m, day), p.first, p.first)
            }
        }

        // Calculate opening balances so balance equals target
        val accounts = initialTargets.map { target ->
            val id = target.first
            val name = target.second
            val targetBal = target.third

            var netFlow = 0.0
            for (t in txList) {
                if (t.accountId == id) {
                    netFlow += if (t.type == "inc" || t.type == "wd") t.amount else -t.amount
                }
                if (t.type == "tr" && t.toAccountId == id) {
                    netFlow += t.amount
                }
            }
            val openingBal = targetBal - netFlow
            AccountEntity(id = id, name = name, openingBalance = openingBal, isSavings = id == "a4")
        }

        fun nx(months: Long): String {
            return now.plusMonths(months).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }

        val goals = listOf(
            GoalEntity("g1", "Laptop Fund", seedAmount = 350000.0, targetAmount = 1000000.0, targetDate = nx(8)),
            GoalEntity("g2", "Emergency Fund", seedAmount = 100000.0, targetAmount = 200000.0, targetDate = nx(4)),
            GoalEntity("g3", "College Fund", seedAmount = 150000.0, targetAmount = 400000.0, targetDate = nx(12))
        )

        val budgets = listOf(
            BudgetEntity("Food", 100000.0),
            BudgetEntity("Transport", 60000.0),
            BudgetEntity("Entertainment", 30000.0)
        )

        // Clear and save
        dao.deleteAllTransactions()
        dao.deleteAllAccounts()
        dao.deleteAllGoals()
        dao.deleteAllBudgets()
        dao.deleteAllClosures()

        dao.insertAccounts(accounts)
        dao.insertTransactions(txList)
        dao.insertGoals(goals)
        dao.insertBudgets(budgets)
        dao.insertSettings(UserSettingsEntity(id = 1, theme = "system", hideBalances = false, pin = "", isDemo = true))
    }

    suspend fun startFresh() {
        dao.deleteAllTransactions()
        dao.deleteAllGoals()
        dao.deleteAllBudgets()
        dao.deleteAllClosures()

        // Reset all accounts opening balance to 0
        val currentAccounts = listOf(
            AccountEntity("a1", "Cash", 0.0, false),
            AccountEntity("a2", "Bank", 0.0, false),
            AccountEntity("a3", "Mobile Money", 0.0, false),
            AccountEntity("a4", "Savings", 0.0, true)
        )
        dao.insertAccounts(currentAccounts)
        dao.insertSettings(UserSettingsEntity(id = 1, theme = "system", hideBalances = false, pin = "", isDemo = false))
    }
}
