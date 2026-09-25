package com.example.alphaflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.ui.theme.AlphaTheme

@Composable
fun FlowWaterfallCard(
    income: Double,
    expenses: Double,
    saved: Double,
    remainingBalance: Double,
    hideBalances: Boolean
) {
    val totalIn = if (income > 0) income else 1.0
    val expRatio = (expenses / totalIn).toFloat().coerceIn(0f, 1f)
    val savedRatio = (Math.max(0.0, saved) / totalIn).toFloat().coerceIn(0f, 1f)

    FlowCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left vertical flow accent line
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AlphaTheme.colors.accent)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Income", fontSize = 13.sp, color = AlphaTheme.colors.textPrimary)
                    Text(
                        formatCurrency(income, hideBalances),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AlphaTheme.colors.textPrimary
                    )
                }
                FlowProgressBar(
                    progress = if (income > 0) 1f else 0f,
                    barColor = AlphaTheme.colors.accent,
                    height = 6.dp
                )

                Text(
                    "↓",
                    fontSize = 12.sp,
                    color = AlphaTheme.colors.accent,
                    modifier = Modifier.padding(start = 2.dp)
                )

                // Expenses
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Expenses", fontSize = 13.sp, color = AlphaTheme.colors.textPrimary)
                    Text(
                        formatCurrency(expenses, hideBalances),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AlphaTheme.colors.expense
                    )
                }
                FlowProgressBar(
                    progress = expRatio,
                    barColor = AlphaTheme.colors.expense,
                    height = 6.dp
                )

                Text(
                    "↓",
                    fontSize = 12.sp,
                    color = AlphaTheme.colors.accent,
                    modifier = Modifier.padding(start = 2.dp)
                )

                // Savings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Savings", fontSize = 13.sp, color = AlphaTheme.colors.textPrimary)
                    Text(
                        formatCurrency(Math.max(0.0, saved), hideBalances),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AlphaTheme.colors.accent
                    )
                }
                FlowProgressBar(
                    progress = savedRatio,
                    barColor = AlphaTheme.colors.accent,
                    height = 6.dp
                )

                Text(
                    "↓",
                    fontSize = 12.sp,
                    color = AlphaTheme.colors.accent,
                    modifier = Modifier.padding(start = 2.dp)
                )

                // Remaining Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remaining balance", fontSize = 13.sp, color = AlphaTheme.colors.textPrimary)
                    Text(
                        formatCurrency(remainingBalance, hideBalances),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AlphaTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}
