package com.example.alphaflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.alphaflow.data.model.AccountEntity
import com.example.alphaflow.data.model.BudgetEntity
import com.example.alphaflow.data.model.DayClosureEntity
import com.example.alphaflow.data.model.GoalEntity
import com.example.alphaflow.data.model.TransactionEntity
import com.example.alphaflow.data.model.UserSettingsEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        GoalEntity::class,
        BudgetEntity::class,
        DayClosureEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AlphaFlowDatabase : RoomDatabase() {

    abstract fun dao(): AlphaFlowDao

    companion object {
        @Volatile
        private var INSTANCE: AlphaFlowDatabase? = null

        fun getInstance(context: Context): AlphaFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlphaFlowDatabase::class.java,
                    "alpha_flow.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
