package com.example.alphaflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.data.model.MonthlyStat
import com.example.alphaflow.ui.theme.AlphaTheme

@Composable
fun MonthlyIncomeExpenseChart(
    stats: List<MonthlyStat>,
    modifier: Modifier = Modifier
) {
    if (stats.isEmpty() || stats.all { it.income == 0.0 && it.expense == 0.0 }) {
        EmptyStateView(
            title = "Not enough data",
            message = "Add transactions to see your monthly chart."
        )
        return
    }

    val maxVal = maxOf(1.0, stats.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 1.0)
    val incColor = AlphaTheme.colors.accent
    val expColor = AlphaTheme.colors.expense

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            stats.forEach { stat ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val canvasW = size.width
                        val canvasH = size.height

                        val barWidth = (canvasW * 0.32f).coerceAtMost(16.dp.toPx())
                        val gap = 3.dp.toPx()
                        val totalPairWidth = barWidth * 2 + gap
                        val startX = (canvasW - totalPairWidth) / 2

                        val incH = ((stat.income / maxVal) * canvasH).toFloat().coerceAtLeast(3.dp.toPx())
                        val expH = ((stat.expense / maxVal) * canvasH).toFloat().coerceAtLeast(3.dp.toPx())

                        // Income Bar (Left)
                        drawRoundRect(
                            color = incColor,
                            topLeft = Offset(startX, canvasH - incH),
                            size = Size(barWidth, incH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Expense Bar (Right)
                        drawRoundRect(
                            color = expColor,
                            topLeft = Offset(startX + barWidth + gap, canvasH - expH),
                            size = Size(barWidth, expH),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stat.displayMonth,
                        fontSize = 11.sp,
                        color = AlphaTheme.colors.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawRect(incColor)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Income",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AlphaTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.width(16.dp))

            Canvas(modifier = Modifier.size(8.dp)) {
                drawRect(expColor)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Expenses",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AlphaTheme.colors.textMuted
            )
        }
    }
}
