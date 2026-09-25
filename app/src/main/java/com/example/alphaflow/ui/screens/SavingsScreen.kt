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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.data.model.GoalEntity
import com.example.alphaflow.ui.components.EmptyStateView
import com.example.alphaflow.ui.components.FlowCard
import com.example.alphaflow.ui.components.FlowProgressBar
import com.example.alphaflow.ui.components.SectionHeader
import com.example.alphaflow.ui.components.formatCurrency
import com.example.alphaflow.ui.theme.AlphaTheme
import com.example.alphaflow.ui.viewmodel.UiState

@Composable
fun SavingsScreen(
    uiState: UiState,
    onNewGoal: () -> Unit,
    onAddMoneyToGoal: (GoalEntity) -> Unit,
    onWithdrawFromGoal: (GoalEntity) -> Unit,
    onEditGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit
) {
    val scrollState = rememberScrollState()
    val currentRate = uiState.currentMonthStat?.savingsRate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Savings",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AlphaTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Total Savings Card
        FlowCard {
            Column {
                Text(
                    text = "TOTAL SAVINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlphaTheme.colors.textMuted,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(uiState.totalSavings, uiState.settings.hideBalances),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlphaTheme.colors.textPrimary,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.testTag("total_savings_text")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SAVINGS RATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlphaTheme.colors.textMuted,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (currentRate != null) "$currentRate%" else "—",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlphaTheme.colors.accent
                )
            }
        }

        // Goals Section
        SectionHeader(
            title = "Goals",
            actionText = "+ New goal",
            onActionClick = onNewGoal
        )

        if (uiState.goalsWithProgress.isEmpty()) {
            FlowCard {
                EmptyStateView(
                    title = "No savings goals",
                    message = "Create a goal and start building your future.",
                    actionButton = {
                        Button(
                            onClick = onNewGoal,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlphaTheme.colors.accent,
                                contentColor = AlphaTheme.colors.background
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Create Goal", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.goalsWithProgress.forEach { item ->
                    val goal = item.goal
                    val pct = item.percentage
                    val cur = item.currentAmount

                    FlowCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = AlphaTheme.colors.textPrimary
                                )
                                Text(
                                    text = String.format("%.1f%%", pct),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AlphaTheme.colors.accent
                                )
                            }

                            // Progress Bar
                            FlowProgressBar(
                                progress = pct / 100f,
                                barColor = AlphaTheme.colors.accent,
                                height = 8.dp
                            )

                            // Target & Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${formatCurrency(cur, uiState.settings.hideBalances)} / ${formatCurrency(goal.targetAmount, uiState.settings.hideBalances)}",
                                    fontSize = 12.sp,
                                    color = AlphaTheme.colors.textMuted
                                )
                                if (!goal.targetDate.isNullOrBlank()) {
                                    Text(
                                        text = "by ${goal.targetDate}",
                                        fontSize = 12.sp,
                                        color = AlphaTheme.colors.textMuted
                                    )
                                }
                            }

                            // Action buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { onAddMoneyToGoal(goal) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AlphaTheme.colors.accent,
                                        contentColor = AlphaTheme.colors.background
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Add money", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onWithdrawFromGoal(goal) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Withdraw", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onEditGoal(goal) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Edit", fontSize = 12.sp)
                                }

                                TextButton(
                                    onClick = { onDeleteGoal(goal) },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("✕", color = AlphaTheme.colors.expense, fontWeight = FontWeight.Bold)
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
