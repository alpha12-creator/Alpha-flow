package com.example.alphaflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.alphaflow.data.model.AccountEntity
import com.example.alphaflow.data.model.BudgetEntity
import com.example.alphaflow.data.model.DayClosureEntity
import com.example.alphaflow.data.model.GoalEntity
import com.example.alphaflow.data.model.TransactionEntity
import com.example.alphaflow.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlphaFlowDao {

    // Accounts
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: String)

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transactions WHERE goalId = :goalId")
    suspend fun deleteTransactionsByGoalId(goalId: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // Goals
    @Query("SELECT * FROM goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: String)

    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()

    // Budgets
    @Query("SELECT * FROM budgets ORDER BY category ASC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun deleteBudgetByCategory(category: String)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    // Day Closures
    @Query("SELECT * FROM day_closures ORDER BY date DESC")
    fun getAllClosures(): Flow<List<DayClosureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosure(closure: DayClosureEntity)

    @Query("DELETE FROM day_closures")
    suspend fun deleteAllClosures()

    // Settings
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)
}
