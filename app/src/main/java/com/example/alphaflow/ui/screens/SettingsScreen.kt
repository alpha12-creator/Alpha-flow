package com.example.alphaflow.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.ui.components.BrandHeader
import com.example.alphaflow.ui.components.FlowCard
import com.example.alphaflow.ui.components.SectionHeader
import com.example.alphaflow.ui.theme.AlphaTheme
import com.example.alphaflow.ui.viewmodel.NavigationTab
import com.example.alphaflow.ui.viewmodel.UiState

@Composable
fun SettingsScreen(
    uiState: UiState,
    onNavigateTab: (NavigationTab) -> Unit,
    onSetTheme: (String) -> Unit,
    onToggleHideBalances: () -> Unit,
    onSetPin: (String) -> Unit,
    onLockNow: () -> Unit,
    onStartFresh: () -> Unit,
    onRestoreDemoData: () -> Unit,
    onExportData: () -> String,
    onImportData: (String) -> Boolean
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var pinDialogOpen by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var exportDialogOpen by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var importDialogOpen by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }
    var confirmResetDialogOpen by remember { mutableStateOf(false) }
    var confirmFreshDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AlphaTheme.colors.textPrimary
            )
            Button(
                onClick = { onNavigateTab(NavigationTab.HOME) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlphaTheme.colors.surfaceVariant,
                    contentColor = AlphaTheme.colors.textPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Appearance
        SectionHeader(title = "Appearance")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("light" to "Light", "dark" to "Dark", "system" to "System").forEach { (key, label) ->
                val isSelected = uiState.settings.theme == key
                Button(
                    onClick = { onSetTheme(key) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) AlphaTheme.colors.accent else AlphaTheme.colors.cardBackground,
                        contentColor = if (isSelected) AlphaTheme.colors.background else AlphaTheme.colors.textPrimary
                    )
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        // Privacy & Security
        SectionHeader(title = "Privacy & security")
        FlowCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Hide Balances
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hide balances", fontSize = 14.sp, color = AlphaTheme.colors.textPrimary)
                    Button(
                        onClick = onToggleHideBalances,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.settings.hideBalances) AlphaTheme.colors.accent else AlphaTheme.colors.surfaceVariant,
                            contentColor = if (uiState.settings.hideBalances) AlphaTheme.colors.background else AlphaTheme.colors.textPrimary
                        )
                    ) {
                        Text(if (uiState.settings.hideBalances) "On" else "Off", fontSize = 12.sp)
                    }
                }

                // PIN Lock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PIN lock", fontSize = 14.sp, color = AlphaTheme.colors.textPrimary)
                        Text(
                            text = if (uiState.settings.pin.isNotEmpty()) "Set (4-digit)" else "Off",
                            fontSize = 11.sp,
                            color = AlphaTheme.colors.textMuted
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                pinInput = ""
                                pinDialogOpen = true
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (uiState.settings.pin.isNotEmpty()) "Change" else "Set PIN", fontSize = 12.sp)
                        }
                        if (uiState.settings.pin.isNotEmpty()) {
                            Button(
                                onClick = onLockNow,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AlphaTheme.colors.accent,
                                    contentColor = AlphaTheme.colors.background
                                )
                            ) {
                                Text("Lock now", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text(
                    text = "All data stays on this device in your local database. Nothing is sent to a server. The PIN is a screen lock, not encryption.",
                    fontSize = 11.sp,
                    color = AlphaTheme.colors.textMuted,
                    lineHeight = 16.sp
                )
            }
        }

        // Currency
        SectionHeader(title = "Currency")
        FlowCard {
            Text(
                text = "TZS — Tanzanian shilling",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AlphaTheme.colors.textPrimary
            )
        }

        // Data Management
        SectionHeader(title = "Data")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        exportedJsonText = onExportData()
                        exportDialogOpen = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Export JSON", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        importInputText = ""
                        importDialogOpen = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Import JSON", fontSize = 13.sp)
                }
            }

            Button(
                onClick = { confirmFreshDialogOpen = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlphaTheme.colors.accent,
                    contentColor = AlphaTheme.colors.background
                )
            ) {
                Text("Start fresh (clear demo data)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { confirmResetDialogOpen = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlphaTheme.colors.expense,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Text("Restore demo data", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // About
        SectionHeader(title = "About")
        FlowCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BrandHeader(size = 28.dp)
                Text(
                    text = "Version 1.0. A personal money control system: know where money comes from, where it goes, and control where it flows.",
                    fontSize = 12.sp,
                    color = AlphaTheme.colors.textMuted,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // PIN Dialog
    if (pinDialogOpen) {
        AlertDialog(
            onDismissRequest = { pinDialogOpen = false },
            title = { Text("PIN lock", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a 4-digit PIN (leave empty to remove)", fontSize = 12.sp, color = AlphaTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        singleLine = true,
                        placeholder = { Text("4 digits") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.isEmpty() || pinInput.length == 4) {
                            onSetPin(pinInput)
                            pinDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlphaTheme.colors.accent, contentColor = AlphaTheme.colors.background)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pinDialogOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Export Dialog
    if (exportDialogOpen) {
        AlertDialog(
            onDismissRequest = { exportDialogOpen = false },
            title = { Text("Export Data", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Copy this JSON text to back up your data:", fontSize = 12.sp, color = AlphaTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.height(200.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("AlphaFlow Backup", exportedJsonText))
                        exportDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlphaTheme.colors.accent, contentColor = AlphaTheme.colors.background)
                ) {
                    Text("Copy to clipboard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { exportDialogOpen = false }) { Text("Close") }
            }
        )
    }

    // Import Dialog
    if (importDialogOpen) {
        AlertDialog(
            onDismissRequest = { importDialogOpen = false },
            title = { Text("Import Data", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Paste your exported ALPHA FLOW JSON:", fontSize = 12.sp, color = AlphaTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        modifier = Modifier.height(200.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onImportData(importInputText)) {
                            importDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlphaTheme.colors.accent, contentColor = AlphaTheme.colors.background)
                ) {
                    Text("Import", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { importDialogOpen = false }) { Text("Cancel") }
            }
        )
    }

    // Confirm Start Fresh Dialog - Enhanced Safety
    if (confirmFreshDialogOpen) {
        var confirmSafetyChecked by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { confirmFreshDialogOpen = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AlphaTheme.colors.accent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start fresh?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This removes all demo transactions, goals, and budgets. Your accounts will be set to zero so you can enter your own real transactions.",
                        fontSize = 13.sp,
                        color = AlphaTheme.colors.textPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { confirmSafetyChecked = !confirmSafetyChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = confirmSafetyChecked,
                            onCheckedChange = { confirmSafetyChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = AlphaTheme.colors.accent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I understand and want to clear demo records",
                            fontSize = 12.sp,
                            color = AlphaTheme.colors.textPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStartFresh()
                        confirmFreshDialogOpen = false
                    },
                    enabled = confirmSafetyChecked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.accent,
                        contentColor = AlphaTheme.colors.background,
                        disabledContainerColor = AlphaTheme.colors.surfaceVariant,
                        disabledContentColor = AlphaTheme.colors.textMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Start fresh", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmFreshDialogOpen = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Restore Demo Dialog - Enhanced Safety
    if (confirmResetDialogOpen) {
        var confirmSafetyChecked by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { confirmResetDialogOpen = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AlphaTheme.colors.expense
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore demo data?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "⚠️ This will overwrite your currently entered transactions, accounts, and budgets with sample demo records.",
                        fontSize = 13.sp,
                        color = AlphaTheme.colors.textPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { confirmSafetyChecked = !confirmSafetyChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = confirmSafetyChecked,
                            onCheckedChange = { confirmSafetyChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = AlphaTheme.colors.expense)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "I understand this replaces my existing data",
                            fontSize = 12.sp,
                            color = AlphaTheme.colors.textPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreDemoData()
                        confirmResetDialogOpen = false
                    },
                    enabled = confirmSafetyChecked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.expense,
                        contentColor = androidx.compose.ui.graphics.Color.White,
                        disabledContainerColor = AlphaTheme.colors.surfaceVariant,
                        disabledContentColor = AlphaTheme.colors.textMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Restore Demo Data", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmResetDialogOpen = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
