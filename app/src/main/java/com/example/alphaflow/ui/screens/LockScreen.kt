package com.example.alphaflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.ui.components.AlphaLogo
import com.example.alphaflow.ui.theme.AlphaTheme

@Composable
fun LockScreen(
    onUnlockAttempt: (String) -> Boolean
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun addDigit(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isError = false
            if (newPin.length == 4) {
                val success = onUnlockAttempt(newPin)
                if (!success) {
                    isError = true
                    enteredPin = ""
                }
            }
        }
    }

    fun removeDigit() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AlphaTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            AlphaLogo(size = 72.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ALPHA ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = AlphaTheme.colors.textPrimary
                )
                Text(
                    text = "FLOW",
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp,
                    color = AlphaTheme.colors.textPrimary.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Control your flow. Build your future.",
                fontSize = 12.sp,
                color = AlphaTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = if (isError) "Incorrect PIN. Try again." else "Enter your PIN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isError) AlphaTheme.colors.expense else AlphaTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PIN Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { idx ->
                    val filled = idx < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) AlphaTheme.colors.accent
                                else AlphaTheme.colors.cardBorder
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Keypad
            val buttons = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                buttons.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(64.dp))
                            } else if (key == "DEL") {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable { removeDigit() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Backspace",
                                        tint = AlphaTheme.colors.textPrimary
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(AlphaTheme.colors.cardBackground)
                                        .clickable { addDigit(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AlphaTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
