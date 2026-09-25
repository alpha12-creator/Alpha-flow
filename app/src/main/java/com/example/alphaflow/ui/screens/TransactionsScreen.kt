package com.example.alphaflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alphaflow.data.model.Categories
import com.example.alphaflow.data.model.TransactionEntity
import com.example.alphaflow.ui.components.EmptyStateView
import com.example.alphaflow.ui.components.FlowCard
import com.example.alphaflow.ui.components.formatCurrency
import com.example.alphaflow.ui.theme.AlphaTheme
import com.example.alphaflow.ui.viewmodel.TransactionFilterState
import com.example.alphaflow.ui.viewmodel.UiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionsScreen(
    uiState: UiState,
    onFilterChange: (TransactionFilterState) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val filters = uiState.filterState
    val accMap = remember(uiState.accounts) { uiState.accounts.associateBy { it.id } }
    val goalMap = remember(uiState.goals) { uiState.goals.associateBy { it.id } }

    var typeMenuOpen by remember { mutableStateOf(false) }
    var catMenuOpen by remember { mutableStateOf(false) }
    var accMenuOpen by remember { mutableStateOf(false) }
    var dateMenuOpen by remember { mutableStateOf(false) }

    // Group transactions by date
    val grouped = remember(uiState.filteredTransactions) {
        uiState.filteredTransactions.groupBy { it.date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Transactions",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AlphaTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
            value = filters.query,
            onValueChange = { onFilterChange(filters.copy(query = it)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            placeholder = { Text("Search transactions...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = AlphaTheme.colors.textMuted)
            },
            trailingIcon = {
                if (filters.query.isNotEmpty()) {
                    IconButton(onClick = { onFilterChange(filters.copy(query = "")) }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = AlphaTheme.colors.textMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AlphaTheme.colors.accent,
                unfocusedBorderColor = AlphaTheme.colors.cardBorder
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Type Filter
            Box {
                val typeLabel = when (filters.type) {
                    "inc" -> "Income"
                    "exp" -> "Expense"
                    "tr" -> "Transfer"
                    "sv" -> "Savings"
                    "wd" -> "Withdrawal"
                    else -> "All types"
                }
                AssistChip(
                    onClick = { typeMenuOpen = true },
                    label = { Text(typeLabel, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (filters.type.isNotEmpty()) AlphaTheme.colors.accent else AlphaTheme.colors.textPrimary
                    )
                )
                DropdownMenu(expanded = typeMenuOpen, onDismissRequest = { typeMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("All types") }, onClick = { onFilterChange(filters.copy(type = "")); typeMenuOpen = false })
                    DropdownMenuItem(text = { Text("Income") }, onClick = { onFilterChange(filters.copy(type = "inc")); typeMenuOpen = false })
                    DropdownMenuItem(text = { Text("Expense") }, onClick = { onFilterChange(filters.copy(type = "exp")); typeMenuOpen = false })
                    DropdownMenuItem(text = { Text("Transfer") }, onClick = { onFilterChange(filters.copy(type = "tr")); typeMenuOpen = false })
                    DropdownMenuItem(text = { Text("Savings Deposit") }, onClick = { onFilterChange(filters.copy(type = "sv")); typeMenuOpen = false })
                    DropdownMenuItem(text = { Text("Savings Withdrawal") }, onClick = { onFilterChange(filters.copy(type = "wd")); typeMenuOpen = false })
                }
            }

            // Category Filter
            Box {
                val catLabel = filters.category.ifEmpty { "All categories" }
                AssistChip(
                    onClick = { catMenuOpen = true },
                    label = { Text(catLabel, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (filters.category.isNotEmpty()) AlphaTheme.colors.accent else AlphaTheme.colors.textPrimary
                    )
                )
                DropdownMenu(expanded = catMenuOpen, onDismissRequest = { catMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("All categories") }, onClick = { onFilterChange(filters.copy(category = "")); catMenuOpen = false })
                    (Categories.EXPENSE + Categories.INCOME).distinct().forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { onFilterChange(filters.copy(category = cat)); catMenuOpen = false })
                    }
                }
            }

            // Account Filter
            Box {
                val accLabel = accMap[filters.accountId]?.name ?: "All accounts"
                AssistChip(
                    onClick = { accMenuOpen = true },
                    label = { Text(accLabel, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (filters.accountId.isNotEmpty()) AlphaTheme.colors.accent else AlphaTheme.colors.textPrimary
                    )
                )
                DropdownMenu(expanded = accMenuOpen, onDismissRequest = { accMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("All accounts") }, onClick = { onFilterChange(filters.copy(accountId = "")); accMenuOpen = false })
                    uiState.accounts.forEach { a ->
                        DropdownMenuItem(text = { Text(a.name) }, onClick = { onFilterChange(filters.copy(accountId = a.id)); accMenuOpen = false })
                    }
                }
            }

            // Date Filter
            Box {
                val dateLabel = when (filters.dateRange) {
                    "today" -> "Today"
                    "month" -> "This month"
                    else -> "All dates"
                }
                AssistChip(
                    onClick = { dateMenuOpen = true },
                    label = { Text(dateLabel, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (filters.dateRange.isNotEmpty()) AlphaTheme.colors.accent else AlphaTheme.colors.textPrimary
                    )
                )
                DropdownMenu(expanded = dateMenuOpen, onDismissRequest = { dateMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("All dates") }, onClick = { onFilterChange(filters.copy(dateRange = "")); dateMenuOpen = false })
                    DropdownMenuItem(text = { Text("Today") }, onClick = { onFilterChange(filters.copy(dateRange = "today")); dateMenuOpen = false })
                    DropdownMenuItem(text = { Text("This month") }, onClick = { onFilterChange(filters.copy(dateRange = "month")); dateMenuOpen = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.filteredTransactions.isEmpty()) {
            EmptyStateView(
                title = "No transactions found",
                message = "Your money flow will appear here."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grouped.forEach { (dateStr, txsForDate) ->
                    item(key = "header_$dateStr") {
                        val headerText = try {
                            val parsed = LocalDate.parse(dateStr)
                            parsed.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH))
                        } catch (e: Exception) {
                            dateStr
                        }
                        Text(
                            text = headerText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AlphaTheme.colors.textMuted,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(txsForDate, key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            accMap = accMap,
                            goalMap = goalMap,
                            hideBalances = uiState.settings.hideBalances,
                            onEdit = { onEditTransaction(tx) },
                            onDelete = { onDeleteTransaction(tx) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    accMap: Map<String, com.example.alphaflow.data.model.AccountEntity>,
    goalMap: Map<String, com.example.alphaflow.data.model.GoalEntity>,
    hideBalances: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fromAccName = accMap[tx.accountId]?.name ?: "—"
    val toAccName = accMap[tx.toAccountId]?.name ?: "—"
    val goalName = goalMap[tx.goalId]?.name ?: "goal"

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
    val subtitle = "$categoryOrKind · $fromAccName · $typeLabel"

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

    FlowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AlphaTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AlphaTheme.colors.accent
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
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit transaction",
                            tint = AlphaTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
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
