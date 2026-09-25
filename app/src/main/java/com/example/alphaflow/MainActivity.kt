package com.example.alphaflow

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alphaflow.data.model.AccountEntity
import com.example.alphaflow.data.model.GoalEntity
import com.example.alphaflow.data.model.TransactionEntity
import com.example.alphaflow.ui.components.AccountDialog
import com.example.alphaflow.ui.components.BudgetDialog
import com.example.alphaflow.ui.components.GoalDialog
import com.example.alphaflow.ui.components.TransactionSheet
import com.example.alphaflow.ui.screens.AnalyticsScreen
import com.example.alphaflow.ui.screens.HomeScreen
import com.example.alphaflow.ui.screens.LockScreen
import com.example.alphaflow.ui.screens.SavingsScreen
import com.example.alphaflow.ui.screens.SettingsScreen
import com.example.alphaflow.ui.screens.TransactionsScreen
import com.example.alphaflow.ui.theme.AlphaFlowTheme
import com.example.alphaflow.ui.theme.AlphaTheme
import com.example.alphaflow.ui.viewmodel.AlphaFlowViewModel
import com.example.alphaflow.ui.viewmodel.AlphaFlowViewModelFactory
import com.example.alphaflow.ui.viewmodel.NavigationTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AlphaFlowViewModel by viewModels {
        AlphaFlowViewModelFactory((application as AlphaFlowApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                viewModel.toastEvent.collectLatest { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }

            AlphaFlowTheme(themePreference = uiState.settings.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AlphaTheme.colors.background
                ) {
                    if (uiState.isLocked) {
                        LockScreen(
                            onUnlockAttempt = { pin ->
                                val success = viewModel.unlock(pin)
                                if (!success) {
                                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                                }
                                success
                            }
                        )
                    } else {
                        MainScreenContent(
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    viewModel: AlphaFlowViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog & Sheet States
    var quickAddSheetOpen by remember { mutableStateOf(false) }
    var activeTxType by remember { mutableStateOf<String?>(null) } // "inc", "exp", "tr", "sv", "wd"
    var editingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var preselectedGoalId by remember { mutableStateOf<String?>(null) }

    var accountDialogOpen by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }

    var goalDialogOpen by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<GoalEntity?>(null) }

    var budgetDialogOpen by remember { mutableStateOf(false) }

    var deleteTxTarget by remember { mutableStateOf<TransactionEntity?>(null) }
    var deleteGoalTarget by remember { mutableStateOf<GoalEntity?>(null) }

    // Handle Back Press
    BackHandler(enabled = uiState.currentTab != NavigationTab.HOME) {
        viewModel.selectTab(NavigationTab.HOME)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = AlphaTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AlphaFlowBottomNav(
                currentTab = uiState.currentTab,
                onTabSelect = { viewModel.selectTab(it) },
                onFabClick = { quickAddSheetOpen = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (uiState.currentTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onNavigateTab = { viewModel.selectTab(it) },
                        onToggleHideBalances = { viewModel.toggleHideBalances() },
                        onQuickAdd = { type ->
                            activeTxType = type
                            editingTx = null
                            preselectedGoalId = null
                        },
                        onAddAccount = {
                            editingAccount = null
                            accountDialogOpen = true
                        },
                        onEditAccount = { acc ->
                            editingAccount = acc
                            accountDialogOpen = true
                        },
                        onStartFresh = { viewModel.startFresh() },
                        onCloseDay = { viewModel.closeDay() },
                        onReviewTransactions = {
                            viewModel.updateFilters(uiState.filterState.copy(dateRange = "today"))
                            viewModel.selectTab(NavigationTab.TRANSACTIONS)
                        }
                    )
                }
                NavigationTab.TRANSACTIONS -> {
                    TransactionsScreen(
                        uiState = uiState,
                        onFilterChange = { viewModel.updateFilters(it) },
                        onEditTransaction = { tx ->
                            editingTx = tx
                            activeTxType = tx.type
                        },
                        onDeleteTransaction = { tx ->
                            deleteTxTarget = tx
                        }
                    )
                }
                NavigationTab.SAVINGS -> {
                    SavingsScreen(
                        uiState = uiState,
                        onNewGoal = {
                            editingGoal = null
                            goalDialogOpen = true
                        },
                        onAddMoneyToGoal = { goal ->
                            preselectedGoalId = goal.id
                            activeTxType = "sv"
                            editingTx = null
                        },
                        onWithdrawFromGoal = { goal ->
                            preselectedGoalId = goal.id
                            activeTxType = "wd"
                            editingTx = null
                        },
                        onEditGoal = { goal ->
                            editingGoal = goal
                            goalDialogOpen = true
                        },
                        onDeleteGoal = { goal ->
                            deleteGoalTarget = goal
                        }
                    )
                }
                NavigationTab.ANALYTICS -> {
                    AnalyticsScreen(
                        uiState = uiState,
                        onAddBudget = { budgetDialogOpen = true },
                        onDeleteBudget = { cat -> viewModel.deleteBudget(cat) }
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        uiState = uiState,
                        onNavigateTab = { viewModel.selectTab(it) },
                        onSetTheme = { viewModel.setTheme(it) },
                        onToggleHideBalances = { viewModel.toggleHideBalances() },
                        onSetPin = { viewModel.setPin(it) },
                        onLockNow = { viewModel.lockNow() },
                        onStartFresh = { viewModel.startFresh() },
                        onRestoreDemoData = { viewModel.restoreDemoData() },
                        onExportData = { viewModel.exportDataJson() },
                        onImportData = { viewModel.importDataJson(it) }
                    )
                }
            }
        }
    }

    // Quick Add Sheet
    if (quickAddSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { quickAddSheetOpen = false },
            containerColor = AlphaTheme.colors.cardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add Transaction",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AlphaTheme.colors.textPrimary
                )
                Button(
                    onClick = {
                        quickAddSheetOpen = false
                        activeTxType = "inc"
                        editingTx = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.accent,
                        contentColor = AlphaTheme.colors.background
                    )
                ) {
                    Text("+ Add income", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        quickAddSheetOpen = false
                        activeTxType = "exp"
                        editingTx = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.surfaceVariant,
                        contentColor = AlphaTheme.colors.textPrimary
                    )
                ) {
                    Text("− Add expense", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        quickAddSheetOpen = false
                        activeTxType = "tr"
                        editingTx = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.surfaceVariant,
                        contentColor = AlphaTheme.colors.textPrimary
                    )
                ) {
                    Text("↗ Transfer between accounts", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Transaction Sheet
    if (activeTxType != null) {
        TransactionSheet(
            initialType = activeTxType!!,
            existingTx = editingTx,
            accounts = uiState.accounts,
            goals = uiState.goals,
            preselectedGoalId = preselectedGoalId,
            onDismiss = {
                activeTxType = null
                editingTx = null
                preselectedGoalId = null
            },
            onSave = { tx ->
                val success = viewModel.addOrUpdateTransaction(tx)
                if (success) {
                    activeTxType = null
                    editingTx = null
                    preselectedGoalId = null
                }
            }
        )
    }

    // Account Dialog
    if (accountDialogOpen) {
        AccountDialog(
            existingAccount = editingAccount,
            onDismiss = { accountDialogOpen = false },
            onSave = { acc ->
                viewModel.addOrUpdateAccount(acc)
                accountDialogOpen = false
            }
        )
    }

    // Goal Dialog
    if (goalDialogOpen) {
        GoalDialog(
            existingGoal = editingGoal,
            onDismiss = { goalDialogOpen = false },
            onSave = { goal ->
                viewModel.addOrUpdateGoal(goal)
                goalDialogOpen = false
            }
        )
    }

    // Budget Dialog
    if (budgetDialogOpen) {
        BudgetDialog(
            onDismiss = { budgetDialogOpen = false },
            onSave = { cat, limit ->
                viewModel.addBudget(cat, limit)
                budgetDialogOpen = false
            }
        )
    }

    // Delete Transaction Confirm
    if (deleteTxTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTxTarget = null },
            title = { Text("Delete Transaction?", fontWeight = FontWeight.Bold) },
            text = { Text("This transaction will be removed and balances recalculated.") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteTxTarget?.let { viewModel.deleteTransaction(it.id) }
                        deleteTxTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.expense,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTxTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Goal Confirm
    if (deleteGoalTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteGoalTarget = null },
            title = { Text("Delete Goal?", fontWeight = FontWeight.Bold) },
            text = { Text("Money saved to this goal will return to its source accounts.") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteGoalTarget?.let { viewModel.deleteGoal(it.id) }
                        deleteGoalTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaTheme.colors.expense,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteGoalTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AlphaFlowBottomNav(
    currentTab: NavigationTab,
    onTabSelect: (NavigationTab) -> Unit,
    onFabClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Nav background bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(AlphaTheme.colors.cardBackground)
                .border(1.dp, AlphaTheme.colors.cardBorder),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentTab == NavigationTab.HOME,
                onClick = { onTabSelect(NavigationTab.HOME) },
                testTag = "nav_home"
            )

            NavItem(
                icon = Icons.AutoMirrored.Filled.CompareArrows,
                label = "Transactions",
                isSelected = currentTab == NavigationTab.TRANSACTIONS,
                onClick = { onTabSelect(NavigationTab.TRANSACTIONS) },
                testTag = "nav_transactions"
            )

            // Space for Center FAB
            Spacer(modifier = Modifier.width(48.dp))

            NavItem(
                icon = Icons.Default.PieChart,
                label = "Savings",
                isSelected = currentTab == NavigationTab.SAVINGS,
                onClick = { onTabSelect(NavigationTab.SAVINGS) },
                testTag = "nav_savings"
            )

            NavItem(
                icon = Icons.Default.BarChart,
                label = "Analytics",
                isSelected = currentTab == NavigationTab.ANALYTICS,
                onClick = { onTabSelect(NavigationTab.ANALYTICS) },
                testTag = "nav_analytics"
            )
        }

        // Center Elevated FAB
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(54.dp)
                .testTag("fab_add"),
            shape = RoundedCornerShape(18.dp),
            containerColor = AlphaTheme.colors.accent,
            contentColor = AlphaTheme.colors.background,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val color = if (isSelected) AlphaTheme.colors.accent else AlphaTheme.colors.textMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}
