package com.example.alphaflow.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.R
import com.example.alphaflow.data.model.AccountEntity
import com.example.alphaflow.data.model.GoalEntity
import com.example.alphaflow.data.model.TransactionEntity
import com.example.alphaflow.ui.components.BrandHeader
import com.example.alphaflow.ui.components.EmptyStateView
import com.example.alphaflow.ui.components.FlowCard
import com.example.alphaflow.ui.components.FlowWaterfallCard
import com.example.alphaflow.ui.components.MonthlyIncomeExpenseChart
import com.example.alphaflow.ui.components.SectionHeader
import com.example.alphaflow.ui.components.formatCurrency
import com.example.alphaflow.ui.theme.AlphaTheme
import com.example.alphaflow.ui.viewmodel.NavigationTab
import com.example.alphaflow.ui.viewmodel.UiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: UiState,
    onNavigateTab: (NavigationTab) -> Unit,
    onToggleHideBalances: () -> Unit,
    onQuickAdd: (String) -> Unit, // "inc", "exp", "tr"
    onOpenAddTransaction: () -> Unit,
    onAddAccount: () -> Unit,
    onEditAccount: (AccountEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onStartFresh: () -> Unit,
    onCloseDay: () -> Unit,
    onReviewTransactions: () -> Unit
) {
    val scrollState = rememberScrollState()
    val now = LocalTime.now()
    val today = LocalDate.now()
    val hour = now.hour

    val (greeting, greetingEmoji, greetingMessage) = when {
        hour in 5..11 -> Triple(
            "Good morning",
            "☀️",
            "Start your day with clarity. Keep your flow positive and on track."
        )
        hour in 12..16 -> Triple(
            "Good afternoon",
            "🌤️",
            "Mid-day check-in. Monitor your daily safe-to-spend allowance."
        )
        hour in 17..21 -> Triple(
            "Good evening",
            "🌆",
            "Review your day. Log any afternoon transactions or dinner expenses."
        )
        else -> Triple(
            "Good night",
            "🌙",
            "Rest easy. Your finances and savings goals are safely tracked."
        )
    }

    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH))

    val currentStat = uiState.currentMonthStat
    val inc = currentStat?.income ?: 0.0
    val exp = currentStat?.expense ?: 0.0
    val saved = currentStat?.net ?: 0.0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Brand & Header
            BrandHeader(size = 28.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Greeting and Top Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting, Alpha $greetingEmoji",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlphaTheme.colors.textPrimary
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AlphaTheme.colors.accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = greetingMessage,
                        fontSize = 12.sp,
                        color = AlphaTheme.colors.textMuted,
                        lineHeight = 16.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onToggleHideBalances,
                        modifier = Modifier.testTag("toggle_balance_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.settings.hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.settings.hideBalances) "Show balances" else "Hide balances",
                            tint = AlphaTheme.colors.textPrimary
                        )
                    }
                    IconButton(
                        onClick = { onNavigateTab(NavigationTab.SETTINGS) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = AlphaTheme.colors.textPrimary
                        )
                    }
                }
            }

            // Demo Data Notice
            if (uiState.settings.isDemo) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "You are viewing demo data.",
                            fontSize = 12.sp,
                            color = AlphaTheme.colors.textMuted
                        )
                        Button(
                            onClick = onStartFresh,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlphaTheme.colors.accent,
                                contentColor = AlphaTheme.colors.background
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("start_fresh_button")
                        ) {
                            Text("Start fresh", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Banner Balance Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AlphaTheme.colors.cardBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_balance_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Finance Overview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient scrim overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x990B132B),
                                        Color(0xF00B132B)
                                    )
                                )
                            )
                    )
                    // Content over hero banner
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "TOTAL FLOW BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3DD1AE),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrency(uiState.totalBalance, uiState.settings.hideBalances),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.testTag("total_balance_text")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Column Summary Cards (Income, Expenses, Saved)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Income
                FlowCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Income", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "+" + formatCurrency(inc, uiState.settings.hideBalances),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlphaTheme.colors.accent
                        )
                    }
                }

                // Expenses
                FlowCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Expenses", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "−" + formatCurrency(exp, uiState.settings.hideBalances),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlphaTheme.colors.expense
                        )
                    }
                }

                // Saved
                FlowCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text("Saved", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            formatCurrency(saved, uiState.settings.hideBalances),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlphaTheme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Action Buttons - Easy Access
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onQuickAdd("inc") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_income_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.accent,
                        contentColor = AlphaTheme.colors.background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("+ Income", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onQuickAdd("exp") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_expense_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.expense.copy(alpha = 0.15f),
                        contentColor = AlphaTheme.colors.expense
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("− Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onQuickAdd("tr") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_transfer_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.surfaceVariant,
                        contentColor = AlphaTheme.colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("⇄ Transfer", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onReviewTransactions,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_search_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.surfaceVariant,
                        contentColor = AlphaTheme.colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("🔍 Search", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Recent Transactions List Component
            SectionHeader(
                title = "Recent Transactions",
                actionText = if (uiState.transactions.isNotEmpty()) "See all (${uiState.transactions.size})" else null,
                onActionClick = onReviewTransactions
            )

            RecentTransactionsList(
                transactions = uiState.transactions,
                accounts = uiState.accounts,
                goals = uiState.goals,
                hideBalances = uiState.settings.hideBalances,
                onEditTransaction = onEditTransaction,
                onDeleteTransaction = onDeleteTransaction,
                onViewAllClick = onReviewTransactions,
                onAddTransactionClick = onOpenAddTransaction
            )

            // Waterfall Section: Your Flow
            SectionHeader(title = "Your Flow")
            FlowWaterfallCard(
                income = inc,
                expenses = exp,
                saved = saved,
                remainingBalance = uiState.totalBalance,
                hideBalances = uiState.settings.hideBalances
            )

            // Income vs Expenses Chart
            SectionHeader(title = "Income vs expenses")
            FlowCard {
                MonthlyIncomeExpenseChart(stats = uiState.monthlyStats)
            }

            // Accounts
            SectionHeader(
                title = "Accounts",
                actionText = "+ Add account",
                onActionClick = onAddAccount
            )
            FlowCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.accounts.forEach { acc ->
                        val bal = uiState.accountBalances[acc.id] ?: 0.0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = acc.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = AlphaTheme.colors.textPrimary
                                )
                                if (acc.isSavings) {
                                    Text(
                                        text = "Savings Account",
                                        fontSize = 11.sp,
                                        color = AlphaTheme.colors.accent
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = formatCurrency(bal, uiState.settings.hideBalances),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AlphaTheme.colors.textPrimary
                                )
                                IconButton(
                                    onClick = { onEditAccount(acc) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit ${acc.name}",
                                        tint = AlphaTheme.colors.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Flow Insights
            SectionHeader(title = "Flow insights")
            FlowCard {
                if (uiState.insights.isEmpty()) {
                    Text(
                        text = "Nothing to flag. Insights appear as you record more activity.",
                        fontSize = 12.sp,
                        color = AlphaTheme.colors.textMuted
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.insights.forEach { tip ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("• ", color = AlphaTheme.colors.accent, fontWeight = FontWeight.Bold)
                                Text(
                                    text = tip,
                                    fontSize = 13.sp,
                                    color = AlphaTheme.colors.textPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Flow Health
            SectionHeader(title = "Flow health")
            FlowCard {
                val totalBudget = uiState.budgets.sumOf { it.monthlyLimit }
                val usedBudget = uiState.budgetsWithUsage.sumOf { it.currentSpent }
                val budgetPct = if (totalBudget > 0) Math.round((usedBudget / totalBudget) * 100) else null
                val expRatio = if (inc > 0) Math.round((exp / inc) * 100) else null

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Savings rate", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                            Text(
                                text = if (currentStat?.savingsRate != null) "${currentStat.savingsRate}%" else "—",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AlphaTheme.colors.textPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Expense ratio", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                            Text(
                                text = if (expRatio != null) "$expRatio%" else "—",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AlphaTheme.colors.textPrimary
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Budget usage", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                            Text(
                                text = if (budgetPct != null) "$budgetPct%" else "—",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AlphaTheme.colors.textPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Monthly net flow", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                            Text(
                                text = formatCurrency(saved, uiState.settings.hideBalances),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (saved >= 0) AlphaTheme.colors.accent else AlphaTheme.colors.expense
                            )
                        }
                    }
                }
            }

            // Close your day
            SectionHeader(title = "Close your day")
            FlowCard {
                val net = uiState.todayIncome - uiState.todayExpense
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Income", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                            Text(
                                text = "+" + formatCurrency(uiState.todayIncome, uiState.settings.hideBalances),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AlphaTheme.colors.accent
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Expenses", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                            Text(
                                text = "−" + formatCurrency(uiState.todayExpense, uiState.settings.hideBalances),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AlphaTheme.colors.expense
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Net", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                            val netSign = if (net >= 0) "+" else ""
                            Text(
                                text = netSign + formatCurrency(net, uiState.settings.hideBalances),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AlphaTheme.colors.textPrimary
                            )
                        }
                    }

                    if (uiState.isDayClosedToday) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Day closed",
                                tint = AlphaTheme.colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Day Closed",
                                fontWeight = FontWeight.Bold,
                                color = AlphaTheme.colors.accent,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Did you record everything you spent today?",
                            fontSize = 13.sp,
                            color = AlphaTheme.colors.textPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReviewTransactions,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Review transactions", fontSize = 12.sp)
                            }
                            Button(
                                onClick = onCloseDay,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AlphaTheme.colors.accent,
                                    contentColor = AlphaTheme.colors.background
                                )
                            ) {
                                Text("Close day", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (uiState.closures.isNotEmpty()) {
                        val prevDates = uiState.closures.take(5).joinToString(", ") {
                            if (it.date.length >= 10) it.date.substring(5) else it.date
                        }
                        Text(
                            text = "Previous: $prevDates",
                            fontSize = 11.sp,
                            color = AlphaTheme.colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }

        // Floating Action Button to add new transactions
        FloatingActionButton(
            onClick = onOpenAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("add_transaction_fab"),
            containerColor = AlphaTheme.colors.accent,
            contentColor = AlphaTheme.colors.background,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Add",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RecentTransactionsList(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    goals: List<GoalEntity>,
    hideBalances: Boolean,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onViewAllClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("all") }
    val accMap = remember(accounts) { accounts.associateBy { it.id } }
    val goalMap = remember(goals) { goals.associateBy { it.id } }

    val filteredList = remember(transactions, selectedFilter) {
        val list = when (selectedFilter) {
            "inc" -> transactions.filter { it.type == "inc" }
            "exp" -> transactions.filter { it.type == "exp" }
            "tr" -> transactions.filter { it.type == "tr" }
            else -> transactions
        }
        list.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })
    }
    val recentTxs = remember(filteredList) {
        filteredList.take(5)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Access Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "all" to "All",
                "inc" to "Income",
                "exp" to "Expenses",
                "tr" to "Transfers"
            ).forEach { (typeKey, label) ->
                val isSelected = selectedFilter == typeKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = typeKey },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AlphaTheme.colors.accent,
                        selectedLabelColor = AlphaTheme.colors.background,
                        containerColor = AlphaTheme.colors.surfaceVariant,
                        labelColor = AlphaTheme.colors.textPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_chip_$typeKey")
                )
            }
        }

        if (recentTxs.isEmpty()) {
            FlowCard {
                EmptyStateView(
                    title = if (selectedFilter == "all") "No recent transactions" else "No $selectedFilter transactions found",
                    message = "Track your money by recording your first transaction.",
                    actionButton = {
                        Button(
                            onClick = onAddTransactionClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlphaTheme.colors.accent,
                                contentColor = AlphaTheme.colors.background
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Add Transaction", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        } else {
            recentTxs.forEach { tx ->
                RecentTransactionCard(
                    tx = tx,
                    accMap = accMap,
                    goalMap = goalMap,
                    hideBalances = hideBalances,
                    onEdit = { onEditTransaction(tx) },
                    onDelete = { onDeleteTransaction(tx) }
                )
            }

            if (filteredList.size > 5) {
                OutlinedButton(
                    onClick = onViewAllClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("view_all_transactions_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AlphaTheme.colors.accent
                    )
                ) {
                    Text(
                        text = "View all ${filteredList.size} transactions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTransactionCard(
    tx: TransactionEntity,
    accMap: Map<String, AccountEntity>,
    goalMap: Map<String, GoalEntity>,
    hideBalances: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fromAccName = accMap[tx.accountId]?.name ?: "—"
    val toAccName = accMap[tx.toAccountId]?.name ?: "—"
    val goalName = goalMap[tx.goalId]?.name ?: "Goal"

    val title = when (tx.type) {
        "tr" -> "$fromAccName → $toAccName"
        "sv" -> "To $goalName"
        "wd" -> "From $goalName"
        else -> tx.note ?: tx.category ?: "Transaction"
    }

    val typeLabel = when (tx.type) {
        "inc" -> "Income"
        "exp" -> "Expense"
        "tr" -> "Transfer"
        "sv" -> "Savings"
        "wd" -> "Withdrawal"
        else -> tx.type
    }

    val categoryOrKind = tx.category ?: typeLabel
    val relativeDate = formatRelativeDate(tx.date)
    val subtitle = "$categoryOrKind · $fromAccName · $relativeDate"

    val isPositive = tx.type == "inc"
    val isNegative = tx.type == "exp"
    val amountColor = when {
        isPositive -> AlphaTheme.colors.accent
        isNegative -> AlphaTheme.colors.expense
        else -> AlphaTheme.colors.textPrimary
    }
    val sign = when {
        isPositive -> "+"
        isNegative -> "−"
        else -> ""
    }

    val iconText = when (tx.type) {
        "tr" -> "⇄"
        "sv", "wd" -> "◔"
        else -> (tx.category ?: "T").take(1).uppercase()
    }

    FlowCard(
        modifier = Modifier
            .clickable(onClick = onEdit)
            .testTag("recent_transaction_card_${tx.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isPositive -> AlphaTheme.colors.accent.copy(alpha = 0.15f)
                            isNegative -> AlphaTheme.colors.expense.copy(alpha = 0.15f)
                            else -> AlphaTheme.colors.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = when {
                        isPositive -> AlphaTheme.colors.accent
                        isNegative -> AlphaTheme.colors.expense
                        else -> AlphaTheme.colors.textPrimary
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AlphaTheme.colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = AlphaTheme.colors.textMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount & Actions
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = sign + formatCurrency(tx.amount, hideBalances),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = amountColor
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit transaction",
                            tint = AlphaTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete transaction",
                            tint = AlphaTheme.colors.expense,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatRelativeDate(dateStr: String): String {
    return try {
        val today = LocalDate.now()
        val txDate = LocalDate.parse(dateStr)
        when {
            txDate == today -> "Today"
            txDate == today.minusDays(1) -> "Yesterday"
            txDate.year == today.year -> txDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH))
            else -> txDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
        }
    } catch (e: Exception) {
        dateStr
    }
}

