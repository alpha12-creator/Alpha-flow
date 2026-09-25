package com.example.alphaflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.ui.theme.AlphaTheme
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double, hideBalances: Boolean): String {
    if (hideBalances) return "TZS ••••"
    val isNegative = amount < 0
    val absVal = Math.abs(amount)
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    val formatted = formatter.format(absVal)
    val sign = if (isNegative) "−" else ""
    return "${sign}TZS $formatted"
}

@Composable
fun AlphaLogo(size: Dp = 32.dp) {
    val accent = AlphaTheme.colors.accent
    val barColor = AlphaTheme.colors.textPrimary

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.25f))
            .background(AlphaTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.75f)) {
            val w = this.size.width
            val h = this.size.height

            // Chevron path
            val path = Path().apply {
                moveTo(w * 0.15f, h * 0.85f)
                lineTo(w * 0.5f, h * 0.15f)
                lineTo(w * 0.85f, h * 0.85f)
            }
            drawPath(
                path = path,
                color = accent,
                style = Stroke(
                    width = w * 0.10f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Flow bars inside chevron
            val barW = w * 0.08f
            val cornerR = CornerRadius(barW * 0.35f, barW * 0.35f)

            // Bar 1
            drawRoundRect(
                color = barColor,
                topLeft = Offset(w * 0.33f, h * 0.65f),
                size = Size(barW, h * 0.12f),
                cornerRadius = cornerR
            )
            // Bar 2
            drawRoundRect(
                color = barColor,
                topLeft = Offset(w * 0.45f, h * 0.55f),
                size = Size(barW, h * 0.22f),
                cornerRadius = cornerR
            )
            // Bar 3
            drawRoundRect(
                color = barColor,
                topLeft = Offset(w * 0.57f, h * 0.45f),
                size = Size(barW, h * 0.32f),
                cornerRadius = cornerR
            )
        }
    }
}

@Composable
fun BrandHeader(size: Dp = 28.dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AlphaLogo(size = size)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ALPHA ",
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.6f).sp,
                letterSpacing = 0.5.sp,
                color = AlphaTheme.colors.textPrimary
            )
            Text(
                text = "FLOW",
                fontWeight = FontWeight.Normal,
                fontSize = (size.value * 0.6f).sp,
                letterSpacing = 2.sp,
                color = AlphaTheme.colors.textPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FlowCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AlphaTheme.colors.cardBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = AlphaTheme.colors.cardBackground
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun FlowProgressBar(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    barColor: Color = AlphaTheme.colors.accent,
    backgroundColor: Color = AlphaTheme.colors.cardBorder,
    height: Dp = 8.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(barColor)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AlphaTheme.colors.textPrimary
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = AlphaTheme.colors.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    message: String,
    actionButton: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AlphaTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            fontSize = 12.sp,
            color = AlphaTheme.colors.textMuted,
            textAlign = TextAlign.Center
        )
        if (actionButton != null) {
            Spacer(modifier = Modifier.height(12.dp))
            actionButton()
        }
    }
}
