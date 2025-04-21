package com.example.mypiggybank.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mypiggybank.data.Budget

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY startDate DESC LIMIT 1")
    fun getCurrentBudget(): LiveData<Budget?>

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    @Transaction
    suspend fun setNewBudget(budget: Budget) {
        deleteAllBudgets()
        insertBudget(budget)
    }

    @Query("UPDATE budgets SET spent = spent + :amount")
    suspend fun updateSpentAmount(amount: Double)

    @Insert
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)
} 