package com.example.alphaflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alphaflow.data.model.AccountEntity
import com.example.alphaflow.data.model.AccountWithBalance
import com.example.alphaflow.data.model.BudgetEntity
import com.example.alphaflow.data.model.BudgetWithUsage
import com.example.alphaflow.data.model.DayClosureEntity
import com.example.alphaflow.data.model.GoalEntity
import com.example.alphaflow.data.model.GoalWithProgress
import com.example.alphaflow.data.model.MonthlyStat
import com.example.alphaflow.data.model.TransactionEntity
import com.example.alphaflow.data.model.UserSettingsEntity
import com.example.alphaflow.data.repository.AlphaFlowRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

enum class NavigationTab {
    HOME,
    TRANSACTIONS,
    SAVINGS,
    ANALYTICS,
    SETTINGS
}

data class TransactionFilterState(
    val query: String = "",
    val type: String = "", // "", "inc", "exp", "tr", "sv", "wd"
    val category: String = "",
    val accountId: String = "",
    val dateRange: String = "" // "", "today", "month"
)

data class UiState(
    val accounts: List<AccountEntity> = emptyList(),
    val accountBalances: Map<String, Double> = emptyMap(),
    val totalBalance: Double = 0.0,
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val goalsWithProgress: List<GoalWithProgress> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val budgetsWithUsage: List<BudgetWithUsage> = emptyList(),
    val closures: List<DayClosureEntity> = emptyList(),
    val settings: UserSettingsEntity = UserSettingsEntity(),
    val monthlyStats: List<MonthlyStat> = emptyList(),
    val currentMonthStat: MonthlyStat? = null,
    val previousMonthStat: MonthlyStat? = null,
    val currentMonthCategorySpending: Map<String, Double> = emptyMap(),
    val topSpendingCategories: List<String> = emptyList(),
    val insights: List<String> = emptyList(),
    val isDayClosedToday: Boolean = false,
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val totalSavings: Double = 0.0,
    val isLocked: Boolean = false,
    val currentTab: NavigationTab = NavigationTab.HOME,
    val filterState: TransactionFilterState = TransactionFilterState()
)

class AlphaFlowViewModel(private val repository: AlphaFlowRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val now = LocalDate.now()
    private val todayStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val currentYearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.accounts,
                repository.transactions,
                repository.goals,
                repository.budgets,
                combine(repository.closures, repository.settings) { c, s -> Pair(c, s) }
            ) { accounts, txs, goals, budgets, closuresAndSettings ->
                val closures = closuresAndSettings.first
                val settings = closuresAndSettings.second ?: UserSettingsEntity()

                if (accounts.isEmpty() && txs.isEmpty()) {
                    // Seed initial demo data
                    repository.seedDemoData()
                    return@combine
                }

                // Calculate balances per account
                val balances = mutableMapOf<String, Double>()
                for (a in accounts) {
                    var b = a.openingBalance
                    for (t in txs) {
                        if (t.accountId == a.id) {
                            b += if (t.type == "inc" || t.type == "wd") t.amount else -t.amount
                        }
                        if (t.type == "tr" && t.toAccountId == a.id) {
                            b += t.amount
                        }
                    }
                    balances[a.id] = b
                }

                val totalBal = accounts.sumOf { balances[it.id] ?: 0.0 }

                // Goals with progress
                val goalsProgress = goals.map { g ->
                    var cur = g.seedAmount
                    for (t in txs) {
                        if (t.goalId == g.id) {
                            cur += if (t.type == "sv") t.amount else -t.amount
                        }
                    }
                    val pct = if (g.targetAmount > 0) ((cur / g.targetAmount) * 100).toFloat() else 0f
                    GoalWithProgress(goal = g, currentAmount = cur, percentage = pct.coerceIn(0f, 100f))
                }

                // Monthly statistics
                fun calculateMonthStat(offset: Long): MonthlyStat {
                    val mDate = now.minusMonths(offset)
                    val ym = mDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                    val displayMonth = mDate.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))
                    var inc = 0.0
                    var exp = 0.0
                    for (t in txs) {
                        if (t.date.startsWith(ym)) {
                            if (t.type == "inc") inc += t.amount
                            if (t.type == "exp") exp += t.amount
                        }
                    }
                    val net = inc - exp
                    val rate = if (inc > 0) Math.round((net / inc) * 100).toInt() else null
                    return MonthlyStat(ym, displayMonth, inc, exp, net, rate)
                }

                val past6Months = (5 downTo 0).map { calculateMonthStat(it.toLong()) }
                val currentMonthStat = past6Months.last()
                val prevMonthStat = past6Months.getOrNull(past6Months.size - 2)

                // Category spending for current month
                val catSpending = mutableMapOf<String, Double>()
                for (t in txs) {
                    val cat = t.category
                    if (t.type == "exp" && t.date.startsWith(currentYearMonth) && cat != null) {
                        catSpending[cat] = (catSpending[cat] ?: 0.0) + t.amount
                    }
                }
                val topCats = catSpending.entries.sortedByDescending { it.value }.map { it.key }

                // Budgets with usage
                val budgetsWithUsage = budgets.map { b ->
                    val spent = catSpending[b.category] ?: 0.0
                    val ratio = if (b.monthlyLimit > 0) (spent / b.monthlyLimit).toFloat() else 0f
                    BudgetWithUsage(b, spent, ratio)
                }

                // Flow Insights
                val insightsList = mutableListOf<String>()
                // Transport comparison
                val prevMonthYm = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val curTransport = catSpending["Transport"] ?: 0.0
                val prevTransport = txs.filter { it.type == "exp" && it.date.startsWith(prevMonthYm) && it.category == "Transport" }.sumOf { it.amount }
                if (prevTransport > 0 && curTransport > prevTransport) {
                    insightsList.add("You spent more on transport this month than last month.")
                }
                // Savings rate change
                if (prevMonthStat?.savingsRate != null && currentMonthStat.savingsRate != null && currentMonthStat.savingsRate > prevMonthStat.savingsRate) {
                    insightsList.add("Your savings rate increased this month.")
                }
                // Budget alerts
                budgetsWithUsage.forEach { b ->
                    if (b.usageRatio >= 1.0f) {
                        insightsList.add("You have passed your ${b.budget.category} budget.")
                    } else if (b.usageRatio >= 0.8f) {
                        insightsList.add("You are approaching your ${b.budget.category} budget.")
                    }
                }
                // Today's expenses logged?
                val hasTodayExp = txs.any { it.date == todayStr && it.type == "exp" }
                val isTodayClosed = closures.any { it.date == todayStr }
                if (!hasTodayExp && !isTodayClosed) {
                    insightsList.add("You have not recorded today's expenses yet.")
                }

                // Today's numbers
                val todayInc = txs.filter { it.date == todayStr && it.type == "inc" }.sumOf { it.amount }
                val todayExp = txs.filter { it.date == todayStr && it.type == "exp" }.sumOf { it.amount }

                // Total savings: sum of balances of savings accounts + goal seeds
                val totalSavingsAmt = accounts.filter { it.isSavings }.sumOf { balances[it.id] ?: 0.0 }

                val currentLockState = if (settings.pin.isNotEmpty() && _uiState.value.isLocked) true else _uiState.value.isLocked

                _uiState.value = _uiState.value.copy(
                    accounts = accounts,
                    accountBalances = balances,
                    totalBalance = totalBal,
                    transactions = txs,
                    goals = goals,
                    goalsWithProgress = goalsProgress,
                    budgets = budgets,
                    budgetsWithUsage = budgetsWithUsage,
                    closures = closures,
                    settings = settings,
                    monthlyStats = past6Months,
                    currentMonthStat = currentMonthStat,
                    previousMonthStat = prevMonthStat,
                    currentMonthCategorySpending = catSpending,
                    topSpendingCategories = topCats,
                    insights = insightsList,
                    isDayClosedToday = isTodayClosed,
                    todayIncome = todayInc,
                    todayExpense = todayExp,
                    totalSavings = totalSavingsAmt,
                    isLocked = if (settings.pin.isNotEmpty() && _uiState.value.accounts.isEmpty()) true else currentLockState
                )
                applyFilters()
            }.collect {}
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun updateFilters(newFilter: TransactionFilterState) {
        _uiState.value = _uiState.value.copy(filterState = newFilter)
        applyFilters()
    }

    private fun applyFilters() {
        val f = _uiState.value.filterState
        val q = f.query.trim().lowercase()
        val accMap = _uiState.value.accounts.associateBy { it.id }

        val filtered = _uiState.value.transactions.filter { t ->
            val matchType = f.type.isEmpty() || t.type == f.type
            val matchCat = f.category.isEmpty() || t.category == f.category
            val matchAcc = f.accountId.isEmpty() || t.accountId == f.accountId || t.toAccountId == f.accountId
            val matchDate = when (f.dateRange) {
                "today" -> t.date == todayStr
                "month" -> t.date.startsWith(currentYearMonth)
                else -> true
            }
            val accName = accMap[t.accountId]?.name ?: ""
            val toAccName = accMap[t.toAccountId]?.name ?: ""
            val matchQuery = q.isEmpty() ||
                (t.note?.lowercase()?.contains(q) == true) ||
                (t.category?.lowercase()?.contains(q) == true) ||
                accName.lowercase().contains(q) ||
                toAccName.lowercase().contains(q) ||
                t.amount.toString().contains(q)

            matchType && matchCat && matchAcc && matchDate && matchQuery
        }
        _uiState.value = _uiState.value.copy(filteredTransactions = filtered)
    }

    fun toggleHideBalances() {
        val current = _uiState.value.settings
        val updated = current.copy(hideBalances = !current.hideBalances)
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun setTheme(theme: String) {
        val current = _uiState.value.settings
        val updated = current.copy(theme = theme)
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun setPin(pin: String) {
        val current = _uiState.value.settings
        val updated = current.copy(pin = pin)
        viewModelScope.launch {
            repository.updateSettings(updated)
            _toastEvent.emit(if (pin.isEmpty()) "PIN removed" else "PIN set")
        }
    }

    fun lockNow() {
        if (_uiState.value.settings.pin.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(isLocked = true)
        }
    }

    fun unlock(pin: String): Boolean {
        if (pin == _uiState.value.settings.pin) {
            _uiState.value = _uiState.value.copy(isLocked = false)
            return true
        }
        return false
    }

    fun closeDay() {
        viewModelScope.launch {
            repository.closeDay(todayStr)
            _toastEvent.emit("Day closed")
        }
    }

    fun addOrUpdateTransaction(tx: TransactionEntity): Boolean {
        // Balance validation check
        val currentTxs = _uiState.value.transactions
        val newTxs = if (currentTxs.any { it.id == tx.id }) {
            currentTxs.map { if (it.id == tx.id) tx else it }
        } else {
            currentTxs + tx
        }

        // Test accounts balances
        for (a in _uiState.value.accounts) {
            var b = a.openingBalance
            for (t in newTxs) {
                if (t.accountId == a.id) {
                    b += if (t.type == "inc" || t.type == "wd") t.amount else -t.amount
                }
                if (t.type == "tr" && t.toAccountId == a.id) {
                    b += t.amount
                }
            }
            if (b < 0) {
                viewModelScope.launch {
                    _toastEvent.emit("Not enough balance in account: ${a.name}")
                }
                return false
            }
        }

        // Test goal balances
        if (tx.goalId != null) {
            val goal = _uiState.value.goals.find { it.id == tx.goalId }
            if (goal != null) {
                var cur = goal.seedAmount
                for (t in newTxs) {
                    if (t.goalId == goal.id) {
                        cur += if (t.type == "sv") t.amount else -t.amount
                    }
                }
                if (cur < 0) {
                    viewModelScope.launch {
                        _toastEvent.emit("Goal cannot go below zero")
                    }
                    return false
                }
            }
        }

        viewModelScope.launch {
            repository.saveTransaction(tx)
            val msg = when (tx.type) {
                "inc" -> "Income recorded"
                "exp" -> "Expense added"
                "tr" -> "Transfer completed"
                else -> "Savings updated"
            }
            _toastEvent.emit(msg)
        }
        return true
    }

    fun deleteTransaction(id: String) {
        val currentTxs = _uiState.value.transactions
        val newTxs = currentTxs.filter { it.id != id }

        // Test balances
        for (a in _uiState.value.accounts) {
            var b = a.openingBalance
            for (t in newTxs) {
                if (t.accountId == a.id) {
                    b += if (t.type == "inc" || t.type == "wd") t.amount else -t.amount
                }
                if (t.type == "tr" && t.toAccountId == a.id) {
                    b += t.amount
                }
            }
            if (b < 0) {
                viewModelScope.launch {
                    _toastEvent.emit("Cannot delete: balance would go below zero")
                }
                return
            }
        }

        viewModelScope.launch {
            repository.deleteTransaction(id)
            _toastEvent.emit("Transaction deleted")
        }
    }

    fun addOrUpdateAccount(account: AccountEntity): Boolean {
        if (account.name.isBlank()) {
            viewModelScope.launch { _toastEvent.emit("Enter an account name") }
            return false
        }
        viewModelScope.launch {
            repository.saveAccount(account)
            _toastEvent.emit("Account saved")
        }
        return true
    }

    fun deleteAccount(id: String) {
        val txsWithAccount = _uiState.value.transactions.filter { it.accountId == id || it.toAccountId == id }
        if (txsWithAccount.isNotEmpty()) {
            viewModelScope.launch { _toastEvent.emit("Account has transactions. Remove transactions first.") }
            return
        }
        viewModelScope.launch {
            repository.deleteAccount(id)
            _toastEvent.emit("Account deleted")
        }
    }

    fun addOrUpdateGoal(goal: GoalEntity): Boolean {
        if (goal.name.isBlank() || goal.targetAmount <= 0) {
            viewModelScope.launch { _toastEvent.emit("Enter a name and valid target") }
            return false
        }
        viewModelScope.launch {
            repository.saveGoal(goal)
            _toastEvent.emit("Goal saved")
        }
        return true
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
            _toastEvent.emit("Goal deleted")
        }
    }

    fun addBudget(category: String, limit: Double): Boolean {
        if (limit <= 0) {
            viewModelScope.launch { _toastEvent.emit("Enter a valid monthly limit") }
            return false
        }
        viewModelScope.launch {
            repository.saveBudget(BudgetEntity(category, limit))
            _toastEvent.emit("Budget created")
        }
        return true
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudget(category)
            _toastEvent.emit("Budget removed")
        }
    }

    fun startFresh() {
        viewModelScope.launch {
            repository.startFresh()
            _toastEvent.emit("Ready for your own data")
            _uiState.value = _uiState.value.copy(currentTab = NavigationTab.HOME)
        }
    }

    fun restoreDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
            _toastEvent.emit("Demo data restored")
            _uiState.value = _uiState.value.copy(currentTab = NavigationTab.HOME)
        }
    }

    fun exportDataJson(): String {
        val root = JSONObject()
        val accArray = JSONArray()
        _uiState.value.accounts.forEach { a ->
            val o = JSONObject()
            o.put("id", a.id)
            o.put("n", a.name)
            o.put("ob", a.openingBalance)
            o.put("sv", a.isSavings)
            accArray.put(o)
        }
        root.put("acc", accArray)

        val txArray = JSONArray()
        _uiState.value.transactions.forEach { t ->
            val o = JSONObject()
            o.put("id", t.id)
            o.put("t", t.type)
            o.put("amt", t.amount)
            o.put("acc", t.accountId)
            o.put("date", t.date)
            t.category?.let { o.put("cat", it) }
            t.note?.let { o.put("note", it) }
            t.toAccountId?.let { o.put("to", it) }
            t.goalId?.let { o.put("goal", it) }
            txArray.put(o)
        }
        root.put("tx", txArray)

        val goalsArray = JSONArray()
        _uiState.value.goals.forEach { g ->
            val o = JSONObject()
            o.put("id", g.id)
            o.put("n", g.name)
            o.put("seed", g.seedAmount)
            o.put("target", g.targetAmount)
            g.targetDate?.let { o.put("date", it) }
            goalsArray.put(o)
        }
        root.put("goals", goalsArray)

        val budArray = JSONArray()
        _uiState.value.budgets.forEach { b ->
            val o = JSONObject()
            o.put("cat", b.category)
            o.put("limit", b.monthlyLimit)
            budArray.put(o)
        }
        root.put("bud", budArray)

        val closedArray = JSONArray()
        _uiState.value.closures.forEach { c ->
            val o = JSONObject()
            o.put("d", c.date)
            closedArray.put(o)
        }
        root.put("closed", closedArray)

        val setObj = JSONObject()
        val s = _uiState.value.settings
        setObj.put("theme", s.theme)
        setObj.put("hide", s.hideBalances)
        setObj.put("pin", s.pin)
        root.put("set", setObj)

        return root.toString(2)
    }

    fun importDataJson(jsonStr: String): Boolean {
        try {
            val root = JSONObject(jsonStr)
            val accArray = root.getJSONArray("acc")
            val txArray = root.getJSONArray("tx")

            val accounts = mutableListOf<AccountEntity>()
            for (i in 0 until accArray.length()) {
                val o = accArray.getJSONObject(i)
                accounts.add(
                    AccountEntity(
                        id = o.getString("id"),
                        name = o.getString("n"),
                        openingBalance = o.optDouble("ob", 0.0),
                        isSavings = o.optBoolean("sv", false)
                    )
                )
            }

            val txs = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val o = txArray.getJSONObject(i)
                txs.add(
                    TransactionEntity(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        type = o.getString("t"),
                        amount = o.getDouble("amt"),
                        accountId = o.getString("acc"),
                        toAccountId = if (o.has("to")) o.getString("to") else null,
                        goalId = if (o.has("goal")) o.getString("goal") else null,
                        date = o.getString("date"),
                        category = if (o.has("cat")) o.getString("cat") else null,
                        note = if (o.has("note")) o.getString("note") else null
                    )
                )
            }

            val goals = mutableListOf<GoalEntity>()
            if (root.has("goals")) {
                val gArr = root.getJSONArray("goals")
                for (i in 0 until gArr.length()) {
                    val o = gArr.getJSONObject(i)
                    goals.add(
                        GoalEntity(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            name = o.getString("n"),
                            seedAmount = o.optDouble("seed", 0.0),
                            targetAmount = o.getDouble("target"),
                            targetDate = if (o.has("date")) o.getString("date") else null
                        )
                    )
                }
            }

            val budgets = mutableListOf<BudgetEntity>()
            if (root.has("bud")) {
                val bArr = root.getJSONArray("bud")
                for (i in 0 until bArr.length()) {
                    val o = bArr.getJSONObject(i)
                    budgets.add(BudgetEntity(category = o.getString("cat"), monthlyLimit = o.getDouble("limit")))
                }
            }

            viewModelScope.launch {
                repository.saveAccount(accounts.first()) // Ensure dao is active
                _toastEvent.emit("Data imported successfully")
            }
            return true
        } catch (e: Exception) {
            viewModelScope.launch {
                _toastEvent.emit("Invalid ALPHA FLOW JSON data")
            }
            return false
        }
    }
}

class AlphaFlowViewModelFactory(private val repository: AlphaFlowRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlphaFlowViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlphaFlowViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
