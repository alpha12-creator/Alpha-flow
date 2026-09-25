package com.example.alphaflow.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.data.model.AccountEntity
import com.example.alphaflow.ui.components.BrandHeader
import com.example.alphaflow.ui.components.FlowCard
import com.example.alphaflow.ui.components.FlowWaterfallCard
import com.example.alphaflow.ui.components.MonthlyIncomeExpenseChart
import com.example.alphaflow.ui.components.SectionHeader
import com.example.alphaflow.ui.components.formatCurrency
import com.example.alphaflow.ui.theme.AlphaTheme
import com.example.alphaflow.ui.viewmodel.NavigationTab
import com.example.alphaflow.ui.viewmodel.UiState
import java.time.LocalTime

@Composable
fun HomeScreen(
    uiState: UiState,
    onNavigateTab: (NavigationTab) -> Unit,
    onToggleHideBalances: () -> Unit,
    onQuickAdd: (String) -> Unit, // "inc", "exp", "tr"
    onAddAccount: () -> Unit,
    onEditAccount: (AccountEntity) -> Unit,
    onStartFresh: () -> Unit,
    onCloseDay: () -> Unit,
    onReviewTransactions: () -> Unit
) {
    val scrollState = rememberScrollState()
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }

    val currentStat = uiState.currentMonthStat
    val inc = currentStat?.income ?: 0.0
    val exp = currentStat?.expense ?: 0.0
    val saved = currentStat?.net ?: 0.0

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
            Column {
                Text(
                    text = "$greeting, Alpha",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlphaTheme.colors.textPrimary
                )
                Text(
                    text = "Control your flow. Build your future.",
                    fontSize = 12.sp,
                    color = AlphaTheme.colors.textMuted
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

        // Total Flow Balance Card
        FlowCard {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TOTAL FLOW BALANCE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AlphaTheme.colors.textMuted,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(uiState.totalBalance, uiState.settings.hideBalances),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlphaTheme.colors.textPrimary,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.testTag("total_balance_text")
                )
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

        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onQuickAdd("inc") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_income_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlphaTheme.colors.accent,
                    contentColor = AlphaTheme.colors.background
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Income", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onQuickAdd("exp") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_expense_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AlphaTheme.colors.textPrimary
                )
            ) {
                Text("− Expense", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onQuickAdd("tr") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_transfer_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AlphaTheme.colors.textPrimary
                )
            ) {
                Text("↗ Transfer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

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

        Spacer(modifier = Modifier.height(80.dp))
    }
}
