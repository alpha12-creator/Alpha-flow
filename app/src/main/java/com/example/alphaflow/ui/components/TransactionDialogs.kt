package com.example.alphaflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.data.model.AccountEntity
import com.example.alphaflow.data.model.Categories
import com.example.alphaflow.data.model.GoalEntity
import com.example.alphaflow.data.model.TransactionEntity
import com.example.alphaflow.ui.theme.AlphaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSheet(
    initialType: String, // "inc", "exp", "tr", "sv", "wd"
    existingTx: TransactionEntity? = null,
    accounts: List<AccountEntity>,
    goals: List<GoalEntity>,
    preselectedGoalId: String? = null,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember { mutableStateOf(existingTx?.type ?: initialType) }
    var amountStr by remember { mutableStateOf(existingTx?.let { if (it.amount % 1.0 == 0.0) it.amount.toLong().toString() else it.amount.toString() } ?: "") }
    var selectedAccountId by remember { mutableStateOf(existingTx?.accountId ?: accounts.firstOrNull()?.id ?: "") }
    var selectedToAccountId by remember { mutableStateOf(existingTx?.toAccountId ?: accounts.getOrNull(1)?.id ?: "") }
    var selectedGoalId by remember { mutableStateOf(existingTx?.goalId ?: preselectedGoalId ?: goals.firstOrNull()?.id ?: "") }

    val defaultCategories = if (type == "inc") Categories.INCOME else Categories.EXPENSE
    var selectedCategory by remember { mutableStateOf(existingTx?.category ?: defaultCategories.first()) }
    var dateStr by remember { mutableStateOf(existingTx?.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var noteStr by remember { mutableStateOf(existingTx?.note ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var toAccountDropdownExpanded by remember { mutableStateOf(false) }
    var goalDropdownExpanded by remember { mutableStateOf(false) }

    val title = when {
        existingTx != null -> "Edit Transaction"
        type == "inc" -> "Add Income"
        type == "exp" -> "Add Expense"
        type == "tr" -> "Transfer Money"
        type == "sv" -> "Add to Goal"
        type == "wd" -> "Withdraw from Goal"
        else -> "Transaction"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AlphaTheme.colors.cardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AlphaTheme.colors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AlphaTheme.colors.textMuted
                    )
                }
            }

            // Amount Field
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount (TZS)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AlphaTheme.colors.accent,
                    unfocusedBorderColor = AlphaTheme.colors.cardBorder,
                    focusedLabelColor = AlphaTheme.colors.accent,
                    unfocusedLabelColor = AlphaTheme.colors.textMuted
                )
            )

            // Category (for inc / exp)
            if (type == "inc" || type == "exp") {
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { categoryDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlphaTheme.colors.accent,
                            unfocusedBorderColor = AlphaTheme.colors.cardBorder
                        )
                    )
                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        val cats = if (type == "inc") Categories.INCOME else Categories.EXPENSE
                        cats.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Account selection
            if (type != "tr") {
                val accLabel = if (type == "sv") "From Account" else if (type == "wd") "To Account" else "Account"
                Box {
                    val currentAccName = accounts.find { it.id == selectedAccountId }?.name ?: "Select account"
                    OutlinedTextField(
                        value = currentAccName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(accLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accountDropdownExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { accountDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select account")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlphaTheme.colors.accent,
                            unfocusedBorderColor = AlphaTheme.colors.cardBorder
                        )
                    )
                    DropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Transfer: From and To
                Box {
                    val fromName = accounts.find { it.id == selectedAccountId }?.name ?: "From"
                    OutlinedTextField(
                        value = fromName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Account") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accountDropdownExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { accountDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "From")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Box {
                    val toName = accounts.find { it.id == selectedToAccountId }?.name ?: "To"
                    OutlinedTextField(
                        value = toName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Account") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { toAccountDropdownExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { toAccountDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "To")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = toAccountDropdownExpanded,
                        onDismissRequest = { toAccountDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedToAccountId = acc.id
                                    toAccountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Goal selection (for sv/wd)
            if (type == "sv" || type == "wd") {
                Box {
                    val currentGoalName = goals.find { it.id == selectedGoalId }?.name ?: "Select Goal"
                    OutlinedTextField(
                        value = currentGoalName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Savings Goal") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { goalDropdownExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { goalDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select goal")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = goalDropdownExpanded,
                        onDismissRequest = { goalDropdownExpanded = false }
                    ) {
                        goals.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.name) },
                                onClick = {
                                    selectedGoalId = g.id
                                    goalDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date
            OutlinedTextField(
                value = dateStr,
                onValueChange = { dateStr = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Note (optional)
            if (type != "sv" && type != "wd") {
                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            val tx = TransactionEntity(
                                id = existingTx?.id ?: UUID.randomUUID().toString().substring(0, 8),
                                type = type,
                                amount = amt,
                                accountId = selectedAccountId,
                                toAccountId = if (type == "tr") selectedToAccountId else null,
                                goalId = if (type == "sv" || type == "wd") selectedGoalId else null,
                                date = dateStr.ifBlank { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) },
                                category = if (type == "inc" || type == "exp") selectedCategory else null,
                                note = noteStr.trim().ifEmpty { null }
                            )
                            onSave(tx)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_transaction_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.accent,
                        contentColor = AlphaTheme.colors.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (existingTx != null) "Update" else "Save", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = AlphaTheme.colors.textMuted)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AccountDialog(
    existingAccount: AccountEntity? = null,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingAccount?.name ?: "") }
    var openingBalStr by remember { mutableStateOf(existingAccount?.openingBalance?.toLong()?.toString() ?: "0") }
    var isSavings by remember { mutableStateOf(existingAccount?.isSavings ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingAccount != null) "Edit Account" else "Add Account", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.contains("sav", ignoreCase = true)) {
                            isSavings = true
                        }
                    },
                    label = { Text("Account Name") },
                    singleLine = true,
                    modifier = Modifier.testTag("account_name_input")
                )
                OutlinedTextField(
                    value = openingBalStr,
                    onValueChange = { openingBalStr = it },
                    label = { Text("Opening Balance (TZS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isSavings = !isSavings }
                ) {
                    Checkbox(
                        checked = isSavings,
                        onCheckedChange = { isSavings = it },
                        colors = CheckboxDefaults.colors(checkedColor = AlphaTheme.colors.accent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Designate as Savings Account", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val ob = openingBalStr.toDoubleOrNull() ?: 0.0
                        onSave(
                            AccountEntity(
                                id = existingAccount?.id ?: ("a" + UUID.randomUUID().toString().substring(0, 4)),
                                name = name.trim(),
                                openingBalance = ob,
                                isSavings = isSavings
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AlphaTheme.colors.accent, contentColor = AlphaTheme.colors.background)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GoalDialog(
    existingGoal: GoalEntity? = null,
    onDismiss: () -> Unit,
    onSave: (GoalEntity) -> Unit
) {
    var name by remember { mutableStateOf(existingGoal?.name ?: "") }
    var targetStr by remember { mutableStateOf(existingGoal?.targetAmount?.toLong()?.toString() ?: "") }
    var seedStr by remember { mutableStateOf(existingGoal?.seedAmount?.toLong()?.toString() ?: "0") }
    var dateStr by remember { mutableStateOf(existingGoal?.targetDate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingGoal != null) "Edit Goal" else "New Goal", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    singleLine = true,
                    modifier = Modifier.testTag("goal_name_input")
                )
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target (TZS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (existingGoal == null) {
                    OutlinedTextField(
                        value = seedStr,
                        onValueChange = { seedStr = it },
                        label = { Text("Already Saved (TZS)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Target Date (YYYY-MM-DD)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetStr.toDoubleOrNull() ?: 0.0
                    val seed = seedStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && target > 0) {
                        onSave(
                            GoalEntity(
                                id = existingGoal?.id ?: ("g" + UUID.randomUUID().toString().substring(0, 4)),
                                name = name.trim(),
                                seedAmount = if (existingGoal != null) existingGoal.seedAmount else seed,
                                targetAmount = target,
                                targetDate = dateStr.trim().ifEmpty { null }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AlphaTheme.colors.accent, contentColor = AlphaTheme.colors.background)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BudgetDialog(
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double) -> Unit
) {
    var selectedCat by remember { mutableStateOf(Categories.EXPENSE.first()) }
    var limitStr by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New Budget", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box {
                    OutlinedTextField(
                        value = selectedCat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Categories.EXPENSE.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCat = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Monthly Limit (TZS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.testTag("budget_limit_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitStr.toDoubleOrNull() ?: 0.0
                    if (limit > 0) {
                        onSave(selectedCat, limit)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AlphaTheme.colors.accent, contentColor = AlphaTheme.colors.background)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
