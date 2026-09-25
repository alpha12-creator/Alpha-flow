package com.example.alphaflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.ui.components.EmptyStateView
import com.example.alphaflow.ui.components.FlowCard
import com.example.alphaflow.ui.components.FlowProgressBar
import com.example.alphaflow.ui.components.MonthlyIncomeExpenseChart
import com.example.alphaflow.ui.components.SectionHeader
import com.example.alphaflow.ui.components.formatCurrency
import com.example.alphaflow.ui.theme.AlphaTheme
import com.example.alphaflow.ui.viewmodel.UiState

@Composable
fun AnalyticsScreen(
    uiState: UiState,
    onAddBudget: () -> Unit,
    onDeleteBudget: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val curStat = uiState.currentMonthStat
    val displayMonth = curStat?.displayMonth ?: ""
    val catSpending = uiState.currentMonthCategorySpending
    val totalExpenseCurrentMonth = catSpending.values.sum()
    val sortedCats = catSpending.entries.sortedByDescending { it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Analytics",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AlphaTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Income vs Expenses Chart Card
        FlowCard {
            Column {
                Text(
                    text = "Income vs expenses",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AlphaTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                MonthlyIncomeExpenseChart(stats = uiState.monthlyStats)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Monthly Flow Breakdown Table
        FlowCard {
            Column {
                Text(
                    text = "Monthly flow",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AlphaTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                uiState.monthlyStats.reversed().forEach { stat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stat.displayMonth,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AlphaTheme.colors.textPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+" + formatCurrency(stat.income, uiState.settings.hideBalances),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AlphaTheme.colors.accent
                            )
                            Text(
                                text = "−" + formatCurrency(stat.expense, uiState.settings.hideBalances),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AlphaTheme.colors.expense
                            )
                            Text(
                                text = if (stat.savingsRate != null) "${stat.savingsRate}%" else "—",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlphaTheme.colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Last column: savings rate = (income − expenses) ÷ income",
                    fontSize = 11.sp,
                    color = AlphaTheme.colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spending by Category
        FlowCard {
            Column {
                Text(
                    text = "Spending by category · $displayMonth",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AlphaTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (sortedCats.isEmpty()) {
                    EmptyStateView(
                        title = "No expenses this month",
                        message = "Spending will appear here."
                    )
                } else {
                    val maxCatSpend = sortedCats.first().value.coerceAtLeast(1.0)
                    sortedCats.forEachIndexed { index, entry ->
                        val cat = entry.key
                        val amt = entry.value
                        val pct = if (totalExpenseCurrentMonth > 0) Math.round((amt / totalExpenseCurrentMonth) * 100) else 0

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (index < 3) {
                                        Text("★ ", color = AlphaTheme.colors.warning, fontSize = 12.sp)
                                    }
                                    Text(
                                        text = cat,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AlphaTheme.colors.textPrimary
                                    )
                                }
                                Text(
                                    text = "${formatCurrency(amt, uiState.settings.hideBalances)} · $pct%",
                                    fontSize = 12.sp,
                                    color = AlphaTheme.colors.textMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowProgressBar(
                                progress = (amt / maxCatSpend).toFloat(),
                                barColor = AlphaTheme.colors.accent,
                                height = 6.dp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "★ Top spending categories",
                        fontSize = 11.sp,
                        color = AlphaTheme.colors.textMuted
                    )
                }
            }
        }

        // Budgets
        SectionHeader(
            title = "Budgets · $displayMonth",
            actionText = "+ Budget",
            onActionClick = onAddBudget
        )

        FlowCard {
            if (uiState.budgetsWithUsage.isEmpty()) {
                EmptyStateView(
                    title = "No budgets",
                    message = "Set a budget to take control of your spending."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    uiState.budgetsWithUsage.forEach { item ->
                        val b = item.budget
                        val spent = item.currentSpent
                        val limit = b.monthlyLimit
                        val ratio = item.usageRatio

                        val barColor = when {
                            ratio >= 1.0f -> AlphaTheme.colors.expense
                            ratio >= 0.8f -> AlphaTheme.colors.warning
                            else -> AlphaTheme.colors.accent
                        }

                        val statusText = when {
                            ratio >= 1.0f -> "Exceeded by " + formatCurrency(spent - limit, uiState.settings.hideBalances)
                            ratio >= 0.8f -> "Approaching limit (${Math.round(ratio * 100)}% used)"
                            else -> "${Math.round(ratio * 100)}% used"
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = b.category,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = AlphaTheme.colors.textPrimary
                                )
                                Text(
                                    text = "${formatCurrency(spent, uiState.settings.hideBalances)} / ${formatCurrency(limit, uiState.settings.hideBalances)}",
                                    fontSize = 12.sp,
                                    color = barColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowProgressBar(
                                progress = ratio,
                                barColor = barColor,
                                height = 8.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    color = barColor
                                )
                                TextButton(
                                    onClick = { onDeleteBudget(b.category) },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Remove", fontSize = 11.sp, color = AlphaTheme.colors.textMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
